package com.im.chat.application.service;

import com.im.chat.domain.common.ConversationId;
import com.im.chat.domain.common.MessageId;
import com.im.chat.domain.common.UserId;
import com.im.chat.domain.conversation.Conversation;
import com.im.chat.domain.conversation.ConversationRepository;
import com.im.chat.domain.friend.Friend;
import com.im.chat.domain.friend.FriendRepository;
import com.im.chat.domain.friend.FriendStatus;
import com.im.chat.domain.message.*;
import com.im.chat.domain.push.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Application service for message sending use cases.
 * <p>
 * Orchestrates: domain validation → persistence → push delivery.
 */
@Service
@RequiredArgsConstructor
public class MessageApplicationService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;
    private final PushService pushService;

    /**
     * Send a message to a conversation.
     */
    @Transactional
    public Message sendMessage(String senderId, String conversationId, String content, MessageType type) {
        UserId sender = UserId.of(senderId);
        ConversationId convId = ConversationId.of(conversationId);

        // 1. Validate: conversation exists and sender is a member
        Conversation conversation = conversationRepository.findById(convId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.containsMember(sender)) {
            throw new IllegalStateException("You are not a member of this conversation");
        }

        if (!conversation.isActive()) {
            throw new IllegalStateException("Conversation has been disbanded");
        }

        // 2. Validate: for private chat, both parties must be friends
        if (conversation.getType() == com.im.chat.domain.conversation.ConversationType.PRIVATE) {
            Set<UserId> members = conversation.memberIds();
            members.remove(sender);
            UserId other = members.iterator().next();

            Friend relationship = friendRepository.findByUsers(sender, other)
                    .orElseThrow(() -> new IllegalStateException("Not friends — cannot send message"));

            if (relationship.getStatus() != FriendStatus.ACCEPTED) {
                throw new IllegalStateException("Cannot send message: friend status is " + relationship.getStatus());
            }
        }

        // 3. Create message (domain logic)
        MessageId msgId = MessageId.of("MSG_" + UUID.randomUUID().toString().replace("-", ""));
        Message message = Message.send(msgId, convId, sender, content, type);

        // 4. Persist
        message = messageRepository.save(message);

        // 5. Push to conversation members (exclude sender)
        pushService.pushToConversation(conversation.memberIds(), message, sender);

        return message;
    }

    /** Query message history (paginated, newest first). */
    public List<Message> queryHistory(String conversationId, int page, int size) {
        return messageRepository.findByConversation(
                ConversationId.of(conversationId), page, size);
    }

    /** System message: member joined / left group. */
    @Transactional
    public void sendSystemMessage(String conversationId, String content) {
        ConversationId convId = ConversationId.of(conversationId);
        MessageId msgId = MessageId.of("SYS_" + UUID.randomUUID().toString().replace("-", ""));
        Message sysMsg = Message.system(msgId, convId, content);
        messageRepository.save(sysMsg);

        Conversation conversation = conversationRepository.findById(convId).orElse(null);
        if (conversation != null) {
            pushService.pushToConversation(conversation.memberIds(), sysMsg, null);
        }
    }
}
