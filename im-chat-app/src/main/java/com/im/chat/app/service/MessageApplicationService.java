package com.im.chat.app.service;

import com.im.chat.common.UserId;
import com.im.chat.common.enums.FriendStatus;
import com.im.chat.domain.friend.Friend;
import com.im.chat.domain.friend.FriendRepository;
import com.im.chat.domain.message.InboundMessageHandler;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessagePublisher;
import com.im.chat.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Application service for messages — aligns with webchat's ChatMessageService + PersistentMessageService.
 * Messages reference sender/receiver directly (no conversation entity).
 */
@Service
@RequiredArgsConstructor
public class MessageApplicationService implements InboundMessageHandler {

    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;
    private final MessagePublisher mqProducer;

    /**
     * Process an inbound message from im-long-connection (via RocketMQ).
     * Validates sender-receiver are friends, persists to web_chat_message, publishes downstream.
     */
    @Override
    @Transactional
    public Message handle(String senderId, String receiverId, String content, Integer type) {
        UserId sender = UserId.of(senderId);
        UserId receiver = UserId.of(receiverId);

        // Validate: sender and receiver must be friends
        Friend relationship = friendRepository.findByUsers(sender, receiver)
                .orElseThrow(() -> new IllegalStateException("Not friends — cannot send message"));
        if (relationship.getStatus() != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot send message: friend status is " + relationship.getStatus());
        }

        // Create and persist message (aligns with web_chat_message)
        Message message = Message.createPrivate(sender, receiver, content, type);
        message = messageRepository.save(message);

        // Publish downstream for push (after commit)
        final Message finalMsg = message;
        final Set<UserId> recipients = new LinkedHashSet<>(Arrays.asList(receiver));
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        mqProducer.publish(finalMsg, recipients);
                    }
                });

        return message;
    }

    /** Query private chat history. */
    public List<Message> queryPrivateHistory(String userA, String userB, int page, int size) {
        return messageRepository.findPrivateMessages(userA, userB, page, size);
    }

    /** Query group chat history. */
    public List<Message> queryGroupHistory(String groupId, int page, int size) {
        return messageRepository.findGroupMessages(groupId, page, size);
    }
}
