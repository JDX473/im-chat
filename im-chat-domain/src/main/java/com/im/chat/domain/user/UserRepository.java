package com.im.chat.domain.user;

import com.im.chat.domain.common.UserId;
import java.util.List;
import java.util.Optional;

/**
 * User repository interface (domain port).
 */
public interface UserRepository {

    Optional<User> findById(UserId userId);

    List<User> findByIds(List<UserId> userIds);

    User save(User user);

    boolean exists(UserId userId);
}
