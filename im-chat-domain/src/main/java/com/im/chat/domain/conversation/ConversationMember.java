package com.im.chat.domain.conversation;

import com.im.chat.common.UserId;
import com.im.chat.common.enums.MemberRole;
import lombok.Value;

import java.time.Instant;

@Value
public class ConversationMember {
    UserId userId;
    MemberRole role;
    Instant joinedAt;

    public static ConversationMember owner(UserId userId) {
        return new ConversationMember(userId, MemberRole.OWNER, Instant.now());
    }

    public static ConversationMember member(UserId userId) {
        return new ConversationMember(userId, MemberRole.MEMBER, Instant.now());
    }

    public ConversationMember promote() {
        if (this.role == MemberRole.OWNER) {
            throw new IllegalStateException("Owner is already the highest role");
        }
        return new ConversationMember(userId, MemberRole.ADMIN, joinedAt);
    }
}
