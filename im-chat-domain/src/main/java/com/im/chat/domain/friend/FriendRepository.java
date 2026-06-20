package com.im.chat.domain.friend;


import com.im.chat.common.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Friend repository interface (domain port).
 */
public interface FriendRepository {

    Optional<Friend> findByUsers(UserId userId, UserId friendId);

    List<Friend> findByUserId(UserId userId);

    List<Friend> findByUserIdAndStatus(UserId userId, int status);

    boolean isFriend(UserId userId, UserId friendId);

    Friend save(Friend friend);

    void delete(Friend friend);
}
