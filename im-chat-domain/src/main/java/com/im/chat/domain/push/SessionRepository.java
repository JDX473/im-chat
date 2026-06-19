package com.im.chat.domain.push;

import com.im.chat.common.UserId;

import java.util.Optional;

/**
 * Distributed session registry — maps userId to server node.
 */
public interface SessionRepository {

    /** Register a user's WebSocket session on this node. */
    void register(Session session);

    /** Remove a user's session registration. */
    void remove(UserId userId);

    /** Find which node holds a user's connection. */
    Optional<String> findNodeId(UserId userId);
}
