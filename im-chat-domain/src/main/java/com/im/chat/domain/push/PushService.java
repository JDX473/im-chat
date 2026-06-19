package com.im.chat.domain.push;

import com.im.chat.common.UserId;
import com.im.chat.domain.message.Message;

/**
 * Domain service interface for pushing messages to online users.
 * The implementation (WebSocket, MQ routing, etc.) lives in infrastructure.
 */
public interface PushService {

    /**
     * Push a message to a specific user if they are online.
     *
     * @return true if the user was online and the message was delivered
     */
    boolean pushToUser(UserId userId, Message message);

    /**
     * Push a message to all members of a conversation.
     * For N members, this makes N pushToUser calls (or one broadcast, depending on implementation).
     */
    void pushToConversation(java.util.Set<UserId> memberIds, Message message, UserId excludeSender);
}
