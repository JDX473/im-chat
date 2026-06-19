package com.im.chat.domain.friend;

/**
 * Friend relationship status.
 */
public enum FriendStatus {

    /** Friend request sent, waiting for acceptance */
    PENDING,

    /** Friend relationship established */
    ACCEPTED,

    /** Friend relationship blocked by one party */
    BLOCKED,

    /** Friend relationship removed */
    DELETED
}
