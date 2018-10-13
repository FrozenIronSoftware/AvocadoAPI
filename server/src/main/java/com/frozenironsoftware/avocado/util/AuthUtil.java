/*
 * Copyright (c) 2017 Rolando Islas. All Rights Reserved
 *
 */

package com.frozenironsoftware.avocado.util;

import com.frozenironsoftware.avocado.data.AppToken;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;
import spark.Request;
import spark.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.halt;


public class AuthUtil {
    public static final int BCRYPT_ROUNDS = 15;
    private static List<String> ALLOWED_IDS;
    private static boolean authenticate;

    static {
        String[] allowedIds = System.getenv().getOrDefault("ALLOWED_CLIENT_ID", "").split(",");
        ALLOWED_IDS = new ArrayList<>(Arrays.asList(allowedIds));
        authenticate = true;
    }

    /**
     * Check headers for a client ID and match it against the allowed ID
     * @param request request to check
     * @return has valid header/ID
     */
    public static boolean verifyClientId(Request request) {
        if (!shouldAuthenticate())
            return true;
        if (ALLOWED_IDS.isEmpty()) {
            Logger.warn("Missing environment variable: ALLOWED_CLIENT_ID");
            return false;
        }
        String header = request.headers("Client-ID");
        if (header == null || header.isEmpty() || !ALLOWED_IDS.contains(header)) {
            Logger.warn(
                    String.format(
                            "Received an unauthenticated request:\n\tIP %s\n\tUser Agent: %s\n\tID: %s",
                            request.ip(),
                            request.userAgent(),
                            header == null ? "" : header
                    ));
            return false;
        }
        return true;
    }

    /**
     * Set if the auth util should authenticate requests
     * @param authenticate do auth?
     */
    public static void setAuthenticate(boolean authenticate) {
        if (!authenticate)
            Logger.warn("Requests will not be authenticated!");
        AuthUtil.authenticate = authenticate;
    }

    /**
     * Hash a string with SHA1 and the specified salt
     * @param raw raw string
     * @param salt salt - the global salt will be used if this is null
     * @return hashed string
     */
    public static String hashString(@Nullable String raw, @Nullable String salt) {
        if (raw == null)
            raw = "";
        if (salt == null)
            salt = System.getenv().getOrDefault("SALT", "");
        String hash = Hashing.sha1().hashString(raw, Charsets.UTF_8).toString();
        hash = Hashing.sha1().hashString(hash + salt, Charsets.UTF_8).toString();
        return hash;
    }

    /**
     * Return whether or not authentication checks should be performed
     * @return do authenticate
     */
    public static boolean shouldAuthenticate() {
        return AuthUtil.authenticate;
    }

    /**
     * Check a basic authentication header against user password and app tokens
     * @param request request
     * @return user id
     */
    public static long checkAuth(Request request) {
        if (!verifyClientId(request))
            throw halt(HttpStatus.UNAUTHORIZED_401);
        // Check if a session exists
        Session session = request.session(false);
        if (session != null && session.attribute("user_id") != null)
            return StringUtil.parseLong(session.attribute("user_id"));
        // Check auth with Authorization header
        String authorizationHeader = request.headers("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic"))
            throw halt(HttpStatus.BAD_REQUEST_400);
        String usernamePassword = new String(BaseEncoding.base64().decode(
                authorizationHeader.replace("Basic", "").trim()), Charsets.UTF_8);
        String[] usernamePasswordSplit = usernamePassword.split(":");
        if (usernamePasswordSplit.length != 2)
            throw halt(HttpStatus.BAD_REQUEST_400);
        String username = usernamePasswordSplit[0];
        String password = usernamePasswordSplit[1];
        if (!(checkPassword(username, password) || checkAppToken(username, password)))
            throw halt(HttpStatus.UNAUTHORIZED_401);
        // Create a session
        session = request.session(true);
        session.attribute("user_id", username);
        return StringUtil.parseLong(username);
    }

    /**
     * Check app token for users
     * @param userId user id
     * @param password plain app token
     * @return did the app token match any in the database for the given user id
     */
    private static boolean checkAppToken(String userId, String password) {
        String sql = "select * from %s.tokens where user_id = :user_id;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            List<AppToken> appTokens = connection.createQuery(sql)
                    .addParameter("user_id", StringUtil.parseLong(userId))
                    .addColumnMapping("user_id", "userId")
                    .executeAndFetch(AppToken.class);
            DatabaseUtil.releaseConnection(connection);
            for (AppToken appToken : appTokens) {
                if (appToken.token != null && BCrypt.checkpw(Hashing.sha256().hashString(password).toString(), appToken.token))
                    return true;
            }
            return false;
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return false;
        }
    }

    /**
     * Check password against the stored hash
     * @param userId user id
     * @param password password
     * @return if the password matched the stored hash
     */
    private static boolean checkPassword(String userId, String password) {
        return false;
    }
}
