package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.GroupUserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaGroupUserRepository extends JpaRepository<GroupUserPO, Long> {

    List<GroupUserPO> findByGroupId(String groupId);

    List<GroupUserPO> findByUserId(String userId);

    GroupUserPO findByGroupIdAndUserId(String groupId, String userId);

    void deleteByGroupId(String groupId);
}
