package com.frozenironsoftware.avocado;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.util.List;

class AvocadoServer {
    /**
     * Handle global page rules
     * @param request request
     * @param response response
     */
    static void handleGlobalPageRules(Request request, Response response) {
        // Root to WWW
        if (request.host() != null) {
            String[] colonSplit = request.host().split(":");
            if (colonSplit.length > 0) {
                String[] host = colonSplit[0].split("\\.");
                // Exactly 2 for TLD
                if (host.length == 2) {
                    String port = null;
                    if (colonSplit.length > 1)
                        port = colonSplit[1];
                    List<String> hostArray = Lists.newArrayList(host);
                    hostArray.add(0, "www");
                    response.redirect(constructUrl(request.scheme(), hostArray.toArray(new String[0]), port,
                            request.pathInfo(), request.queryString()));
                    return;
                }
            }
        }
        // HTTP to HTTPS
        if (request.scheme().equalsIgnoreCase("http") && Avocado.redirectToHttps) {
            String redirect = request.url() + (request.queryString() != null ? "?" + request.queryString() : "");
            response.redirect(redirect.replace("http://", "https://"));
            return;
        }
        // Redirect paths with a trailing slash
        if (request.pathInfo().endsWith("/") && !request.pathInfo().equals("/"))
            response.redirect(request.pathInfo().substring(0, request.pathInfo().length() - 1));
    }

    /**
     * Construct a url from its pars
     * @param scheme url scheme e.g https
     * @param host array of host parts not including dots, colons or ports
     * @param port Port to use
     * @param path path beginning with a forward slash
     * @param queryString query string excluding a question mark
     */
    private static String constructUrl(String scheme, String[] host, @Nullable String port,
                                       @Nullable String path, @Nullable String queryString) {
        StringBuilder hostString = new StringBuilder();
        for (String hostPart : host)
            hostString.append(hostPart).append(".");
        hostString.deleteCharAt(hostString.lastIndexOf("."));
        String portString = port != null ? ":" + port : "";
        String queryAppend = queryString != null ? "?" + queryString : "";
        String pathString = path != null ? path : "";
        return String.format("%s://%s%s%s%s", scheme, hostString.toString(), portString, pathString, queryAppend);
    }
}
