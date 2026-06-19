package com.im.chat.app.service;

import com.im.chat.common.ConversationId;
import com.im.chat.common.UserId;
import com.im.chat.common.enums.ConversationType;
import com.im.chat.common.enums.MemberRole;
import com.im.chat.domain.conversation.*;
import com.im.chat.domain.friend.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Application service for conversation use cases (private + group chat).
 */
@Service
@RequiredArgsConstructor
public class ConversationApplicationService {

    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;

    /**
     * Get or create a private conversation between two users.
     * Only friends can chat (business rule).
     */
    @Transactional
    public Conversation getOrCreatePrivateConversation(String userA, String userB) {
        UserId a = UserId.of(userA);
        UserId b = UserId.of(userB);

        if (!friendRepository.isFriend(a, b)) {
            throw new IllegalStateException("Only friends can chat");
        }

        return conversationRepository.findPrivateConversation(a, b)
                .orElseGet(() -> {
                    ConversationId id = ConversationId.of(UUID.randomUUID().toString());
                    Conversation c = Conversation.createPrivate(id, a, b);
                    return conversationRepository.save(c);
                });
    }

    /** Create a group conversation. */
    @Transactional
    public Conversation createGroup(String name, String ownerUserId, Set<String> memberIds) {
        UserId owner = UserId.of(ownerUserId);
        Set<UserId> members = new LinkedHashSet<>();
        for (String id : memberIds) {
            members.add(UserId.of(id));
        }
        ConversationId id = ConversationId.of(UUID.randomUUID().toString());
        Conversation group = Conversation.createGroup(id, name, owner, members);
        return conversationRepository.save(group);
    }

    /** Add a member to a group. */
    @Transactional
    public void addMember(String conversationId, String operatorUserId, String newMemberUserId) {
        Conversation group = getGroup(conversationId);
        group.addMember(UserId.of(operatorUserId), UserId.of(newMemberUserId));
        conversationRepository.save(group);
    }

    /** Remove a member from a group. */
    @Transactional
    public void removeMember(String conversationId, String operatorUserId, String targetUserId) {
        Conversation group = getGroup(conversationId);
        group.removeMember(UserId.of(operatorUserId), UserId.of(targetUserId));
        conversationRepository.save(group);
    }

    /** Disband a group. */
    @Transactional
    public void disband(String conversationId, String operatorUserId) {
        Conversation group = getGroup(conversationId);
        group.disband(UserId.of(operatorUserId));
        conversationRepository.save(group);
    }

    /** List all conversations a user participates in. */
    public List<Conversation> listConversations(String userId) {
        return conversationRepository.findByUserId(UserId.of(userId));
    }

    /** Get conversation members. */
    public Set<UserId> getMembers(String conversationId) {
        return getGroup(conversationId).memberIds();
    }

    private Conversation getGroup(String conversationId) {
        Conversation c = conversationRepository.findById(ConversationId.of(conversationId))
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        if (c.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Not a group conversation");
        }
        return c;
    }
}
