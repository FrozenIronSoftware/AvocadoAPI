package com.frozenironsoftware.avocado;

import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.AuthUtil;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.data.Constants;
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
        Logger.info("Starting Avocado %s", Constants.VERSION);
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
        String redisUrlEnv = System.getenv("REDIS_URL_ENV");
        String redisUrlEnvName = redisUrlEnv == null || redisUrlEnv.isEmpty() ? "REDIS_URL" : redisUrlEnv;
        String redisServer = System.getenv(redisUrlEnvName);
        if (redisServer == null || redisServer.isEmpty()) {
            Logger.warn("Missing env: %s", redisUrlEnvName);
            System.exit(1);
        }
        // SQL address
        String sqlUrlEnv = System.getenv("SQL_URL_ENV");
        String sqlUrlEnvName = sqlUrlEnv == null || sqlUrlEnv.isEmpty() ? "SQL_URL" : sqlUrlEnv;
        String sqlServer = System.getenv(sqlUrlEnvName);
        if (sqlServer == null || sqlServer.isEmpty()) {
            Logger.warn("Missing env: %s", sqlUrlEnvName);
            System.exit(1);
        }
        // Redirect
        redirectToHttps = Boolean.parseBoolean(System.getenv().getOrDefault("REDIRECT_HTTP", "true"));
        // Set values
        port(port);
        staticFiles.location("/static/");
        staticFiles.expireTime(604800); // One Week cache
        Avocado.cache = new ApiCache(redisServer);
        DatabaseUtil.setServer(sqlServer);
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
        });
    }
}
