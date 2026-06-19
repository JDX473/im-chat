package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.ConversationMemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaConversationMemberRepository extends JpaRepository<ConversationMemberPO, Long> {

    List<ConversationMemberPO> findByConversationId(String conversationId);

    List<ConversationMemberPO> findByConversationIdIn(Collection<String> conversationIds);

    List<ConversationMemberPO> findByUserId(String userId);

    Optional<ConversationMemberPO> findByConversationIdAndUserId(String conversationId, String userId);

    void deleteByConversationId(String conversationId);
}
