package com.im.chat.domain.message;

import com.im.chat.common.enums.MessageType;

/**
 * Domain port: handles an inbound message from the transport layer.
 * Implemented by the application layer, called by infrastructure (MQ consumer).
 */
public interface InboundMessageHandler {

    /**
     * Process a message that arrived from the transport layer.
     *
     * @return the persisted message
     */
    Message handle(String senderId, String conversationId, String content, MessageType type);
}
