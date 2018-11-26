/*
 * Copyright (c) 2017 Rolando Islas. All Rights Reserved
 *
 */

package com.frozenironsoftware.avocado.util;

import com.google.gson.Gson;
import com.heroku.sdk.EnvKeyStore;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.util.JedisURIHelper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiCache {
    public static final int TIMEOUT_HOUR = 60 * 60;
    public static final int TIMEOUT_DAY = 24 * 60 * 60; // 1 Day
    private static final int TIMEOUT = TIMEOUT_HOUR * 12; // Seconds before a cache value should be considered invalid
    public static final String PREFIX_PODCAST = "_p_";
    public static final String KEY_QUERY_HISTORY = "_qh_";
    private final String redisPassword;
    private final Gson gson;
    private JedisPool redisPool;

    public ApiCache(String redisServer) {
        String connectionLimit = System.getenv("REDIS_CONNECTIONS");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setBlockWhenExhausted(true);
        if (connectionLimit != null && !connectionLimit.isEmpty())
            poolConfig.setMaxTotal((int) StringUtil.parseLong(connectionLimit));
        else
            poolConfig.setMaxTotal(1);
        // Create the pool
        boolean useSsl = Boolean.parseBoolean(System.getenv().getOrDefault("REDIS_SECURE", "true"));
        if (useSsl)
            redisServer = redisServer.replace("redis://", "rediss://");
        URI uri = URI.create(redisServer);
        if (JedisURIHelper.isValid(uri)) {
            String host = uri.getHost();
            int port = uri.getPort();
            redisPassword = JedisURIHelper.getPassword(uri);
            if (!useSsl) {
                Logger.warn("Connecting to Redis without SSL");
                redisPool = new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, redisPassword,
                        Protocol.DEFAULT_DATABASE, null);
            }
            else {
                try {
                    SSLSocketFactory sslSocketFactory = getSocketFactory();
                    SSLParameters sslParameters = new SSLParameters();
                    redisPool = new JedisPool(poolConfig, uri, sslSocketFactory, sslParameters, null);
                }
                catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException |
                        KeyManagementException e) {
                    Logger.exception(e);
                    redisPool = new JedisPool();
                }
            }
        }
        else {
            redisPool = new JedisPool();
            redisPassword = "";
        }
        gson = new Gson();
    }

    /**
     * Create a socket factory with the env var trusted cert
     * @return socket factory that trusts the env var
     */
    private SSLSocketFactory getSocketFactory() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, KeyManagementException {
        String cert = System.getenv().getOrDefault("REDIS_TRUST", "")
                .replace("\\n", "\n");
        EnvKeyStore keyStore = EnvKeyStore.createFromPEMStrings(cert,
                new BigInteger(130, new SecureRandom()).toString(32));
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init(keyStore.keyStore());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }

    /**
     * Get a value from redis
     * @param key key to get
     * @return value of key
     */
    @Nullable
    public String get(String key) {
        String value = null;
        try (Jedis redis = getAuthenticatedJedis()) {
            value = redis.get(key);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return value;
    }

    public Jedis getAuthenticatedJedis() {
        Jedis jedis = redisPool.getResource();
        if (!redisPassword.isEmpty())
        jedis.auth(redisPassword);
        if (!jedis.isConnected())
            jedis.connect();
        return jedis;
    }

    /**
     * Create a key for an endpoint request with its parameters
     * @param endpoint API endpoint called
     * @param params parameters passed to endpoint
     * @return key
     */
    public static String createKey(String endpoint, Object... params) {
        StringBuilder key = new StringBuilder(endpoint);
        String separator = "?";
        for (Object param : params) {
            key.append(separator).append(String.valueOf(param));
            separator = "&";
        }
        return key.toString();
    }

    /**
     * @param key key to set
     * @param value value to set the key
     * @param timeout cache expire time in seconds
     */
    public void set(String key, String value, int timeout) {
        try (Jedis redis = getAuthenticatedJedis()) {
            redis.setex(key, timeout, value);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * Set a key with the default cache timeout
     * @see #set(String, String, int)
     */
    public void set(String key, String value) {
        set(key, value, TIMEOUT);
    }


    /**
     * Get multiple keys each with a common prefix
     * @param keyPrefix prefix to be added to all keys
     * @param keys keys to get
     * @return map with redis keys as the key and possibly null value if key did not exist
     */
    public Map<String, String> mgetWithPrefix(String keyPrefix, List<String> keys) {
        Map<String, String> map = new HashMap<>();
        String[] prefixedKeys = new String[keys.size()];
        for (int keyIndex = 0; keyIndex < keys.size(); keyIndex++)
            prefixedKeys[keyIndex] = keyPrefix + String.valueOf(keys.get(keyIndex));
        List<String> values = new ArrayList<>();
        if (keys.size() > 0) {
            try (Jedis redis = getAuthenticatedJedis()) {
                values.addAll(redis.mget(prefixedKeys));
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        if (values.size() != prefixedKeys.length)
            for (String key : prefixedKeys)
                map.put(key, null);
        else
            for (int keyIndex = 0; keyIndex < prefixedKeys.length; keyIndex++)
                map.put(prefixedKeys[keyIndex], values.get(keyIndex));
        return map;
    }

    /**
     * Attempt to remove a key
     * Fails silently
     * @param key key to remove
     */
    @SuppressWarnings("UnusedReturnValue")
    public Long remove(String key) {
        long ret = 0L;
        try (Jedis redis = getAuthenticatedJedis()) {
            ret = redis.del(key);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return ret;
    }

    /**
     * Wrapper for a fail-safe mget
     * Gets multiple values
     * @param keys keys to fetch
     * @return map of all keys with potential null values if the key does not exist in cache
     */
    private Map<String, String> mget(List<String> keys) {
        return mgetWithPrefix("", keys);
    }

    /**
     * Set multiple keys with a common prefix
     * @param prefix keys prefix
     * @param keysValues key values
     * @param timeout key expire time
     */
    public void msetWithPrefix(String prefix, Map<String, String> keysValues, int timeout) {
        List<String> keysValuesList = new ArrayList<>();
        for (Map.Entry<String, String> entry : keysValues.entrySet()) {
            keysValuesList.add(prefix + entry.getKey());
            keysValuesList.add(entry.getValue());
        }
        try (Jedis redis = getAuthenticatedJedis()) {
            redis.mset(keysValuesList.toArray(new String[0]));
            Pipeline pipeline = redis.pipelined();
            pipeline.multi();
            for (String key : keysValues.keySet()) {
                pipeline.expire(prefix + key, timeout);
            }
            pipeline.exec();
            pipeline.close();
        }
        catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * Set multiple keys with a common prefix and default timeout
     * @see #msetWithPrefix(String, Map, int)
     */
    public void msetWithPrefix(String prefix, Map<String, String> keysValues) {
        msetWithPrefix(prefix, keysValues, TIMEOUT);
    }
}
