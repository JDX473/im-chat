package com.im.chat.infra.repository;

import com.im.chat.common.ConversationId;
import com.im.chat.common.MessageId;
import com.im.chat.common.UserId;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessageRepository;
import com.im.chat.common.enums.MessageStatus;
import com.im.chat.common.enums.MessageType;
import com.im.chat.infra.persistence.po.MessagePO;
import com.im.chat.infra.persistence.repository.JpaMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaMessageRepository jpaRepo;

    @Override
    public Optional<Message> findById(MessageId id) {
        return jpaRepo.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public List<Message> findByConversation(ConversationId conversationId, int page, int size) {
        return jpaRepo.findByConversationIdOrderByCreatedAtDesc(
                conversationId.getValue(),
                PageRequest.of(page, size)
        ).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationAfter(ConversationId conversationId, long afterTimestamp) {
        return jpaRepo.findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(
                conversationId.getValue(),
                Instant.ofEpochMilli(afterTimestamp)
        ).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Message save(Message message) {
        MessagePO po = toPO(message);
        MessagePO saved = jpaRepo.save(po);
        return toDomain(saved);
    }

    @Override
    public int countUnread(ConversationId conversationId, UserId userId, long lastReadTimestamp) {
        // Simplified: count messages after timestamp not sent by this user
        return (int) jpaRepo.findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(
                conversationId.getValue(),
                Instant.ofEpochMilli(lastReadTimestamp)
        ).stream()
                .filter(m -> !m.getSenderId().equals(userId.getValue()))
                .count();
    }

    private Message toDomain(MessagePO po) {
        Message msg = new Message();
        msg.setMessageId(MessageId.of(po.getMessageId()));
        msg.setConversationId(ConversationId.of(po.getConversationId()));
        msg.setSenderId(po.getSenderId() != null ? UserId.of(po.getSenderId()) : null);
        msg.setContent(po.getContent());
        msg.setType(MessageType.valueOf(po.getType()));
        msg.setStatus(MessageStatus.valueOf(po.getStatus()));
        msg.setCreatedAt(po.getCreatedAt());
        return msg;
    }

    private MessagePO toPO(Message msg) {
        MessagePO po = new MessagePO();
        po.setMessageId(msg.getMessageId().getValue());
        po.setConversationId(msg.getConversationId().getValue());
        po.setSenderId(msg.getSenderId() != null ? msg.getSenderId().getValue() : null);
        po.setContent(msg.getContent());
        po.setType(msg.getType().name());
        po.setStatus(msg.getStatus().name());
        po.setCreatedAt(msg.getCreatedAt());
        return po;
    }
}
