package com.im.chat.infra.repository;

import com.im.chat.common.UserId;
import com.im.chat.domain.friend.Friend;
import com.im.chat.domain.friend.FriendRepository;
import com.im.chat.common.enums.FriendStatus;
import com.im.chat.infra.persistence.po.FriendPO;
import com.im.chat.infra.persistence.repository.JpaFriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepository {

    private final JpaFriendRepository jpaRepo;

    @Override
    public Optional<Friend> findByUsers(UserId userId, UserId friendId) {
        return jpaRepo.findByUsers(userId.getValue(), friendId.getValue()).map(this::toDomain);
    }

    @Override
    public List<Friend> findByUserId(UserId userId) {
        return jpaRepo.findByUserId(userId.getValue()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Friend> findByUserIdAndStatus(UserId userId, FriendStatus status) {
        return jpaRepo.findByUserIdAndStatus(userId.getValue(), status.name()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean isFriend(UserId userId, UserId friendId) {
        return jpaRepo.findByUsers(userId.getValue(), friendId.getValue())
                .map(po -> po.getStatus().equals(FriendStatus.ACCEPTED.name()))
                .orElse(false);
    }

    @Override
    public Friend save(Friend friend) {
        FriendPO po = toPO(friend);
        FriendPO saved = jpaRepo.save(po);
        return toDomain(saved);
    }

    @Override
    public void delete(Friend friend) {
        jpaRepo.deleteById(friend.getId());
    }

    private Friend toDomain(FriendPO po) {
        Friend friend = new Friend();
        friend.setId(po.getId());
        friend.setUserId(UserId.of(po.getUserId()));
        friend.setFriendId(UserId.of(po.getFriendId()));
        friend.setStatus(FriendStatus.valueOf(po.getStatus()));
        friend.setRemark(po.getRemark());
        friend.setCreatedAt(po.getCreatedAt());
        friend.setUpdatedAt(po.getUpdatedAt());
        return friend;
    }

    private FriendPO toPO(Friend friend) {
        FriendPO po = new FriendPO();
        po.setId(friend.getId());
        po.setUserId(friend.getUserId().getValue());
        po.setFriendId(friend.getFriendId().getValue());
        po.setStatus(friend.getStatus().name());
        po.setRemark(friend.getRemark());
        po.setCreatedAt(friend.getCreatedAt());
        po.setUpdatedAt(friend.getUpdatedAt());
        return po;
    }
}
