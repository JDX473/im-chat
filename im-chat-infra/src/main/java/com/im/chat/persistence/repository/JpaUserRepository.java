package com.im.chat.infra.persistence.repository;

import com.im.chat.infra.persistence.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<UserPO, String> {
}
