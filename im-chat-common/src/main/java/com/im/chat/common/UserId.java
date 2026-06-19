package com.im.chat.common;

import lombok.Value;

/**
 * User identifier value object.
 */
@Value
public class UserId {
    String value;

    public static UserId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("userId cannot be blank");
        }
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
