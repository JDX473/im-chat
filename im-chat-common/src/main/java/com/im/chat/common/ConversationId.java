package com.im.chat.common;

import lombok.Value;

/**
 * Conversation identifier value object.
 */
@Value
public class ConversationId {
    String value;

    public static ConversationId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("conversationId cannot be blank");
        }
        return new ConversationId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
