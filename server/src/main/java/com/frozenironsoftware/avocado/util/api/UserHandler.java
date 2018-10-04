package com.frozenironsoftware.avocado.util.api;

import com.frozenironsoftware.avocado.data.AppToken;
import com.frozenironsoftware.avocado.data.User;
import com.frozenironsoftware.avocado.util.AuthUtil;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.StringUtil;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;
import spark.Request;
import spark.Response;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.halt;

public class UserHandler {

    /**
     * Handle a request to create a new anonymous user
     * @param request request
     * @param response response
     * @return user data JSON
     */
    public static String createAnonymousUser(Request request, Response response) {
        if (!AuthUtil.verifyClientId(request))
            throw halt(HttpStatus.UNAUTHORIZED_401);
        User user = createUserInDatabase();
        if (user == null) {
            throw halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        else {
            AppToken appToken = createAppToken(user);
            if (appToken == null)
                throw halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
            JsonObject ret = new JsonObject();
            ret.addProperty("token", appToken.tokenPlain);
            ret.addProperty("account_level", user.accountLevel.ordinal());
            ret.addProperty("email", user.email);
            ret.addProperty("id", user.id);
            return ret.toString();
        }
    }

    /**
     * Generate a save an app token for a user
     * This will be the only time an AppToken will contain the `tokenPlain` field populated
     * @param user user for which to generate app token
     * @return token or null on error
     */
    @Nullable
    private static AppToken createAppToken(User user) {
        String sql = "insert into %s.tokens (user_id, token, name) values (:user_id, :token, :name)";
        sql = String.format(sql, DatabaseUtil.schema);
        // Generate a new token
        String usableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String token = new SecureRandom().ints(32, 0, usableChars.length())
                .mapToObj(i -> "" + usableChars.charAt(i))
                .collect(Collectors.joining());
        String hash = BCrypt.hashpw(Hashing.sha256().hashString(token).toString(),
                BCrypt.gensalt(AuthUtil.BCRYPT_ROUNDS));
        // Query
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            long id = connection.createQuery(sql, true)
                    .addParameter("user_id", user.id)
                    .addParameter("token", hash)
                    .addParameter("name", "")
                    .executeUpdate()
                    .getKey(Long.class);
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            AppToken appToken = new AppToken(id, user.id, hash, "");
            appToken.tokenPlain = token;
            return appToken;
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Create a new anonymous user in the database
     * @return null on error
     */
    @Nullable
    private static User createUserInDatabase() {
        String sqlinsert = "insert into %s.users (account_level) values (:account_level);";
        sqlinsert = String.format(sqlinsert, DatabaseUtil.schema);
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            long id = connection.createQuery(sqlinsert, true)
                    .addParameter("account_level", User.AccountLevel.ANONYMOUS.ordinal())
                    .executeUpdate()
                    .getKey(Long.class);
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            return new User(id, User.AccountLevel.ANONYMOUS, "");
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }

    /**
     * Get user information from database
     * @param request request
     * @param response response
     * @return user info json. Always returns status 200 with the error field.
     */
    public static String getInfo(Request request, Response response) {
        long userId = AuthUtil.checkAuth(request);
        String sql = "select * from %s.users where id = :id;";
        sql = String.format(sql, DatabaseUtil.schema);
        Connection connection = null;
        JsonObject ret = new JsonObject();
        try {
            connection = DatabaseUtil.getTransaction();
            List<User> users = connection.createQuery(sql)
                    .addParameter("id", userId)
                    .addColumnMapping("account_level", "accountLevel")
                    .executeAndFetch(User.class);
            DatabaseUtil.releaseConnection(connection);
            if (users.size() != 1)
                throw halt(HttpStatus.NOT_FOUND_404);
            User user = users.get(0);
            ret.addProperty("error", false);
            ret.addProperty("account_level", user.accountLevel.ordinal());
            ret.addProperty("email", user.email);
            ret.addProperty("id", user.id);
            return ret.toString();
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            ret.addProperty("error", true);
            return ret.toString();
        }
    }
}
