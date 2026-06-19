package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.ConversationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaConversationRepository extends JpaRepository<ConversationPO, String> {
}
