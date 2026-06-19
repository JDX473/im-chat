package com.im.chat.domain.message;

import com.im.chat.common.ConversationId;
import com.im.chat.common.MessageId;
import com.im.chat.common.UserId;
import com.im.chat.common.enums.MessageType;
import com.im.chat.common.enums.MessageStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Message aggregate root.
 * <p>
 * Messages are append-only. Each message belongs to exactly one conversation.
 * Messages are NOT part of the Conversation aggregate because:
 * <ul>
 *   <li>High volume — loading all messages with a conversation wouldn't scale</li>
 *   <li>No invariant spans multiple messages — each message is independently valid</li>
 *   <li>Messages reference the conversation by ID, forming a looser association</li>
 * </ul>
 */
@Getter
@Setter
public class Message {

    private MessageId messageId;
    private ConversationId conversationId;
    private UserId senderId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private Instant createdAt;

    public Message() {}

    public static Message send(MessageId messageId, ConversationId conversationId,
                                UserId senderId, String content, MessageType type) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be blank");
        }
        Message msg = new Message();
        msg.messageId = messageId;
        msg.conversationId = conversationId;
        msg.senderId = senderId;
        msg.content = content;
        msg.type = type != null ? type : MessageType.TEXT;
        msg.status = MessageStatus.SENT;
        msg.createdAt = Instant.now();
        return msg;
    }

    public static Message system(MessageId messageId, ConversationId conversationId, String content) {
        return send(messageId, conversationId, null, content, MessageType.SYSTEM);
    }

    public void markDelivered() {
        this.status = MessageStatus.DELIVERED;
    }

    public void markRead() {
        this.status = MessageStatus.READ;
    }

    public boolean isSystemMessage() {
        return this.type == MessageType.SYSTEM;
    }
}
