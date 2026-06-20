package com.im.chat.app.service;

import com.im.chat.common.UserId;
import com.im.chat.common.enums.FriendStatus;
import com.im.chat.domain.friend.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for friend relationship use cases.
 */
@Service
@RequiredArgsConstructor
public class FriendApplicationService {

    private final FriendRepository friendRepository;

    /** Send a friend request. */
    @Transactional
    public Friend sendRequest(String fromUserId, String toUserId) {
        UserId from = UserId.of(fromUserId);
        UserId to = UserId.of(toUserId);

        // Check existing relationship
        friendRepository.findByUsers(from, to).ifPresent(existing -> {
            if (existing.getStatus() == FriendStatus.BLOCKED) {
                throw new IllegalStateException("Cannot send request: blocked relationship");
            }
            if (existing.getStatus() == FriendStatus.ACCEPTED) {
                throw new IllegalStateException("Already friends");
            }
            if (existing.getStatus() == FriendStatus.PENDING) {
                throw new IllegalStateException("Friend request already pending");
            }
        });

        Friend request = Friend.request(from, to);
        return friendRepository.save(request);
    }

    /** Accept a pending friend request. */
    @Transactional
    public void acceptRequest(String userId, String requestFromUserId) {
        UserId me = UserId.of(userId);
        UserId from = UserId.of(requestFromUserId);

        Friend request = friendRepository.findByUsers(from, me)
                .orElseThrow(() -> new IllegalArgumentException("No friend request found"));

        // Guard: only the recipient can accept (not the sender)
        if (!request.getFriendId().equals(me)) {
            throw new IllegalArgumentException("Only the recipient can accept a friend request");
        }
        request.accept();
        friendRepository.save(request);
    }

    /** Reject a pending friend request. */
    @Transactional
    public void rejectRequest(String userId, String requestFromUserId) {
        UserId me = UserId.of(userId);
        UserId from = UserId.of(requestFromUserId);

        Friend request = friendRepository.findByUsers(from, me)
                .orElseThrow(() -> new IllegalArgumentException("No friend request found"));

        if (!request.getFriendId().equals(me)) {
            throw new IllegalArgumentException("Only the recipient can reject a friend request");
        }
        request.reject();
        friendRepository.save(request);
    }

    /** Delete a friend. */
    @Transactional
    public void deleteFriend(String userId, String friendUserId) {
        UserId me = UserId.of(userId);
        UserId target = UserId.of(friendUserId);

        Friend friend = friendRepository.findByUsers(me, target)
                .orElseThrow(() -> new IllegalArgumentException("Not friends"));

        friendRepository.delete(friend);
    }

    /** Block a friend. */
    @Transactional
    public void blockFriend(String userId, String friendUserId) {
        UserId me = UserId.of(userId);
        UserId target = UserId.of(friendUserId);

        Friend friend = friendRepository.findByUsers(me, target)
                .orElseThrow(() -> new IllegalArgumentException("Not friends"));

        friend.block();
        friendRepository.save(friend);
    }

    /** Unblock a previously blocked friend. */
    @Transactional
    public void unblockFriend(String userId, String friendUserId) {
        UserId me = UserId.of(userId);
        UserId target = UserId.of(friendUserId);

        Friend friend = friendRepository.findByUsers(me, target)
                .orElseThrow(() -> new IllegalArgumentException("No relationship found"));

        friend.unblock();
        friendRepository.save(friend);
    }

    /** List all accepted friends. */
    public List<Friend> listFriends(String userId) {
        return friendRepository.findByUserIdAndStatus(UserId.of(userId), FriendStatus.ACCEPTED);
    }

    /** List pending friend requests sent TO me. */
    public List<Friend> listPendingRequests(String userId) {
        return friendRepository.findByUserIdAndStatus(UserId.of(userId), FriendStatus.PENDING);
    }

    /** Check if two users are friends. */
    public boolean isFriend(String userA, String userB) {
        return friendRepository.isFriend(UserId.of(userA), UserId.of(userB));
    }
}
