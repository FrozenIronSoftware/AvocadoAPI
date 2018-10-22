package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.data.model.bytes.EpisodesRequest;
import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class MessageConsumer extends DefaultConsumer {
    private final MessageQueue queue;

    public MessageConsumer(MessageQueue messageQueue) {
        super(messageQueue.getChannel());
        this.queue = messageQueue;
        queue.addConsumer(this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        if (properties.getContentType() != null && body != null) {
            try {
                MessageQueue.TYPE type = MessageQueue.TYPE.valueOf(properties.getContentType());
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
                    default:
                        Logger.warn("Unhandled message: %s", type.name());
                }
            }
            catch (Exception e) {
                Logger.exception(e);
            }
        }
        queue.getChannel().basicAck(envelope.getDeliveryTag(), false);
    }
}
