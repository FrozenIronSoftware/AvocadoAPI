package com.frozenironsoftware.avocado.util.api;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.AuthUtil;
import com.frozenironsoftware.avocado.util.Logger;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.frozenironsoftware.avocado.util.QueryUtil;
import com.frozenironsoftware.avocado.util.StringUtil;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.http.HttpStatus;
import spark.HaltException;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        checkLimitAndOffset(limit, offset);
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
        checkLimitAndOffset(limit, offset);
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
        checkLimitAndOffset(limit, offset);
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

    /**
     * Ensure the limit and offset are within the limits
     * @param limit limit
     * @param offset offset
     * @throws HaltException 404 bad request if arguments are not within bounds
     */
    private static void checkLimitAndOffset(int limit, long offset) throws HaltException {
        if (limit < 1 || limit > DefaultConfig.ITEM_LIMIT)
            throw halt(HttpStatus.BAD_REQUEST_400, "{\"error\": \"Limit out of bounds\"}");
        if (offset < 0 || offset > DefaultConfig.MAX_OFFSET)
            throw halt(HttpStatus.BAD_REQUEST_400, "{\"error\": \"Offset out of bounds\"}");
    }

    /**
     * Get podcast data for specifed podcast ids
     * @param request request
     * @param response response
     * @return podcast JSON array
     */
    public static String getPodcasts(Request request, Response response) {
        List<String> podcastIds = QueryUtil.extractQueryToList(request, "id");
        if (podcastIds.size() < 1 || podcastIds.size() > DefaultConfig.ITEM_LIMIT) {
            response.type("text/html");
            throw halt(HttpStatus.BAD_REQUEST_400, "ID count out of range");
        }
        // Queue cache update
        StringArrayRequest stringArrayRequest = new StringArrayRequest(podcastIds);
        Avocado.queue.cacheRequest(MessageQueue.TYPE.GET_PODCASTS, stringArrayRequest);
        // Fetch from cache
        Map<String, String> podcastsJson = Avocado.cache.mgetWithPrefix(ApiCache.PREFIX_PODCAST, podcastIds);
        List<Podcast> podcasts = new ArrayList<>();
        for (String podcastJson : podcastsJson.values()) {
            if (podcastJson == null)
                continue;
            try {
                Podcast podcast = Avocado.gson.fromJson(podcastJson, Podcast.class);
                podcasts.add(podcast);
            }
            catch (JsonSyntaxException e) {
                Logger.exception(e);
            }
        }
        return Avocado.gson.toJson(podcasts);
    }
}
