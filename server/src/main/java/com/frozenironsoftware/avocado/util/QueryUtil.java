package com.frozenironsoftware.avocado.util;

import org.jetbrains.annotations.NotNull;
import spark.Request;

import java.util.ArrayList;
import java.util.List;

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
}
