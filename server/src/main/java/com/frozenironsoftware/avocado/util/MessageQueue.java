package com.frozenironsoftware.avocado.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class MessageQueue {

    private Connection queue;

    public MessageQueue(String url) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(url);
        }
        catch (NoSuchAlgorithmException | KeyManagementException | URISyntaxException e) {
            Logger.exception(e);
            Logger.warn("Failed to create connection factory");
            System.exit(1);
        }
        try {
            queue = factory.newConnection();
        }
        catch (IOException | TimeoutException e) {
            Logger.exception(e);
            Logger.warn("Failed to connect to message queue");
            System.exit(1);
        }
    }
}
