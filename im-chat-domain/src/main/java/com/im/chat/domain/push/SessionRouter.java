package com.im.chat.domain.push;

import com.im.chat.common.UserId;

import java.util.Optional;

/**
 * Domain port: query which Netty node a user is connected to.
 * Reads from Redis Hash built by im-long-connection.
 */
public interface SessionRouter {

    /**
     * Find the node tag (MD5) for a user.
     * @return node MD5 tag if online, empty if offline
     */
    Optional<String> findNodeTag(UserId userId);
}
