package com.im.chat.domain.message;

import com.im.chat.common.UserId;
import java.util.Set;

/**
 * Port for publishing processed messages to the transport layer.
 */
public interface MessagePublisher {
    void publish(Message message, Set<UserId> recipients);
}
