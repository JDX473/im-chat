package com.im.chat.domain.friend;

import com.im.chat.common.UserId;
import com.im.chat.common.enums.FriendStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Friend relationship aggregate root.
 * <p>
 * Invariants:
 * <ul>
 *   <li>A user cannot be friends with themselves</li>
 *   <li>No duplicate relationship between the same pair</li>
 *   <li>Blocked users cannot exchange messages</li>
 * </ul>
 */
@Getter
@Setter
public class Friend {

    private Long id;
    private UserId userId;
    private UserId friendId;
    private int status;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    public Friend() {}

    private Friend(UserId userId, UserId friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Cannot add yourself as friend");
        }
        this.userId = userId;
        this.friendId = friendId;
        this.status = FriendStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /** Send a friend request. */
    public static Friend request(UserId from, UserId to) {
        return new Friend(from, to);
    }

    /** Accept a pending friend request. */
    public void accept() {
        if (this.status != FriendStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be accepted, current: " + this.status);
        }
        this.status = FriendStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    /** Reject a pending friend request. */
    public void reject() {
        if (this.status != FriendStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        this.status = FriendStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /** Block this friend. Cannot send or receive messages. */
    public void block() {
        if (this.status != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted friends can be blocked");
        }
        this.status = FriendStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    /** Unblock this friend. */
    public void unblock() {
        if (this.status != FriendStatus.BLOCKED) {
            throw new IllegalStateException("Only blocked friends can be unblocked");
        }
        this.status = FriendStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    /** Set a remark for this friend. */
    public void setRemark(String remark) {
        this.remark = remark;
        this.updatedAt = Instant.now();
    }

    public boolean canSendMessage() {
        return this.status == FriendStatus.ACCEPTED;
    }
}
