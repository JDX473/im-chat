package com.im.chat.app.service;

import com.im.chat.common.ConversationId;
import com.im.chat.common.MessageId;
import com.im.chat.common.UserId;
import com.im.chat.common.enums.ConversationType;
import com.im.chat.common.enums.FriendStatus;
import com.im.chat.common.enums.MessageType;
import com.im.chat.domain.conversation.Conversation;
import com.im.chat.domain.conversation.ConversationRepository;
import com.im.chat.domain.friend.Friend;
import com.im.chat.domain.friend.FriendRepository;
import com.im.chat.domain.message.InboundMessageHandler;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessageCache;
import com.im.chat.domain.message.MessagePublisher;
import com.im.chat.domain.message.MessageRepository;
import com.im.chat.domain.message.UnreadTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Application service for message use cases.
 * <p>
 * Messages arrive from im-long-connection via RocketMQ (not WebSocket).
 * After processing (validation + persistence), the processed message
 * is sent back via RocketMQ for im-long-connection to push to clients.
 */
@Service
@RequiredArgsConstructor
public class MessageApplicationService implements InboundMessageHandler {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;
    private final MessagePublisher mqProducer;
    private final MessageCache messageCache;
    private final UnreadTracker unreadTracker;

    /**
     * Process an inbound message from the transport layer (im-long-connection via RocketMQ).
     * Validates, persists, and produces a downstream message for push delivery.
     */
    @Transactional
    @Override
    public Message handle(String senderId, String conversationId, String content, MessageType type) {
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
        if (conversation.getType() == ConversationType.PRIVATE) {
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

        // 4. Persist to MySQL
        message = messageRepository.save(message);

        // 5. Cache in Redis (hot data, like webchat's MALL_IM_MESSAGE_HISTORY_CACHE)
        messageCache.cache(message);

        // 6. Increment unread count for all members except sender
        for (UserId member : conversation.memberIds()) {
            if (!member.equals(sender)) {
                unreadTracker.increment(convId, member);
            }
        }

        // 7. Send to im-long-connection via RocketMQ for push delivery
        mqProducer.publish(message);

        return message;
    }

    /** Query message history: cache-aside (Redis hot → MySQL cold). */
    public List<Message> queryHistory(String conversationId, int page, int size) {
        ConversationId convId = ConversationId.of(conversationId);

        // First page from Redis ZSET (hot data, like webchat's cache pattern)
        if (page == 0) {
            List<Message> cached = messageCache.getRecent(convId, size);
            if (!cached.isEmpty()) {
                return cached;
            }
        }
        // Cache miss or beyond hot range → MySQL
        return messageRepository.findByConversation(convId, page, size);
    }

    /** Get unread count for a user in a conversation. */
    public int getUnreadCount(String conversationId, String userId) {
        return unreadTracker.getCount(ConversationId.of(conversationId), UserId.of(userId));
    }

    /** Mark messages as read (clear unread count). */
    public void markRead(String conversationId, String userId) {
        unreadTracker.clear(ConversationId.of(conversationId), UserId.of(userId));
    }

    /** System message: member joined / left group. */
    @Transactional
    public void sendSystemMessage(String conversationId, String content) {
        ConversationId convId = ConversationId.of(conversationId);
        MessageId msgId = MessageId.of("SYS_" + UUID.randomUUID().toString().replace("-", ""));
        Message sysMsg = Message.system(msgId, convId, content);
        messageRepository.save(sysMsg);

        mqProducer.publish(sysMsg);
    }
}
