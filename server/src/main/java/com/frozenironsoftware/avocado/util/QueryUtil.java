package com.frozenironsoftware.avocado.util;

import com.frozenironsoftware.avocado.data.DefaultConfig;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import spark.HaltException;
import spark.Request;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.halt;

public class QueryUtil {
    /**
     * Get all values for a query key
     * @param request request
     * @param key query key
     * @return list of values for a query key. The list may be empty if there was no key found. Emty values will be
     * empty strings and not null.
     */
    @NotNull
    public static List<String> extractQueryToList(Request request, String key) {
        List<String> values = new ArrayList<>();
        String[] valuesArray = request.queryParamsValues(key);
        if (valuesArray == null)
            return values;
        for (String value : valuesArray) {
            if (value == null)
                values.add("");
            else
                values.add(value);
        }
        return values;
    }

    /**
     * Ensure the limit and offset are within the limits
     * @param limit limit
     * @param offset offset
     * @throws HaltException 404 bad request if arguments are not within bounds
     */
    public static void checkLimitAndOffset(int limit, long offset) throws HaltException {
        if (limit < 1 || limit > DefaultConfig.ITEM_LIMIT)
            throw halt(HttpStatus.BAD_REQUEST_400, "{\"error\": \"Limit out of bounds\"}");
        if (offset < 0 || offset > DefaultConfig.MAX_OFFSET)
            throw halt(HttpStatus.BAD_REQUEST_400, "{\"error\": \"Offset out of bounds\"}");
    }

    /**
     * Check if the request should request a cache from the cacher
     * @param request request
     * @return true if the request does not specify a cache request
     */
    public static boolean shouldRequestCache(@NotNull Request request) {
        return !Boolean.parseBoolean(request.queryParams("waiting"));
    }
}
