package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.Cacher;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.bytes.QueryLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import org.sql2o.Connection;
import org.sql2o.ResultSetIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class SearchApiCacheUpdater {
    /**
     * Search the database for podcasts
     * This also propagates the query to the podcast fetcher
     * @param queryLimitedOffsetRequest query
     */
    static void updateSearchCache(QueryLimitedOffsetRequest queryLimitedOffsetRequest) {
        if (!PodcastApiCacheUpdater.checkLimitAndOffset(queryLimitedOffsetRequest.getLimit(),
                queryLimitedOffsetRequest.getOffset()))
            return;
        if (queryLimitedOffsetRequest.getQuery() == null || queryLimitedOffsetRequest.getQuery().isEmpty())
            return;
        // Forward the request to the fetcher
        Cacher.queue.cacheRequest(MessageQueue.TYPE.FETCH_PODCASTS, queryLimitedOffsetRequest,
                MessageQueue.ROUTING_KEY_POD_CACHER);
        // Search the database
        List<Podcast> podcasts = searchDatabase(queryLimitedOffsetRequest.getQuery());
        if (podcasts != null) {
            String cacheId = ApiCache.createKey("search", queryLimitedOffsetRequest.getQuery(),
                    queryLimitedOffsetRequest.getLimit(), queryLimitedOffsetRequest.getOffset());
            Cacher.cache.set(cacheId, Avocado.gson.toJson(podcasts));
        }
    }

    /**
     * Search the database
     * @param query query
     * @return list of podcasts matching the query or null on error
     */
    private static List<Podcast> searchDatabase(String query) {
        String sql = "select * from %s.podcasts;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        List<Podcast> podcastsStartMatch = new ArrayList<>();
        List<Podcast> podcastsAllWordsMatch = new ArrayList<>();
        List<Podcast> podcastsHalfWordMatch = new ArrayList<>();
        try {
            connection = DatabaseUtil.getTransaction();
            try (ResultSetIterable<Podcast> databasePodcasts = connection.createQuery(sql)
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetchLazy(Podcast.class)) {
                for (Podcast podcast : databasePodcasts) {
                    if (podcast.getTitle() != null) {
                        // Start
                        if (podcast.getTitle().toUpperCase(Locale.US).startsWith(query.toUpperCase(Locale.US)))
                            podcastsStartMatch.add(podcast);
                        // Words
                        else {
                            if (podcastsStartMatch.size() + podcastsAllWordsMatch.size() +
                                    podcastsHalfWordMatch.size() >= DefaultConfig.ITEM_LIMIT)
                                break;
                            int matchCount = 0;
                            String[] queryWords = query.split("\\s");
                            for (String queryWord : queryWords)
                                if (podcast.getTitle() != null &&
                                        podcast.getTitle().toUpperCase(Locale.US)
                                                .contains(queryWord.toUpperCase(Locale.US)))
                                    matchCount++;
                            if (matchCount >= Math.round(queryWords.length / 2.0))
                                podcastsHalfWordMatch.add(podcast);
                            else if (matchCount == queryWords.length)
                                podcastsAllWordsMatch.add(podcast);
                        }
                    }
                }
            }
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            List<Podcast> allPodcasts = new ArrayList<>(podcastsStartMatch);
            allPodcasts.addAll(podcastsAllWordsMatch);
            allPodcasts.addAll(podcastsHalfWordMatch);
            return allPodcasts;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }
}
