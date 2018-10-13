package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.Cacher;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.Episode;
import com.frozenironsoftware.avocado.data.model.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.PodcastPlay;
import com.frozenironsoftware.avocado.data.model.UserFavorite;
import com.frozenironsoftware.avocado.data.model.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import org.jetbrains.annotations.Nullable;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PodcastApiCacheUpdater {
    /**
     * Update favorite pocasts for a user with pagination
     * @param userIdLimitedOffsetRequest user id offset and limit
     */
    static void updateUserFavoritePodcastCache(UserIdLimitedOffsetRequest userIdLimitedOffsetRequest) {
        List<Podcast> podcasts = getUserFavoritePodcastsOrderedByUnwatched(userIdLimitedOffsetRequest.getUserId(),
                userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
        if (podcasts != null) {
            String cacheId = ApiCache.createKey("podcasts/favorites", userIdLimitedOffsetRequest.getUserId(),
                    userIdLimitedOffsetRequest.getLimit(), userIdLimitedOffsetRequest.getOffset());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(podcasts));
        }
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
        String sqlAllFavorites = "select * from %s.user_favorites where user_id = :user_id limit :limit;";
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
                    .addParameter("limit", DefaultConfig.MAX_USER_PODCAST_FAVORITES)
                    .addColumnMapping("date_favorited", "dateFavorited")
                    .addColumnMapping("user_id", "userId")
                    .addColumnMapping("podcast_id", "podcastId")
                    .executeAndFetch(UserFavorite.class);
            if (favoritePodcasts.size() == 0) {
                connection.commit();
                DatabaseUtil.releaseConnection(connection);
                return null;
            }
            // Finish SQL queries with favorite podcast ids
            List<Long> favoriteIds = new ArrayList<>();
            for (int paramIndex = 1; paramIndex <= favoritePodcasts.size(); paramIndex++) {
                long podcastId = favoritePodcasts.get(paramIndex - 1).getId();
                favoriteIds.add(podcastId);
                sqlPlayed.append("podcast_id = :p").append(paramIndex);
                sqlEpisodes.append("podcast_id = :p").append(paramIndex);
                sqlPodcasts.append("id = :p").append(paramIndex);
                if (paramIndex > 1 && paramIndex < favoritePodcasts.size()) {
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
                    .addColumnMapping("user_id", "userId")
                    .addColumnMapping("podcast_id", "podcastId")
                    .addColumnMapping("episode_id", "episodeId")
                    .executeAndFetch(PodcastPlay.class);
            // Fetch podcast episodes
            List<Episode> podcastEpisodes = connection.createQuery(String.format(sqlEpisodes.toString(),
                    DatabaseUtil.schema))
                    .withParams(favoriteIds.toArray())
                    .addColumnMapping("podcast_id", "podcastId")
                    .addColumnMapping("episode_id", "episodeId")
                    .executeAndFetch(Episode.class);
            // Fetch podcasts
            List<Podcast> podcasts = connection.createQuery(String.format(sqlPodcasts.toString(), DatabaseUtil.schema))
                    .withParams(favoriteIds.toArray())
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
                            play.getEpisodeId() == episode.getEpisodeId()) {
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
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    static void updateUserRecentPodcastCache(UserIdLimitedOffsetRequest userIdLimitedOffsetRequest) {

    }

    /**
     * Fetch the popular podcasts with the specified offset and limit and add it to the Redis cache
     * @param limitedOffsetRequest limit and offset
     */
    static void updatePopularPodcastCache(LimitedOffsetRequest limitedOffsetRequest) {
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
                    .executeAndFetch(Podcast.class);
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            return podcasts;
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }
}
