package com.im.chat.domain.message;

import com.im.chat.common.UserId;

import java.util.Set;

/**
 * Domain port for publishing processed messages to the transport layer.
 * Implemented by infrastructure (RocketMQ) to send messages to im-long-connection for push delivery.
 */
public interface MessagePublisher {

    /**
     * Publish a processed message to the transport layer for delivery to the specified users.
     *
     * @param message    the persisted message
     * @param recipients users to push the message to (from conversation members)
     */
    void publish(Message message, Set<UserId> recipients);
}
