package com.im.chat.domain.common;

import lombok.Value;

/**
 * Message identifier value object.
 */
@Value
public class MessageId {
    String value;

    public static MessageId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("messageId cannot be blank");
        }
        return new MessageId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
