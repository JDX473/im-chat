package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.MessagePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * webchat's web_chat_message table queries.
 * Private chat: query by (sender=A, receiver=B) OR (sender=B, receiver=A).
 * Group chat: receiver=groupId.
 */
@Repository
public interface JpaMessageRepository extends JpaRepository<MessagePO, Long> {

    /** Private chat history between two users (newest first). */
    @Query("SELECT m FROM MessagePO m WHERE " +
           "(m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) " +
           "ORDER BY m.sendDate DESC")
    List<MessagePO> findPrivateMessages(String userA, String userB, Pageable pageable);

    /** Group chat history. */
    List<MessagePO> findByReceiverOrderBySendDateDesc(String groupId, Pageable pageable);

    /** Messages after a timestamp (for incremental sync). */
    @Query("SELECT m FROM MessagePO m WHERE " +
           "(m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) " +
           "AND m.sendDate > ?3 ORDER BY m.sendDate ASC")
    List<MessagePO> findPrivateMessagesAfter(String userA, String userB, Date after);
}
