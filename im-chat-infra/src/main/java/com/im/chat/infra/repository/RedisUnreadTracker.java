package com.im.chat.infra.repository;

import com.im.chat.common.ConversationId;
import com.im.chat.common.UserId;
import com.im.chat.domain.message.UnreadTracker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Hash-based unread count tracker (aligns with webchat's UN_READ_MESS_COUNT_CACHE).
 * <p>
 * Key: "im:unread:{conversationId}" — Hash: userId → count.
 */
@Component
public class RedisUnreadTracker implements UnreadTracker {

    private static final String KEY_PREFIX = "im:unread:";

    private final StringRedisTemplate redis;

    public RedisUnreadTracker(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void increment(ConversationId conversationId, UserId userId) {
        String key = key(conversationId);
        redis.opsForHash().increment(key, userId.getValue(), 1);
    }

    @Override
    public int getCount(ConversationId conversationId, UserId userId) {
        String key = key(conversationId);
        Object val = redis.opsForHash().get(key, userId.getValue());
        if (val == null) return 0;
        return Integer.parseInt(val.toString());
    }

    @Override
    public void clear(ConversationId conversationId, UserId userId) {
        String key = key(conversationId);
        redis.opsForHash().delete(key, userId.getValue());
    }

    private String key(ConversationId convId) {
        return KEY_PREFIX + convId.getValue();
    }
}
