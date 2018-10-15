package com.frozenironsoftware.avocado.util;

import com.frozenironsoftware.avocado.data.model.bytes.ByteSerializable;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class MessageQueue {

    private static final String EXCHANGE_NAME = "worker";
    private static final String QUEUE_NAME = "request";
    private static final String ROUTING_KEY = "route-request-worker";
    private Channel channel;

    public MessageQueue(String url) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(url);
        }
        catch (NoSuchAlgorithmException | KeyManagementException | URISyntaxException e) {
            Logger.exception(e);
            Logger.warn("Failed to create connection factory");
            System.exit(1);
        }
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        }
        catch (IOException | TimeoutException e) {
            Logger.exception(e);
            Logger.warn("Failed to connect to message queue");
            System.exit(1);
        }
    }

    /**
     * Add a consumer to the default exchange and channel
     * @param messageConsumer consumer
     */
    public void addConsumer(Consumer messageConsumer) {
        try {
            channel.basicConsume(QUEUE_NAME, messageConsumer);
        }
        catch (IOException e) {
            Logger.exception(e);
        }
    }

    /**
     * Get the default channel that has had the default queue and exchange declared and queue bound
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Cache a request of a specified type with only a user id needed
     * @param type type of cache request
     * @param data additional parameters for the request
     */
    public void cacheRequest(TYPE type, ByteSerializable data) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(type.name());
        try {
            getChannel().basicPublish(EXCHANGE_NAME, ROUTING_KEY, builder.build(), data.toBytes());
        }
        catch (IOException e) {
            Logger.exception(e);
        }
    }

    public enum TYPE {
        GET_FAVORITE_PODCASTS, GET_RECENT_PODCASTS, GET_POPULAR_PODCASTS, GET_PODCASTS
    }
}
