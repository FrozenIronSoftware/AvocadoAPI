package com.frozenironsoftware.avocado.util.api;

import com.frozenironsoftware.avocado.Avocado;
import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.bytes.QueryLimitedOffsetRequest;
import com.frozenironsoftware.avocado.util.ApiCache;
import com.frozenironsoftware.avocado.util.MessageQueue;
import com.frozenironsoftware.avocado.util.QueryUtil;
import com.frozenironsoftware.avocado.util.ResponseUtil;
import com.frozenironsoftware.avocado.util.StringUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class SearchHandler {
    /**
     * Get a podcast that match the search
     * @param request request
     * @param response response
     * @return JSON array
     */
    public static String getSearch(@NotNull Request request, Response response) {
        String query = request.queryParams("q");
        if (query == null || query.isEmpty())
            throw halt(HttpStatus.BAD_REQUEST_400, "Missing query");
        int limit = StringUtil.parseInt(request.queryParamOrDefault("limit",
                String.valueOf(DefaultConfig.ITEM_LIMIT)));
        long offset = StringUtil.parseLong(request.queryParamOrDefault("offset", "0"));
        QueryUtil.checkLimitAndOffset(limit, offset);
        // Queue cache update
        if (QueryUtil.shouldRequestCache(request)) {
            QueryLimitedOffsetRequest queryLimitedOffsetRequest = new QueryLimitedOffsetRequest(query, limit, offset);
            Avocado.queue.cacheRequest(MessageQueue.TYPE.GET_SEARCH, queryLimitedOffsetRequest);
        }
        // Fetch from cache
        String cacheId = ApiCache.createKey("search", query, limit, offset);
        String cachedData = Avocado.cache.get(cacheId);
        if (cachedData == null)
            return ResponseUtil.queuedResponse(response, "[]");
        return cachedData;
    }
}
