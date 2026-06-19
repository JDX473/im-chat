package com.im.chat.infra.repository;

import com.im.chat.common.ConversationId;
import com.im.chat.common.MessageId;
import com.im.chat.common.UserId;
import com.im.chat.common.enums.MessageStatus;
import com.im.chat.common.enums.MessageType;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessageCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis ZSET-based message cache (aligns with webchat's MALL_IM_MESSAGE_HISTORY_CACHE).
 * <p>
 * Key: "im:msg:{conversationId}" — ZSET scored by message timestamp.
 * Only the latest 100 messages per conversation are cached.
 */
@Component
public class RedisMessageCache implements MessageCache {

    private static final String KEY_PREFIX = "im:msg:";
    private static final int MAX_CACHED = 100;

    private final StringRedisTemplate redis;

    public RedisMessageCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void cache(Message message) {
        String key = key(message.getConversationId());
        long score = message.getCreatedAt().toEpochMilli();
        String value = toCacheString(message);

        redis.opsForZSet().add(key, value, score);

        // Trim to MAX_CACHED
        Long size = redis.opsForZSet().zCard(key);
        if (size != null && size > MAX_CACHED) {
            redis.opsForZSet().removeRange(key, 0, size - MAX_CACHED - 1);
        }
    }

    @Override
    public List<Message> getRecent(ConversationId conversationId, int count) {
        String key = key(conversationId);
        Set<String> values = redis.opsForZSet().reverseRange(key, 0, count - 1);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(this::fromCacheString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String key(ConversationId convId) {
        return KEY_PREFIX + convId.getValue();
    }

    /** Compact cache format: "msgId|senderId|content|type|status|timestamp" */
    private String toCacheString(Message m) {
        return String.join("|",
                m.getMessageId().getValue(),
                m.getSenderId() != null ? m.getSenderId().getValue() : "SYSTEM",
                m.getContent().replace("|", "\\|"),
                m.getType().name(),
                m.getStatus().name(),
                String.valueOf(m.getCreatedAt().toEpochMilli()));
    }

    private Message fromCacheString(String raw) {
        try {
            String[] parts = raw.split("\\|", 6);
            if (parts.length < 6) return null;
            Message m = new Message();
            m.setMessageId(MessageId.of(parts[0]));
            m.setConversationId(null); // known from context
            m.setSenderId("SYSTEM".equals(parts[1]) ? null : UserId.of(parts[1]));
            m.setContent(parts[2].replace("\\|", "|"));
            m.setType(MessageType.valueOf(parts[3]));
            m.setStatus(MessageStatus.valueOf(parts[4]));
            m.setCreatedAt(java.time.Instant.ofEpochMilli(Long.parseLong(parts[5])));
            return m;
        } catch (Exception e) {
            return null;
        }
    }
}
