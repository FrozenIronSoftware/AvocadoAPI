package com.frozenironsoftware.avocado.util;

import com.frozenironsoftware.avocado.data.model.bytes.ByteSerializable;
import com.heroku.sdk.EnvKeyStore;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ShutdownListener;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

public class MessageQueue {

    private static final String EXCHANGE_NAME = "worker";
    private static final String QUEUE_NAME = "request";
    public static final String ROUTING_KEY_API_CACHER = "route-api-worker";
    public static final String ROUTING_KEY_POD_CACHER = "route-pod-worker";
    private final String connectionUrl;
    @Nullable private final String routingKey;
    @Nullable private Channel channel;

    /**
     * Create a new message queue
     * @param url connection url
     * @param routingKey if this is not null, the queue will be bound with the routing key
     */
    public MessageQueue(String url, @Nullable String routingKey) {
        this.connectionUrl = url;
        this.routingKey = routingKey;
        connect();
    }

    public MessageQueue(String url) {
        this(url, null);
    }

    /**
     * (Re)connect to the message queue server
     * This can be called if already connected.
     * @return True if a connect has been established or has aleady been established
     */
    public boolean connect() {
        if (channel != null && channel.isOpen())
            return true;
        ConnectionFactory factory = new ConnectionFactory();
        try {
            if (Boolean.parseBoolean(System.getenv().getOrDefault("MQ_SECURE", "true")))
                factory.useSslProtocol(getSslContext());
            factory.setUri(connectionUrl);
        }
        catch (NoSuchAlgorithmException | KeyManagementException | URISyntaxException | CertificateException |
                KeyStoreException | IOException e) {
            Logger.exception(e);
            Logger.warn("Failed to create connection factory");
            channel = null;
            return false;
        }
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            if (routingKey != null) {
                Logger.debug("Binding to routing key: %s", routingKey);
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, routingKey);
            }
            return true;
        }
        catch (IOException | TimeoutException e) {
            Logger.exception(e);
            Logger.warn("Failed to connect to message queue");
            channel = null;
            return false;
        }
    }

    /**
     * Create an SSL context with the env var trusted cert
     * @return SSL context that trusts the env var
     */
    private SSLContext getSslContext() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException, KeyManagementException {
        String cert = System.getenv().getOrDefault("MQ_TRUST", "")
                .replace("\\n", "\n");
        EnvKeyStore keyStore = EnvKeyStore.createFromPEMStrings(cert,
                new BigInteger(130, new SecureRandom()).toString(32));
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init(keyStore.keyStore());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    /**
     * Add a consumer to the default exchange and channel
     * @param messageConsumer consumer
     */
    public void addConsumer(Consumer messageConsumer) {
        if (!connect())
            return;
        try {
            if (channel != null)
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
    @Nullable
    public Channel getChannel() {
        return channel;
    }

    /**
     * Cache a request of a specified type
     * @param type type of cache request
     * @param data additional parameters for the request
     * @param routingKey key to use for routing
     */
    public void cacheRequest(TYPE type, ByteSerializable data, String routingKey) {
        if (!connect())
            return;
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(type.name());
        try {
            if (getChannel() != null)
                getChannel().basicPublish(EXCHANGE_NAME, routingKey, builder.build(), data.toBytes());
        }
        catch (IOException e) {
            Logger.exception(e);
        }
    }

    /**
     * Cache a request of a specified type
     * Uses default routing key
     * @param type type of cache request
     * @param data additional parameters for the request
     */
    public void cacheRequest(TYPE type, ByteSerializable data) {
        cacheRequest(type, data, ROUTING_KEY_API_CACHER);
    }

    /**
     * Add a shutdown listener to the channel
     * @param shutdownListener listener
     */
    public void addShutdownListener(ShutdownListener shutdownListener) {
        if (!connect())
            return;
        if (channel != null)
            channel.addShutdownListener(shutdownListener);
    }

    @Nullable
    public String getRoutingKey() {
        return routingKey;
    }

    public enum TYPE {
        GET_FAVORITE_PODCASTS, GET_RECENT_PODCASTS, GET_POPULAR_PODCASTS, GET_EPISODES, GET_PODCASTS, FETCH_PODCASTS,
        GET_SEARCH
    }
}
