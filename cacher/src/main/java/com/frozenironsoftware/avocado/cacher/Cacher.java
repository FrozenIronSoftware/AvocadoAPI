package com.frozenironsoftware.avocado.cacher;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.cacher.util.MessageConsumer;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.frozenironsoftware.avocado.cacher.data.Constants;

import java.util.logging.Level;

public class Cacher {
    public static ApiCache cache = null;

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
        MessageConsumer messageConsumer = new MessageConsumer(new MessageQueue(mqServer));
    }
}
