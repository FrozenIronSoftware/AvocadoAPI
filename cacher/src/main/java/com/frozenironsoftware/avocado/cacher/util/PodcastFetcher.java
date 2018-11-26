package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.Cacher;
import com.frozenironsoftware.avocado.data.ItunesGenre;
import com.frozenironsoftware.avocado.data.model.Episode;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.bytes.QueryLimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.itunes.ItunesPodcast;
import com.frozenironsoftware.avocado.data.model.itunes.ItunesSearch;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.JsonSyntaxException;
import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.InvalidFeedException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sql2o.Connection;
import org.sql2o.Query;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class PodcastFetcher {
    private static final ApiCount apiCount = new ApiCount(20);

    /**
     * Perform a search for a query and store all results in the database
     * @param queryLimitedOffsetRequest query
     */
    public static void fetchForQuery(@NotNull QueryLimitedOffsetRequest queryLimitedOffsetRequest) {
        if (queryLimitedOffsetRequest.getQuery() == null || !canPerformQuery(queryLimitedOffsetRequest.getQuery()))
            return;
        storeQuery(queryLimitedOffsetRequest.getQuery());
        ItunesSearch itunesSearch = getPodcastSearch(queryLimitedOffsetRequest.getQuery());
        if (itunesSearch != null && itunesSearch.getResults() != null)
            for (ItunesPodcast podcast : itunesSearch.getResults())
                addOrUpdatePodcastInDatabase(podcast);
    }

    /**
     * Store query in redis
     * @param query query
     */
    private static void storeQuery(String query) {
        query = query.toUpperCase();
        try (Jedis jedis = Cacher.cache.getAuthenticatedJedis()) {
            jedis.zadd(ApiCache.KEY_QUERY_HISTORY, System.currentTimeMillis() + 60 * 60 * 1000, query);
        }
        catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * Check redis to see if the query has been performed in the last hour
     * @param query query
     * @return true if the query has not been performed in the last hour
     */
    private static boolean canPerformQuery(String query) {
        query = query.toUpperCase();
        try (Jedis jedis = Cacher.cache.getAuthenticatedJedis()) {
            jedis.zremrangeByScore(ApiCache.KEY_QUERY_HISTORY, 0, System.currentTimeMillis());
            String cursor = "";
            ScanParams scanParams = new ScanParams();
            scanParams.match(query);
            while (cursor != null && !cursor.equals("0")) {
                ScanResult<Tuple> scanResult = jedis.zscan(ApiCache.KEY_QUERY_HISTORY, cursor, scanParams);
                List<Tuple> result = scanResult.getResult();
                if (result != null && result.size() > 0)
                    return false;
                cursor = scanResult.getStringCursor();
            }
        }
        catch (Exception e) {
            Logger.exception(e);
        }
        return true;
    }

    /**
     * Store a podcast in the database
     * @param itunesPodcast podcast to store
     */
    private static void addOrUpdatePodcastInDatabase(@NotNull ItunesPodcast itunesPodcast) {
        if (itunesPodcast.getCollectionId() == 0 || itunesPodcast.getCollectionName() == null ||
                itunesPodcast.getArtistName() == null || itunesPodcast.getArtworkUrl600() == null ||
                itunesPodcast.getFeedUrl() == null)
            return;
        Podcast podcast = getPodcastByItunesId(itunesPodcast.getCollectionId());
        if (podcast != null)
            updatePodcastInDatabase(itunesPodcast);
        else {
            long id = addPodcastToDatabase(itunesPodcast);
            if (id > -1)
                updatePodcastInfo(id);
        }
    }

    /**
     * Update podcast description and episodes
     * @param id Avocado podcast id
     */
    private static void updatePodcastInfo(long id) {
        List<Podcast> podcasts = PodcastDatabaseUtil.getPodcastFromDatabase(Collections.singletonList(id));
        if (podcasts == null || podcasts.size() != 1)
            return;
        Podcast podcast = podcasts.get(0); // TODO add last update time to database and check it
        try {
            com.icosillion.podengine.models.Podcast feed =
                    new com.icosillion.podengine.models.Podcast(new URL(podcast.getFeedUrl()));
            try {
                setPodcastDescription(id, feed.getDescription());
            }
            catch (MalformedFeedException ignore) {}
            setPodcastEpisodes(id, feed.getEpisodes());
        }
        catch (MalformedFeedException | MalformedURLException | InvalidFeedException | WebbException e) {
            Logger.exception(e);
        }
    }

    /**
     * Update podcast episodes
     * @param id Avocaodo podcast id
     * @param episodes episodes
     */
    private static void setPodcastEpisodes(long id, List<com.icosillion.podengine.models.Episode> episodes) {
        String sqlGetEpisodes = "select * from %s.episodes where podcast_id = :podcast_id;";
        sqlGetEpisodes = String.format(sqlGetEpisodes, DatabaseUtil.schema);
        String sqlRemoveEpisodes = "delete from %s.episodes where podcast_id = :podcast_id";
        sqlRemoveEpisodes = String.format(sqlRemoveEpisodes, DatabaseUtil.schema);
        String sqlAddEpisode = "insert into %s.episodes (podcast_id, episode_id, title, description, plays, type, " +
                "url, guid, date_released) values " +
                "(:podcast_id, :episode_id, :title, :description, :plays, :type, :url, :guid, :date_released);";
        sqlAddEpisode = String.format(sqlAddEpisode, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            // Select
            List<Episode> episodesDatabase = connection.createQuery(sqlGetEpisodes)
                    .addParameter("podcast_id", id)
                    .setColumnMappings(Episode.getColumnMapings())
                    .executeAndFetch(Episode.class);
            // Delete
            connection.createQuery(sqlRemoveEpisodes)
                    .addParameter("podcast_id", id)
                    .executeUpdate();
            // Insert
            Query addEpisode = connection.createQuery(sqlAddEpisode);
            long newEpisodeId = 0;
            for (Episode episodeInDatabase : episodesDatabase)
                if (episodeInDatabase.getEpisodeId() > newEpisodeId)
                    newEpisodeId = episodeInDatabase.getEpisodeId();
            newEpisodeId++;
            for (com.icosillion.podengine.models.Episode episode : episodes) {
                try {
                    Episode databaseEpisode = null;
                    for (Episode episodeDatabase : episodesDatabase)
                        if (episode.getGUID() != null && episodeDatabase.getGuid() != null &&
                                episode.getGUID().equals(episodeDatabase.getGuid()))
                            databaseEpisode = episodeDatabase;
                    String description = "";
                    try {
                        description = episode.getDescription();
                        if (description.length() > 5000)
                            description = description.substring(0, 5000);
                        description = htmlToPlaintext(description);
                    }
                    catch (MalformedFeedException ignore) {}
                    Timestamp dateReleased = null;
                    try {
                        dateReleased = Timestamp.from(episode.getPubDate().toInstant());
                    }
                    catch (DateFormatException ignore) {}
                    if (episode.getEnclosure() == null || episode.getEnclosure().getURL() == null)
                        continue;
                    if (databaseEpisode != null) {
                        addEpisode
                                .addParameter("podcast_id", id)
                                .addParameter("episode_id", databaseEpisode.getEpisodeId())
                                .addParameter("title", episode.getTitle())
                                .addParameter("description", description)
                                .addParameter("plays", databaseEpisode.getPlays())
                                .addParameter("type", determineEpisodeType(episode.getEnclosure().getURL()))
                                .addParameter("url", episode.getEnclosure().getURL().toString())
                                .addParameter("guid", episode.getGUID())
                                .addParameter("date_released", dateReleased)
                                .addToBatch();
                    }
                    else {
                        addEpisode
                                .addParameter("podcast_id", id)
                                .addParameter("episode_id", newEpisodeId++)
                                .addParameter("title", episode.getTitle())
                                .addParameter("description", description)
                                .addParameter("plays", 0)
                                .addParameter("type", determineEpisodeType(episode.getEnclosure().getURL()))
                                .addParameter("url", episode.getEnclosure().getURL().toString())
                                .addParameter("guid", episode.getGUID())
                                .addParameter("date_released", dateReleased)
                                .addToBatch();
                    }
                }
                catch (MalformedFeedException | MalformedURLException e) {
                    Logger.exception(e);
                }
            }
            addEpisode.executeBatch();
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
        }
    }

    /**
     * Remove all HTML tags
     * @param html html
     * @return plain text
     */
    @NotNull
    private static String htmlToPlaintext(@NotNull String html) {
        return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").trim();
    }

    /**
     * Determine the episode type
     * @param url media url
     * @return type
     */
    @NotNull
    private static String determineEpisodeType(@NotNull URL url) {
        String urlString = url.getFile();
        if (urlString.toUpperCase().endsWith(".MP4") || urlString.toUpperCase().endsWith(".MOV") ||
                urlString.toUpperCase().endsWith(".M3U8"))
            return "video";
        if (urlString.toUpperCase().endsWith(".PNG") || urlString.toUpperCase().endsWith(".JPG") ||
                urlString.toUpperCase().endsWith(".JPEG") || urlString.toUpperCase().endsWith(".GIF"))
            return "image";
        return "audio";
    }

    /**
     * Update the description of a podcast
     * @param id Avocado podcast id
     * @param description description
     */
    private static void setPodcastDescription(long id, String description) {
        description = htmlToPlaintext(description);
        String sql = "update %s.podcasts set description = :description where id = :id;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            connection.createQuery(sql)
                    .addParameter("id", id)
                    .addParameter("description", description)
                    .executeUpdate();
            DatabaseUtil.releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
        }
    }

    /**
     * Add podcast to database
     * @param itunesPodcast podcast
     */
    private static long addPodcastToDatabase(ItunesPodcast itunesPodcast) {
        String sql = "insert into %s.podcasts (title, image, description, author, itunes_id, feed_url, genre) values " +
                "(:title, :image, :description, :author, :itunes_id, :feed_url, :genre);";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            Connection result = connection.createQuery(sql, true)
                    .addParameter("title", itunesPodcast.getCollectionName())
                    .addParameter("image", itunesPodcast.getArtworkUrl600())
                    .addParameter("description", "")
                    .addParameter("author", itunesPodcast.getArtistName())
                    .addParameter("itunes_id", itunesPodcast.getCollectionId())
                    .addParameter("feed_url", itunesPodcast.getFeedUrl())
                    .addParameter("genre", genreFromIds(itunesPodcast.getGenreIds()))
                    .executeUpdate();
            long id = result.getKey(Long.class);
            DatabaseUtil.releaseConnection(connection);
            return id;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
        }
        return -1;
    }

    /**
     * Convert a list of genre ids to the most general genre tag
     * @param genreIds ids
     * @return genre tag
     */
    private static String genreFromIds(List<String> genreIds) {
        for (ItunesGenre itunesGenre : ItunesGenre.values()) {
            for (String id : genreIds) {
                if (itunesGenre.getId().equals(id))
                    return itunesGenre.getLetterCode();
            }
        }
        return "General Variety";
    }

    /**
     * Update podcast data in database
     * @param itunesPodcast podcast
     */
    private static void updatePodcastInDatabase(ItunesPodcast itunesPodcast) {
        String sql = "update %s.podcasts set title = :title, image = :image, author = :author, feed_url = :feed_url, " +
                "genre = :genre where itunes_id = :itunes_id;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            connection.createQuery(sql)
                    .addParameter("title", itunesPodcast.getCollectionName())
                    .addParameter("image", itunesPodcast.getArtworkUrl600())
                    .addParameter("author", itunesPodcast.getArtistName())
                    .addParameter("itunes_id", itunesPodcast.getCollectionId())
                    .addParameter("feed_url", itunesPodcast.getFeedUrl())
                    .addParameter("genre", genreFromIds(itunesPodcast.getGenreIds()))
                    .executeUpdate();
            DatabaseUtil.releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
        }
    }

    /**
     * Get podcasts by Itunes collection id
     * @param collectionId collection id
     * @return podcast matching the id
     */
    @Nullable
    private static Podcast getPodcastByItunesId(long collectionId) {
        String sql = "select * from %s.podcasts where itunes_id = :itunes_id;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            List<Podcast> podcasts =  connection.createQuery(sql)
                    .addParameter("itunes_id", collectionId)
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetch(Podcast.class);
            DatabaseUtil.releaseConnection(connection);
            if (podcasts.size() != 1)
                return null;
            return podcasts.get(0);
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Make a request to the iTunes search API
     * @param query podcast query
     * @return parsed response
     */
    @Nullable
    private static ItunesSearch getPodcastSearch(String query) {
        synchronized (apiCount) {
            try {
                apiCount.waitForFree(60);
            }
            catch (TimeoutException e) {
                Logger.exception(e);
                return null;
            }
            apiCount.use();
        }
        Webb webb = WebbUtil.getWebb();
        try {
            Response<String> response = webb.get("https://itunes.apple.com/search")
                    .param("media", "podcast")
                    .param("term", query)
                    .ensureSuccess()
                    .asString();
            if (response.getBody() == null)
                return null;
            return Avocado.gson.fromJson(response.getBody(), ItunesSearch.class);
        }
        catch (WebbException | JsonSyntaxException e) {
            if (e instanceof WebbException) {
                if (((WebbException) e).getResponse().getStatusCode() == 403)
                    apiCount.useAll();
            }
            Logger.exception(e);
            return null;
        }
    }
}
