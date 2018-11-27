package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.bytes.LongRequest;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import org.sql2o.Connection;
import org.sql2o.ResultSetIterable;

public class UpdateRequester {
    static final long TIME_DAY = 24 * 60 * 60 * 1000;
    private final MessageQueue messageQueue;

    public UpdateRequester(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    /**
     * Loop through all podcasts in the database and request updates for those that have not had an update in the past
     * day
     */
    public void checkDatabaseAndRequestOutdated() {
        String sql = "select * from %s.podcasts";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            try (ResultSetIterable<Podcast> podcasts = connection.createQuery(sql)
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetchLazy(Podcast.class)) {
                for (Podcast podcast : podcasts) {
                    if (podcast.getLastUpdate() == null ||
                            System.currentTimeMillis() - podcast.getLastUpdate().getTime() > TIME_DAY) {
                        LongRequest podcastIdRequest = new LongRequest(podcast.getId());
                        messageQueue.cacheRequest(MessageQueue.TYPE.UPDATE_PODCAST, podcastIdRequest,
                                MessageQueue.ROUTING_KEY_POD_CACHER);
                    }
                }
            }
            DatabaseUtil.releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
        }
    }
}
