package com.im.chat.domain.message;

import com.im.chat.common.ConversationId;

import java.util.List;

/**
 * Domain port for message cache (Redis ZSET, like webchat's MALL_IM_MESSAGE_HISTORY_CACHE).
 */
public interface MessageCache {

    /** Cache a newly persisted message (add to ZSET). */
    void cache(Message message);

    /** Get recent messages from cache. Returns empty list on cache miss. */
    List<Message> getRecent(ConversationId conversationId, int count);
}
