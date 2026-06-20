package com.im.chat.common.enums;

/**
 * Friend status codes matching webchat's web_chat_friend.status.
 */
public class FriendStatus {
    public static final int PENDING = 0;
    public static final int ACCEPTED = 1;
    public static final int BLOCKED = 2;
    public static final int DELETED = 3;

    private FriendStatus() {}

    public static String nameOf(int status) {
        switch (status) {
            case 0: return "PENDING";
            case 1: return "ACCEPTED";
            case 2: return "BLOCKED";
            case 3: return "DELETED";
            default: return "UNKNOWN";
        }
    }
}
