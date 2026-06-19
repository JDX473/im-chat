package com.im.chat.infra.repository;

import com.im.chat.common.UserId;
import com.im.chat.domain.push.SessionRouter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Reads im-long-connection's session registry (Redis Hash).
 * <p>
 * Key: IM_LC_USER_NETTY_CHANNEL_INSTANCE_HASH_CACHE
 * Field: userId
 * Value: node MD5 tag
 */
@Component
public class RedisSessionRouter implements SessionRouter {

    private static final String SESSION_KEY = "IM_LC_USER_NETTY_CHANNEL_INSTANCE_HASH_CACHE";

    private final StringRedisTemplate redis;

    public RedisSessionRouter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> findNodeTag(UserId userId) {
        Object tag = redis.opsForHash().get(SESSION_KEY, userId.getValue());
        return Optional.ofNullable(tag).map(Object::toString);
    }
}
