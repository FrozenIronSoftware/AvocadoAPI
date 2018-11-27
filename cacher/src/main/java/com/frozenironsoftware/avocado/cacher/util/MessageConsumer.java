package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.data.model.bytes.EpisodesRequest;
import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.LongRequest;
import com.frozenironsoftware.avocado.data.model.bytes.QueryLimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

public class MessageConsumer extends DefaultConsumer implements ShutdownListener {
    private final MessageQueue queue;

    public MessageConsumer(MessageQueue messageQueue) {
        super(messageQueue.getChannel());
        this.queue = messageQueue;
        queue.addConsumer(this);
        queue.addShutdownListener(this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        if (!envelope.getRoutingKey().equals(queue.getRoutingKey())) {
            if (queue.connect() && queue.getChannel() != null) {
                queue.getChannel().basicNack(envelope.getDeliveryTag(), false, false);
            }
            return;
        }
        if (properties.getContentType() != null && body != null) {
            // TODO Thread this
            try {
                MessageQueue.TYPE type = MessageQueue.TYPE.valueOf(properties.getContentType());
                Logger.extra("Message Consumer received: %s", type.name());
                switch (type) {
                    case GET_FAVORITE_PODCASTS:
                        PodcastApiCacheUpdater.updateUserFavoritePodcastCache(new UserIdLimitedOffsetRequest(body));
                        break;
                    case GET_RECENT_PODCASTS:
                        PodcastApiCacheUpdater.updateUserRecentPodcastCache(new UserIdLimitedOffsetRequest(body));
                        break;
                    case GET_POPULAR_PODCASTS:
                        PodcastApiCacheUpdater.updatePopularPodcastCache(new LimitedOffsetRequest(body));
                        break;
                    case GET_PODCASTS:
                        PodcastApiCacheUpdater.updatePodcastsCache(new StringArrayRequest(body));
                        break;
                    case GET_EPISODES:
                        PodcastApiCacheUpdater.updatePodcastEpisodesCache(new EpisodesRequest(body));
                        break;
                    case GET_SEARCH:
                        SearchApiCacheUpdater.updateSearchCache(new QueryLimitedOffsetRequest(body));
                        break;
                    case FETCH_PODCASTS:
                        PodcastFetcher.fetchForQuery(new QueryLimitedOffsetRequest(body));
                        break;
                    case UPDATE_PODCAST:
                        PodcastFetcher.updatePodcast(new LongRequest(body));
                        break;
                    default:
                        Logger.warn("Unhandled message: %s", type.name());
                }
            }
            catch (Exception e) {
                Logger.exception(e);
            }
        }
        if (queue.connect() && queue.getChannel() != null) {
            queue.getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        queue.connect();
        while (queue.getChannel() == null) {
            Logger.debug("Message queue connection closed. Retrying connection in 5 seconds");
            queue.connect();
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException ignore) {}
        }
    }
}
