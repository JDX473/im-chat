package com.im.chat.domain.message;

import com.im.chat.common.ConversationId;
import com.im.chat.common.UserId;

/**
 * Domain port for unread message tracking.
 */
public interface UnreadTracker {

    /** Increment unread count for a user in a conversation. */
    void increment(ConversationId conversationId, UserId userId);

    /** Get unread count for a user in a conversation. */
    int getCount(ConversationId conversationId, UserId userId);

    /** Clear unread count (user has read all messages). */
    void clear(ConversationId conversationId, UserId userId);
}
