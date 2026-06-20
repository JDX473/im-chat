package com.im.chat.infra.repository;

import com.im.chat.common.UserId;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessageRepository;
import com.im.chat.infra.persistence.po.MessagePO;
import com.im.chat.infra.persistence.repository.JpaMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaMessageRepository jpaRepo;

    @Override
    public Message save(Message message) {
        MessagePO po = toPO(message);
        MessagePO saved = jpaRepo.save(po);
        return toDomain(saved);
    }

    @Override
    public List<Message> findPrivateMessages(String userA, String userB, int page, int size) {
        return jpaRepo.findPrivateMessages(userA, userB, PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Message> findGroupMessages(String groupId, int page, int size) {
        return jpaRepo.findByReceiverOrderBySendDateDesc(groupId, PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Message toDomain(MessagePO po) {
        Message m = new Message();
        m.setId(po.getId());
        m.setSenderId(po.getSender() != null ? UserId.of(po.getSender()) : null);
        m.setProxySenderId(po.getProxySender() != null ? UserId.of(po.getProxySender()) : null);
        m.setReceiverId(UserId.of(po.getReceiver()));
        m.setContent(po.getMessage());
        m.setType(po.getType());
        m.setIsRead(po.getIsRead());
        m.setSendDate(po.getSendDate() != null ? po.getSendDate().toInstant() : null);
        m.setUpdateDate(po.getUpdateDate() != null ? po.getUpdateDate().toInstant() : null);
        return m;
    }

    private MessagePO toPO(Message m) {
        MessagePO po = new MessagePO();
        po.setId(m.getId());
        po.setSender(m.getSenderId() != null ? m.getSenderId().getValue() : null);
        po.setProxySender(m.getProxySenderId() != null ? m.getProxySenderId().getValue() : null);
        po.setReceiver(m.getReceiverId().getValue());
        po.setType(m.getType());
        po.setMessage(m.getContent());
        po.setIsRead(m.getIsRead());
        return po;
    }
}
