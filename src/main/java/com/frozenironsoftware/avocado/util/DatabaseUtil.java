package com.frozenironsoftware.avocado.util;

import org.jetbrains.annotations.Nullable;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.sql2o.quirks.PostgresQuirks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseUtil {
    private static Sql2o sql2o;
    private static int connections;
    private static int MAX_CONNECTIONS =
            (int) StringUtil.parseLong(System.getenv().getOrDefault("SQL_CONNECTIONS", "5"));
    public static String schema;

    /**
     * Get new sql instance with a parsed connection url
     * Use sql2o instance instead of calling this multiple times
     * @param serverUrl connection url
     * @return new instance
     */
    private static Sql2o getSql2oInstance(String serverUrl) {
        Pattern mysqlPattern = Pattern.compile("(.*://)(.*):(.*)@(.*)"); // (scheme)(user):(pass)@(url)
        Matcher mysqlMatches = mysqlPattern.matcher(serverUrl);
        if (!mysqlMatches.find()) {
            Logger.warn("Could not parse mysql database connection string.");
            System.exit(1);
        }
        String mysqlUrl = mysqlMatches.group(1).replace("postgres", "postgresql") +
                mysqlMatches.group(4);
        if (Boolean.parseBoolean(System.getenv().getOrDefault("SQL_SSL", "true")))
            mysqlUrl += "?sslmode=require";
        String mysqlUsername = mysqlMatches.group(2);
        String mysqlPassword = mysqlMatches.group(3);
        schema = System.getenv().getOrDefault("SQL_SCHEMA", "avocado");
        return new Sql2o(mysqlUrl, mysqlUsername, mysqlPassword, new PostgresQuirks());
    }

    /**
     * Set the server url an initialize a new sql2o instance
     * @param serverUrl url of sql server
     */
    public static void setServer(String serverUrl) {
        sql2o = getSql2oInstance(serverUrl);
    }

    /**
     * Release a connection
     * @param connection sql2o connection
     */
    public static void releaseConnection(@Nullable Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (Sql2oException e) {
                Logger.exception(e);
            }
        }
        if (connections > 0)
            connections--;
    }

    /**
     * Try to fetch a connection
     * @return sql2o connection
     */
    private static Connection getConnection() {
        while (connections >= MAX_CONNECTIONS)
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {}
        Connection connection = sql2o.open();
        connections++;
        return connection;
    }

    /**
     * Try to fetch a transaction
     * @return sql2o connection
     */
    public static Connection getTransaction() {
        while (connections >= MAX_CONNECTIONS)
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {}
        Connection connection = sql2o.beginTransaction();
        connections++;
        return connection;
    }
}
