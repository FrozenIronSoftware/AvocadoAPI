package com.frozenironsoftware.avocado.util;

import com.frozenironsoftware.avocado.data.DefaultConfig;
import spark.Response;

public class ResponseUtil {

    /**
     * Set the HTTP status code to a number indicating that the result is empty because a cache has been queued
     * @param response http response object
     * @param responseString response body
     * @return the response string is returned
     */
    public static String queuedResponse(Response response, String responseString) {
        response.status(DefaultConfig.HTTP_STATUS_QUEUED_RESPONSE);
        response.body(responseString);
        return responseString;
    }
}
