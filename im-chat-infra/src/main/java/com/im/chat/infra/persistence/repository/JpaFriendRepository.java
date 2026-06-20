package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.FriendPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * webchat's web_chat_friend table queries.
 */
@Repository
public interface JpaFriendRepository extends JpaRepository<FriendPO, Long> {

    @Query("SELECT f FROM FriendPO f WHERE (f.userId = ?1 AND f.friendId = ?2) OR (f.userId = ?2 AND f.friendId = ?1)")
    Optional<FriendPO> findByUsers(String userA, String userB);

    @Query("SELECT f FROM FriendPO f WHERE f.userId = ?1 OR f.friendId = ?1")
    List<FriendPO> findByUserId(String userId);

    @Query("SELECT f FROM FriendPO f WHERE (f.userId = ?1 OR f.friendId = ?1) AND f.status = ?2")
    List<FriendPO> findByUserIdAndStatus(String userId, Integer status);
}
