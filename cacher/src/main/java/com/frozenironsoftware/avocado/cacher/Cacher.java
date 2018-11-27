package com.frozenironsoftware.avocado.cacher;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.data.Constants;
import com.frozenironsoftware.avocado.cacher.util.MessageConsumer;
import com.frozenironsoftware.avocado.cacher.util.UpdateRequester;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;

import java.util.logging.Level;

public class Cacher {
    public static ApiCache cache = null;
    public static MessageQueue queue = null;

    public static void main(String[] args) {
        // Log level
        String logLevel = System.getenv().getOrDefault("LOG_LEVEL", "INFO");
        Logger.setLevel(Level.parse(logLevel));
        Logger.info("Starting %s %s", Constants.NAME, Constants.VERSION);
        // Redis address
        String redisServer = Avocado.getRedisServer();
        cache = new ApiCache(redisServer);
        // SQL address
        String sqlServer = Avocado.getSqlServer();
        DatabaseUtil.setServer(sqlServer);
        // MQ address
        String mqServer = Avocado.getMqServer();
        // MQ routing key
        String routingKey = MessageQueue.ROUTING_KEY_API_CACHER;
        if (args.length < 1)
            Logger.info("Running an an API cache worker");
        else {
            if (args[0].equalsIgnoreCase("--podcast") || args[0].equalsIgnoreCase("-p")) {
                Logger.info("Running as a podcast cache worker");
                routingKey = MessageQueue.ROUTING_KEY_POD_CACHER;
            }
            else if (args[0].equalsIgnoreCase("--update") || args[0].equalsIgnoreCase("-u")) {
                Logger.info("Running as a podcast update requester worker");
                startUpdateRequester(mqServer);
                return;
            }
        }
        // Start
        startMessageConsumer(mqServer, routingKey);
    }

    /**
     * Start the update requester
     * @param mqServer MQ server URL
     */
    private static void startUpdateRequester(String mqServer) {
        MessageQueue messageQueue = MessageQueue.createRetry(mqServer);
        queue = messageQueue;
        UpdateRequester updateRequester = new UpdateRequester(messageQueue);
        updateRequester.checkDatabaseAndRequestOutdated();
        messageQueue.close();
    }

    /**
     * Start the message consumer loop
     * @param mqServer MQ server url
     * @param routingKey MQ routing key
     */
    private static void startMessageConsumer(String mqServer, String routingKey) {
        MessageQueue messageQueue = MessageQueue.createRetry(mqServer, routingKey);
        queue = messageQueue;
        MessageConsumer messageConsumer = new MessageConsumer(messageQueue);
    }
}
