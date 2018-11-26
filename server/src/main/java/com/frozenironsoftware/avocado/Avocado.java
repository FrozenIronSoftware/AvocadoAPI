package com.frozenironsoftware.avocado;

import com.frozenironsoftware.avocado.data.Constants;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.AuthUtil;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.frozenironsoftware.avocado.util.api.PodcastHandler;
import com.frozenironsoftware.avocado.util.api.SearchHandler;
import com.frozenironsoftware.avocado.util.api.UserHandler;
import com.google.gson.Gson;

import java.util.logging.Level;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class Avocado {
    public static ApiCache cache;
    public static MessageQueue queue;
    static boolean redirectToHttps;
    public static Gson gson = new Gson();

    public static void main(String[] args) {
        // Parse args
        String logLevel = System.getenv().getOrDefault("LOG_LEVEL", "INFO");
        // No auth check
        if (System.getenv().getOrDefault("NO_AUTH_CLIENT_ID", "false").equalsIgnoreCase("true"))
            AuthUtil.setAuthenticate(false);
        // Init logger
        Logger.setLevel(Level.parse(logLevel));
        Logger.info("Starting %s %s", Constants.NAME, Constants.VERSION);
        // Parse port
        int port = 5000;
        String portString = System.getenv("PORT");
        try {
            if (portString != null && !portString.isEmpty())
                port = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) {
            Logger.warn("Failed to parse PORT env var: %s", portString);
        }
        // Redis address
        String redisServer = getRedisServer();
        // SQL address
        String sqlServer = getSqlServer();
        // MQ address
        String mqServer = getMqServer();
        // Redirect
        redirectToHttps = Boolean.parseBoolean(System.getenv().getOrDefault("REDIRECT_HTTP", "true"));
        // Set values
        port(port);
        staticFiles.location("/static/");
        staticFiles.expireTime(604800); // One Week cache
        Avocado.cache = new ApiCache(redisServer);
        DatabaseUtil.setServer(sqlServer);
        Avocado.queue = new MessageQueue(mqServer);
        // Global page rules
        before(AvocadoServer::handleGlobalPageRules);
        // API
        path("/api", () -> {
            before("/*", (request, response) -> response.type("application/json"));
            // User
            path("/user", () -> {
                get("", UserHandler::getInfo);
                get("/create", UserHandler::createAnonymousUser);
            });
            // Podcasts
            path("/podcasts", () -> {
                get("", PodcastHandler::getPodcasts);
                get("/favorites", PodcastHandler::getFavorites);
                get("/recents", PodcastHandler::getRecent);
                get("/popular", PodcastHandler::getPopular);
                get("/episodes", PodcastHandler::getEpisodes);
            });
            // Search
            get("/search", SearchHandler::getSearch);
        });
    }

    /**
     * Fetch the sql server url
     * @return url string
     */
    public static String getSqlServer() {
        String sqlUrlEnv = System.getenv("SQL_URL_ENV");
        String sqlUrlEnvName = sqlUrlEnv == null || sqlUrlEnv.isEmpty() ? "SQL_URL" : sqlUrlEnv;
        String sqlServer = System.getenv(sqlUrlEnvName);
        if (sqlServer == null || sqlServer.isEmpty()) {
            Logger.warn("Missing env: %s", sqlUrlEnvName);
            System.exit(1);
        }
        return sqlServer;
    }

    /**
     * Fetch the redis server url
     * @return url string
     */
    public static String getRedisServer() {
        String redisUrlEnv = System.getenv("REDIS_URL_ENV");
        String redisUrlEnvName = redisUrlEnv == null || redisUrlEnv.isEmpty() ? "REDIS_URL" : redisUrlEnv;
        String redisServer = System.getenv(redisUrlEnvName);
        if (redisServer == null || redisServer.isEmpty()) {
            Logger.warn("Missing env: %s", redisUrlEnvName);
            System.exit(1);
        }
        return redisServer;
    }

    /**
     * Fetch the MQ server URL
     * @return MQ server URL
     */
    public static String getMqServer() {
        String mqUrlEnv = System.getenv("MQ_URL_ENV");
        String mqUrlEnvName = mqUrlEnv == null || mqUrlEnv.isEmpty() ? "MQ_URL" : mqUrlEnv;
        String mqServer = System.getenv(mqUrlEnvName);
        if (mqServer == null || mqServer.isEmpty()) {
            Logger.warn("Missing env: %s", mqUrlEnvName);
            System.exit(1);
        }
        if (Boolean.parseBoolean(System.getenv().getOrDefault("MQ_SECURE", "true")))
            mqServer = mqServer.replace("amqp://", "amqps://");
        else
            Logger.warn("Connecting to message queue without SSL");
        return mqServer;
    }
}
