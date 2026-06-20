package com.im.chat.infra.repository;

import com.im.chat.common.UserId;
import com.im.chat.domain.user.User;
import com.im.chat.domain.user.UserRepository;
import com.im.chat.infra.persistence.po.UserPO;
import com.im.chat.infra.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepo;

    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepo.findById(userId.getValue()).map(this::toDomain);
    }

    @Override
    public List<User> findByIds(List<UserId> userIds) {
        List<String> ids = userIds.stream().map(UserId::getValue).collect(Collectors.toList());
        return jpaRepo.findAllById(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        UserPO po = toPO(user);
        UserPO saved = jpaRepo.save(po);
        return toDomain(saved);
    }

    @Override
    public boolean exists(UserId userId) {
        return jpaRepo.existsById(userId.getValue());
    }

    private User toDomain(UserPO po) {
        User user = new User(UserId.of(po.getUserId()),
                po.getUserName() != null ? po.getUserName() : po.getUserId(),
                po.getPhoto());
        if (po.getStatus() != null && po.getStatus() == 1) user.markOnline();
        return user;
    }

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setUserId(user.getUserId().getValue());
        po.setUserName(user.getNickname());
        po.setPhoto(user.getAvatar());
        po.setStatus(user.isOnline() ? 1 : 0);
        return po;
    }
}
