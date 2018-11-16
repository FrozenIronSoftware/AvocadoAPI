package com.frozenironsoftware.avocado.cacher;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.data.Constants;
import com.frozenironsoftware.avocado.cacher.util.MessageConsumer;
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
        }
        // Start
        MessageQueue messageQueue = new MessageQueue(mqServer, routingKey);
        while (messageQueue.getChannel() == null) {
            Logger.warn("Failed to connect to message queue. Retrying in 5 seconds");
            messageQueue.connect();
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException ignore) {}
        }
        queue = messageQueue;
        MessageConsumer messageConsumer = new MessageConsumer(messageQueue);
    }
}
