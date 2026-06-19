package com.im.chat.domain.message;

import com.im.chat.domain.common.ConversationId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    Optional<Message> findById(com.im.chat.domain.common.MessageId id);

    /** Paginated message history, newest first. */
    List<Message> findByConversation(ConversationId conversationId, int page, int size);

    /** Messages after a cursor (for incremental sync). */
    List<Message> findByConversationAfter(ConversationId conversationId, long afterTimestamp);

    Message save(Message message);

    /** Count unread messages for a user in a conversation. */
    int countUnread(ConversationId conversationId, com.im.chat.domain.common.UserId userId, long lastReadTimestamp);
}
