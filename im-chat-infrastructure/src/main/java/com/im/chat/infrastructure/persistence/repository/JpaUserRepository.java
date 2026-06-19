package com.im.chat.infrastructure.persistence.repository;

import com.im.chat.infrastructure.persistence.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<UserPO, String> {
}
