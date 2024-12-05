package ru.tbank.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConsumer {
    private static final String QUEUE = "queue";
    private final Channel channel;

    public RabbitConsumer(String host, int port, String username, String password) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        channel = connectionFactory.newConnection().createChannel();
        channel.queueDeclare(QUEUE, false, false, false, null);
    }

    public String consume() throws IOException {
        return channel.basicConsume(QUEUE, true, new DefaultConsumer(channel) {
        });
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

