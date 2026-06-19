package com.im.chat.domain.conversation;

import com.im.chat.common.ConversationId;
import com.im.chat.common.UserId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {

    Optional<Conversation> findById(ConversationId id);

    /** Find all conversations a user participates in. */
    List<Conversation> findByUserId(UserId userId);

    /** Find the private conversation between two users, if it exists. */
    Optional<Conversation> findPrivateConversation(UserId userA, UserId userB);

    Conversation save(Conversation conversation);

    void delete(Conversation conversation);
}
