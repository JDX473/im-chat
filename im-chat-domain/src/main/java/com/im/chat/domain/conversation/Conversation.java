package com.im.chat.domain.conversation;

import com.im.chat.domain.common.ConversationId;
import com.im.chat.domain.common.UserId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.*;

/**
 * Conversation aggregate root (private chat or group chat).
 * <p>
 * Invariants:
 * <ul>
 *   <li>PRIVATE conversation has exactly 2 members</li>
 *   <li>GROUP conversation has 2 to 200 members</li>
 *   <li>Only members can send messages to the conversation</li>
 *   <li>Only OWNER can disband a GROUP conversation</li>
 * </ul>
 */
@Getter
@Setter
public class Conversation {

    private static final int MAX_GROUP_MEMBERS = 200;

    private ConversationId conversationId;
    private ConversationType type;
    private String name;
    private String avatar;
    private Set<ConversationMember> members = new LinkedHashSet<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Conversation() {}

    // ── Factory methods ───────────────────────────────────────────

    /** Create a 1-on-1 private conversation between two users. */
    public static Conversation createPrivate(ConversationId id, UserId userA, UserId userB) {
        if (userA.equals(userB)) {
            throw new IllegalArgumentException("Cannot create private conversation with yourself");
        }
        Conversation c = new Conversation();
        c.conversationId = id;
        c.type = ConversationType.PRIVATE;
        c.name = null; // private chats have no name
        c.members.add(ConversationMember.member(userA));
        c.members.add(ConversationMember.member(userB));
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    /** Create a group conversation. */
    public static Conversation createGroup(ConversationId id, String name, UserId owner, Set<UserId> initialMembers) {
        if (initialMembers == null || initialMembers.size() < 1) {
            throw new IllegalArgumentException("Group must have at least 1 member besides owner");
        }
        if (initialMembers.size() >= MAX_GROUP_MEMBERS) {
            throw new IllegalArgumentException("Group cannot exceed " + MAX_GROUP_MEMBERS + " members");
        }
        Conversation c = new Conversation();
        c.conversationId = id;
        c.type = ConversationType.GROUP;
        c.name = name;
        c.members.add(ConversationMember.owner(owner));
        for (UserId member : initialMembers) {
            if (!member.equals(owner)) {
                c.members.add(ConversationMember.member(member));
            }
        }
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    // ── Commands ──────────────────────────────────────────────────

    public void addMember(UserId operator, UserId newMember) {
        assertGroup();
        assertAdmin(operator);
        if (members.size() >= MAX_GROUP_MEMBERS) {
            throw new IllegalStateException("Group is full (" + MAX_GROUP_MEMBERS + " max)");
        }
        if (containsMember(newMember)) {
            throw new IllegalArgumentException("User is already a member");
        }
        members.add(ConversationMember.member(newMember));
        updatedAt = Instant.now();
    }

    public void removeMember(UserId operator, UserId target) {
        assertGroup();
        assertAdmin(operator);
        if (findMember(target).map(m -> m.getRole() == MemberRole.OWNER).orElse(false)) {
            throw new IllegalArgumentException("Cannot remove the group owner");
        }
        members.removeIf(m -> m.getUserId().equals(target));
        updatedAt = Instant.now();
    }

    public void disband(UserId operator) {
        assertGroup();
        if (!isOwner(operator)) {
            throw new IllegalStateException("Only the owner can disband the group");
        }
        members.clear();
        updatedAt = Instant.now();
    }

    public void updateInfo(String name, String avatar) {
        assertGroup();
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
        updatedAt = Instant.now();
    }

    // ── Queries ───────────────────────────────────────────────────

    public boolean containsMember(UserId userId) {
        return members.stream().anyMatch(m -> m.getUserId().equals(userId));
    }

    public boolean isOwner(UserId userId) {
        return findMember(userId).map(m -> m.getRole() == MemberRole.OWNER).orElse(false);
    }

    public boolean isAdmin(UserId userId) {
        return findMember(userId)
                .map(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    public Optional<ConversationMember> findMember(UserId userId) {
        return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
    }

    public Set<UserId> memberIds() {
        Set<UserId> ids = new LinkedHashSet<>();
        for (ConversationMember m : members) {
            ids.add(m.getUserId());
        }
        return Collections.unmodifiableSet(ids);
    }

    public int memberCount() {
        return members.size();
    }

    public boolean isActive() {
        return !members.isEmpty();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void assertGroup() {
        if (type != ConversationType.GROUP) {
            throw new IllegalStateException("Operation only valid for group conversations");
        }
    }

    private void assertAdmin(UserId userId) {
        if (!isAdmin(userId)) {
            throw new IllegalStateException("Only admin/owner can perform this operation");
        }
    }
}
