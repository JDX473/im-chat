package com.im.chat.domain.message;

/**
 * Domain port for handling inbound messages from the transport layer.
 */
public interface InboundMessageHandler {
    Message handle(String senderId, String receiverId, String content, Integer type);
}
