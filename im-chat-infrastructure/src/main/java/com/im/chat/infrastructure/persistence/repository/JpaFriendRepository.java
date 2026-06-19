package com.im.chat.infrastructure.persistence.repository;

import com.im.chat.infrastructure.persistence.po.FriendPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFriendRepository extends JpaRepository<FriendPO, Long> {

    @Query("SELECT f FROM FriendPO f WHERE (f.userId = ?1 AND f.friendId = ?2) OR (f.userId = ?2 AND f.friendId = ?1)")
    Optional<FriendPO> findByUsers(String userA, String userB);

    List<FriendPO> findByUserId(String userId);

    List<FriendPO> findByUserIdAndStatus(String userId, String status);
}
