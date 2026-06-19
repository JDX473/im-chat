package com.im.chat.domain.message;

import com.im.chat.common.enums.MessageType;

/**
 * Domain port: handles an inbound message from the transport layer.
 * Implemented by the application layer, called by infrastructure (MQ consumer).
 */
public interface InboundMessageHandler {

    /**
     * Process a message that arrived from the transport layer (im-long-connection).
     * The transport layer only knows senderId/receiverId — the app layer resolves
     * or creates the conversation.
     *
     * @param senderId   who sent the message
     * @param receiverId intended recipient (for private chat) or groupId (for group)
     * @return the persisted message
     */
    Message handle(String senderId, String receiverId, String content, MessageType type);
}
