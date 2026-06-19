package com.im.chat.domain.push;

import com.im.chat.common.UserId;
import lombok.Value;

import java.time.Instant;

/**
 * Represents a user's WebSocket session on a specific server node.
 * Used for distributed message push routing.
 */
@Value
public class Session {
    UserId userId;
    String nodeId;      // server node identifier (ip:port)
    Instant connectedAt;

    public static Session connect(UserId userId, String nodeId) {
        return new Session(userId, nodeId, Instant.now());
    }
}
