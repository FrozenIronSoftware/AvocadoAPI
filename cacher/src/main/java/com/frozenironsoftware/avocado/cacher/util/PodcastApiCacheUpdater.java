package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.Cacher;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.SortOrder;
import com.frozenironsoftware.avocado.data.model.Episode;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.PodcastPlay;
import com.frozenironsoftware.avocado.data.model.UserFavorite;
import com.frozenironsoftware.avocado.data.model.UserRecent;
import com.frozenironsoftware.avocado.data.model.bytes.EpisodesRequest;
import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.sql2o.Connection;
import org.sql2o.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class PodcastApiCacheUpdater {
    /**
     * Update favorite pocasts for a user with pagination
     * @param userIdLimitedOffsetRequest user id offset and limit
     */
    static void updateUserFavoritePodcastCache(UserIdLimitedOffsetRequest userIdLimitedOffsetRequest) {
        if (!checkLimitAndOffset(userIdLimitedOffsetRequest))
            return;
        List<Podcast> podcasts = getUserFavoritePodcastsOrderedByUnwatched(userIdLimitedOffsetRequest.getUserId(),
                userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
        if (podcasts != null) {
            String cacheId = ApiCache.createKey("podcasts/favorites", userIdLimitedOffsetRequest.getUserId(),
                    userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(podcasts));
        }
    }

    /**
     * Ensure the limit and offset are within the limits
     * @param userIdLimitedOffsetRequest user id limited offset request
     * @return is the limit and offset within range
     */
    private static boolean checkLimitAndOffset(UserIdLimitedOffsetRequest userIdLimitedOffsetRequest) {
        return checkLimitAndOffset(userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
    }

    /**
     * Ensure the limit and offset are within the limits
     * @param limitedOffsetRequest limit and offset
     * @return within range
     */
    private static boolean checkLimitAndOffset(LimitedOffsetRequest limitedOffsetRequest) {
        return checkLimitAndOffset(limitedOffsetRequest.getLimit(), limitedOffsetRequest.getOffset());
    }

    /**
     * Ensure the limit and offset are within the limits
     * @param limit limit
     * @param offset offset
     * @return if limit and offset within range
     */
    static boolean checkLimitAndOffset(int limit, long offset) {
        if (limit < 1 || limit > DefaultConfig.ITEM_LIMIT)
            return false;
        return offset >= 0 && offset <= DefaultConfig.MAX_OFFSET;
    }

    /**
     * Get a list of favorite podcasts for a user ordered by the number of unplayed episodes there are
     * @param userId user id
     * @param limit result limit
     * @param offset result offset
     * @return podcasts
     */
    @Nullable
    private static List<Podcast> getUserFavoritePodcastsOrderedByUnwatched(long userId, int limit, long offset) {
        String sqlAllFavorites = "select * from %s.user_favorites where user_id = :user_id;";
        sqlAllFavorites = String.format(sqlAllFavorites, DatabaseUtil.schema);
        StringBuilder sqlPlayed = new StringBuilder("select * from %s.user_plays where user_id = :user_id and (");
        StringBuilder sqlEpisodes = new StringBuilder("select * from %s.episodes where ");
        StringBuilder sqlPodcasts = new StringBuilder("select * from %s.podcasts where ");
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            // Get favorite podcast ids
            List<UserFavorite> favoritePodcasts = connection.createQuery(sqlAllFavorites)
                    .addParameter("user_id", userId)
                    .setColumnMappings(UserFavorite.getColumnMapings())
                    .executeAndFetch(UserFavorite.class);
            if (favoritePodcasts.size() == 0) {
                connection.commit();
                DatabaseUtil.releaseConnection(connection);
                return new ArrayList<>();
            }
            // Finish SQL queries with favorite podcast ids
            List<Long> favoriteIds = new ArrayList<>();
            for (int paramIndex = 1; paramIndex <= favoritePodcasts.size(); paramIndex++) {
                long podcastId = favoritePodcasts.get(paramIndex - 1).getPodcastId();
                favoriteIds.add(podcastId);
                sqlPlayed.append("podcast_id = :p").append(paramIndex);
                sqlEpisodes.append("podcast_id = :p").append(paramIndex);
                sqlPodcasts.append("id = :p").append(paramIndex);
                if (paramIndex < favoritePodcasts.size()) {
                    sqlPlayed.append(" or ");
                    sqlEpisodes.append(" or ");
                    sqlPodcasts.append(" or ");
                }
                if (paramIndex == favoritePodcasts.size()) {
                    sqlPlayed.append(");");
                    sqlEpisodes.append(";");
                    sqlPodcasts.append(";");
                }
            }
            // Fetch played episodes
            List<PodcastPlay> podcastPlays = connection.createQuery(String.format(sqlPlayed.toString(),
                    DatabaseUtil.schema))
                    .addParameter("user_id", userId)
                    .withParams(favoriteIds.toArray())
                    .setColumnMappings(PodcastPlay.getColumnMapings())
                    .executeAndFetch(PodcastPlay.class);
            // Fetch podcast episodes
            List<Episode> podcastEpisodes = connection.createQuery(String.format(sqlEpisodes.toString(),
                    DatabaseUtil.schema))
                    .withParams(favoriteIds.toArray())
                    .setColumnMappings(Episode.getColumnMapings())
                    .executeAndFetch(Episode.class);
            // Fetch podcasts
            List<Podcast> podcasts = connection.createQuery(String.format(sqlPodcasts.toString(), DatabaseUtil.schema))
                    .withParams(favoriteIds.toArray())
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetch(Podcast.class);
            // Commit and close connection
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            // Order and return
            Map<Long, Integer> unplayed = new HashMap<>();
            for (Long favoriteId : favoriteIds)
                unplayed.put(favoriteId, 0);
            for (Episode episode : podcastEpisodes) {
                boolean played = false;
                for (PodcastPlay play : podcastPlays) {
                    if (play.getPodcastId() == episode.getPodcastId() &&
                            Objects.equals(play.getEpisodeGuid(), episode.getGuid())) {
                        played = true;
                        break;
                    }
                }
                if (!played)
                    unplayed.put(episode.getPodcastId(), unplayed.getOrDefault(episode.getPodcastId(), 0) + 1);
            }
            List<Map.Entry<Long, Integer>> unplayedSorted = unplayed.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
            List<Podcast> favorites = new ArrayList<>();
            for (int unplayedIndex = (int) offset; unplayedIndex < Math.min(offset + limit, unplayedSorted.size());
                 unplayedIndex++) {
                Map.Entry<Long, Integer> unplayedEntry = unplayedSorted.get(unplayedIndex);
                for (Podcast favorite : podcasts) {
                    if (favorite.getId() == unplayedEntry.getKey()) {
                        favorite.setUnplayedEpisodes(unplayedEntry.getValue());
                        favorites.add(favorite);
                    }
                }
            }
            return favorites;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Update user recent podcasts cache
     * @param userIdLimitedOffsetRequest params
     */
    static void updateUserRecentPodcastCache(UserIdLimitedOffsetRequest userIdLimitedOffsetRequest) {
        List<Podcast> podcasts = getUserRecentPodcasts(userIdLimitedOffsetRequest.getUserId(),
                userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
        if (podcasts != null) {
            String cacheId = ApiCache.createKey("podcasts/recents", userIdLimitedOffsetRequest.getUserId(),
                    userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(podcasts));
        }
    }

    /**
     * Poll database for user recent podcasts
     * @param userId user id
     * @param limit result limit
     * @param offset result offset
     * @return podcasts
     */
    @Nullable
    private static List<Podcast> getUserRecentPodcasts(long userId, int limit, long offset) {
        String sqlRecents = "select * from %s.user_recents where user_id = :user_id order by added desc limit :limit" +
                " offset :offset rows;";
        sqlRecents = String.format(sqlRecents, DatabaseUtil.schema);
        StringBuilder sqlPodcasts = new StringBuilder("select * from %s.podcasts where ");
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            List<UserRecent> userRecents = connection.createQuery(sqlRecents)
                    .addParameter("user_id", userId)
                    .addParameter("limit", limit)
                    .addParameter("offset", offset)
                    .setColumnMappings(UserRecent.getColumnMapings())
                    .executeAndFetch(UserRecent.class);
            if (userRecents.size() == 0) {
                connection.commit();
                DatabaseUtil.releaseConnection(connection);
                return new ArrayList<>();
            }
            List<Long> podcastIds = new ArrayList<>();
            for (int userRecentIndex = 1; userRecentIndex <= userRecents.size(); userRecentIndex++) {
                long podcastId = userRecents.get(userRecentIndex - 1).getPodcastId();
                podcastIds.add(podcastId);
                sqlPodcasts.append("id = :p").append(userRecentIndex);
                if (userRecentIndex < userRecents.size()) {
                    sqlPodcasts.append(" or ");
                }
                if (userRecentIndex == userRecents.size()) {
                    sqlPodcasts.append(" limit :limit offset :offset rows;");
                }
            }
            List<Podcast> podcasts = connection.createQuery(String.format(sqlPodcasts.toString(), DatabaseUtil.schema))
                    .withParams(podcastIds.toArray())
                    .addParameter("limit", limit)
                    .addParameter("offset", offset)
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetch(Podcast.class);
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            return podcasts;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Fetch the popular podcasts with the specified offset and limit and add it to the Redis cache
     * @param limitedOffsetRequest limit and offset
     */
    static void updatePopularPodcastCache(LimitedOffsetRequest limitedOffsetRequest) {
        if (!checkLimitAndOffset(limitedOffsetRequest))
            return;
        List<Podcast> podcasts = getPopularPodcastsFromDatabase(limitedOffsetRequest.getLimit(),
                limitedOffsetRequest.getOffset());
        if (podcasts != null) {
            String cacheId = ApiCache.createKey("podcasts/popular", limitedOffsetRequest.getLimit(),
                    limitedOffsetRequest.getOffset());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(podcasts));
        }
    }

    /**
     * Fetch popular podcasts (order by plays)
     * @param limit results limit
     * @param offset row offset
     * @return list of popular podcasts or null on error
     */
    @Nullable
    private static List<Podcast> getPopularPodcastsFromDatabase(int limit, long offset) {
        String sql = "select * from %s.podcasts order by plays desc limit :limit offset :offset rows;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            List<Podcast> podcasts = connection.createQuery(sql)
                    .addParameter("limit", limit)
                    .addParameter("offset", offset)
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetch(Podcast.class);
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            return podcasts;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Update podcsts for the given IDs
     * @param stringArrayRequest string array request
     */
    static void updatePodcastsCache(StringArrayRequest stringArrayRequest) {
        List<String> podcastIds = stringArrayRequest.getStrings();
        List<Long>  longPodcastIds = new ArrayList<>();
        for (String id : podcastIds) {
            try {
                longPodcastIds.add(Long.parseLong(id));
            }
            catch (NumberFormatException ignore) {}
        }
        List<Podcast> podcasts = PodcastDatabaseUtil.getPodcastFromDatabase(longPodcastIds);
        if (podcasts != null) {
            Map<String, String> podcastMap = new HashMap<>();
            for (Podcast podcast : podcasts) {
                try {
                    podcastMap.put(String.valueOf(podcast.getId()), Avocado.gson.toJson(podcast));
                }
                catch (JsonSyntaxException e) {
                    Logger.exception(e);
                }
            }
            Cacher.cache.msetWithPrefix(ApiCache.PREFIX_PODCAST, podcastMap);
        }
    }

    /**
     * Update podcast episode cache
     * @param episodesRequest episode request
     */
    static void updatePodcastEpisodesCache(EpisodesRequest episodesRequest) {
        List<Episode> episodes = getPodcastEpisodesFromDatabase(episodesRequest.getUserId(), episodesRequest.getLimit(),
                episodesRequest.getOffset(), episodesRequest.getPodcastId(), episodesRequest.getSortOrder(),
                episodesRequest.getEpisodeId());
        if (episodes != null) {
            String cacheId = ApiCache.createKey("podcasts/episodes", episodesRequest.getUserId(),
                    episodesRequest.getLimit(), episodesRequest.getOffset(), episodesRequest.getPodcastId(),
                    episodesRequest.getSortOrder().ordinal(), episodesRequest.getEpisodeId());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(episodes));
        }
    }

    /**
     * Get podcasts episodes
     * @param userId user id
     * @param limit result limit
     * @param offset result row offset
     * @param podcastId podcast id of episodes
     * @param sortOrder order
     * @param episodeId episode id
     * @return episodes or null on error
     */
    @Nullable
    private static List<Episode> getPodcastEpisodesFromDatabase(long userId, int limit, long offset, long podcastId,
                                                                SortOrder sortOrder, long episodeId) {
        String sqlGetEpisodes = "select * from %s.episodes where podcast_id = :podcast_id %s order by date_released %s" +
                " limit :limit offset :offset rows;";
        String episodeAnd = episodeId > -1 ? "and episode_id = :episode_id" : "";
        sqlGetEpisodes = String.format(sqlGetEpisodes, DatabaseUtil.schema, episodeAnd, sortOrder.name());
        StringBuilder sqlGetPlays = new StringBuilder("select * from %s.user_plays where podcast_id = :podcast_id" +
                " and user_id = :user_id and" +
                " (");
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            List<Episode> episodes;
            Query getEpisodesQuery = connection.createQuery(sqlGetEpisodes)
                    .addParameter("podcast_id", podcastId)
                    .addParameter("limit", limit)
                    .addParameter("offset", offset)
                    .setColumnMappings(Episode.getColumnMapings());
            if (episodeId > -1) {
                getEpisodesQuery.addParameter("episode_id", episodeId);
            }
            episodes = getEpisodesQuery.executeAndFetch(Episode.class);
            if (episodes.size() == 0) {
                connection.commit();
                DatabaseUtil.releaseConnection(connection);
                return episodes;
            }
            List<String> episodeGuids = new ArrayList<>();
            for (int episodeIndex = 1; episodeIndex <= episodes.size(); episodeIndex++) {
                String episodeGuidLoop = episodes.get(episodeIndex - 1).getGuid();
                episodeGuids.add(episodeGuidLoop);
                sqlGetPlays.append("episode_guid = :p").append(episodeIndex);
                if (episodeIndex < episodes.size()) {
                    sqlGetPlays.append(" or ");
                }
                else {
                    sqlGetPlays.append(");");
                }
            }
            List<PodcastPlay> plays = connection.createQuery(String.format(sqlGetPlays.toString(), DatabaseUtil.schema))
                    .withParams(episodeGuids.toArray())
                    .addParameter("podcast_id", podcastId)
                    .addParameter("user_id", userId)
                    .setColumnMappings(PodcastPlay.getColumnMapings())
                    .executeAndFetch(PodcastPlay.class);
            for (PodcastPlay play : plays) {
                for (Episode episode : episodes) {
                    if (Objects.equals(episode.getGuid(), play.getEpisodeGuid())) {
                        episode.setProgress(play.getProgress());
                        episode.setPosition(play.getPosition());
                        break;
                    }
                }
            }
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            return episodes;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }
}
