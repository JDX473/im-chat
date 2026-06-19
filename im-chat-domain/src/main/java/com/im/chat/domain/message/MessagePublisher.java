package com.im.chat.domain.message;

/**
 * Domain port for publishing processed messages to the transport layer.
 * Implemented by infrastructure (RocketMQ) to send messages to im-long-connection for push delivery.
 */
public interface MessagePublisher {

    /**
     * Publish a processed message to the transport layer for delivery to clients.
     */
    void publish(Message message);
}
