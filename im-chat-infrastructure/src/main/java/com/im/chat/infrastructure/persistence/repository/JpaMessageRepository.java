package com.im.chat.infrastructure.persistence.repository;

import com.im.chat.infrastructure.persistence.po.MessagePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMessageRepository extends JpaRepository<MessagePO, String> {

    List<MessagePO> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<MessagePO> findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(
            String conversationId, java.time.Instant after);
}
