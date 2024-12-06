package ru.tbank.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitProducer {
    private static final String QUEUE = "queue";
    private final Channel channel;

    public RabbitProducer(String host, int port, String username, String password) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        channel = connectionFactory.newConnection().createChannel();
        channel.queueDeclare(QUEUE, false, false, false, null);
    }

    public void send(String message) throws IOException {
          channel.basicPublish("", QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

