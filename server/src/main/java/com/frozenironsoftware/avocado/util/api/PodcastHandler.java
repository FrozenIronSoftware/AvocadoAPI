package com.frozenironsoftware.avocado.util.api;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.AuthUtil;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.frozenironsoftware.avocado.util.StringUtil;
import com.rabbitmq.client.DefaultConsumer;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class PodcastHandler {

    /**
     * Get a JSON array of podcasts favorited a user
     * @param request should contain a client id and user auth details
     * @param response response
     * @return JSON array
     */
    public static String getFavorites(Request request, Response response) {
        long userId = AuthUtil.checkAuth(request);
        int limit = StringUtil.parseInt(request.queryParamOrDefault("limit",
                String.valueOf(DefaultConfig.ITEM_LIMIT)));
        long offset = StringUtil.parseLong(request.queryParamOrDefault("offset", "0"));
        // Queue cache update
        UserIdLimitedOffsetRequest userIdLimitedOffsetRequest = new UserIdLimitedOffsetRequest(userId, limit, offset);
        Avocado.queue.cacheRequest(MessageQueue.TYPE.GET_FAVORITE_PODCASTS, userIdLimitedOffsetRequest);
        // Fetch from cache
        String cacheId = ApiCache.createKey("podcasts/favorites", userId, limit, offset);
        String cachedData = Avocado.cache.get(cacheId);
        if (cachedData == null)
            return "[]";
        return cachedData;
    }

    /**
     * Get a JSON array of podcasts that the user has recently viewed
     * @param request authenticated request
     * @param response response
     * @return array
     */
    public static String getRecent(Request request, Response response) {
        long userId = AuthUtil.checkAuth(request);
        int limit = StringUtil.parseInt(request.queryParamOrDefault("limit",
                String.valueOf(DefaultConfig.ITEM_LIMIT)));
        long offset = StringUtil.parseLong(request.queryParamOrDefault("offset", "0"));
        // Queue cache update
        UserIdLimitedOffsetRequest userIdLimitedOffsetRequest = new UserIdLimitedOffsetRequest(userId, limit, offset);
        Avocado.queue.cacheRequest(MessageQueue.TYPE.GET_RECENT_PODCASTS, userIdLimitedOffsetRequest);
        // Fetch from cache
        String cacheId = ApiCache.createKey("podcasts/recents", userId, limit, offset);
        String cachedData = Avocado.cache.get(cacheId);
        if (cachedData == null)
            return "[]";
        return cachedData;
    }

    /**
     * Get popular podcasts
     * @param request request
     * @param response response
     * @return JSON array
     */
    public static String getPopular(Request request, Response response) {
        if (!AuthUtil.verifyClientId(request))
            throw halt(HttpStatus.UNAUTHORIZED_401);
        int limit = StringUtil.parseInt(request.queryParamOrDefault("limit",
                String.valueOf(DefaultConfig.ITEM_LIMIT)));
        long offset = StringUtil.parseLong(request.queryParamOrDefault("offset", "0"));
        // Queue cache update
        LimitedOffsetRequest limitedOffsetRequest = new LimitedOffsetRequest(limit, offset);
        Avocado.queue.cacheRequest(MessageQueue.TYPE.GET_POPULAR_PODCASTS, limitedOffsetRequest);
        // Fetch from cache
        String cacheId = ApiCache.createKey("podcasts/popular", limit, offset);
        String cachedData = Avocado.cache.get(cacheId);
        if (cachedData == null)
            return "[]";
        return cachedData;
    }
}
