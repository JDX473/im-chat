package com.im.chat.infrastructure.controller;

import com.im.chat.application.service.FriendApplicationService;
import com.im.chat.domain.friend.Friend;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendApplicationService service;

    @PostMapping("/request")
    public FriendVO sendRequest(@RequestBody RequestBody_ body) {
        Friend f = service.sendRequest(body.fromUserId, body.toUserId);
        return toVO(f);
    }

    @PostMapping("/accept")
    public void accept(@RequestBody RequestBody_ body) {
        service.acceptRequest(body.fromUserId, body.toUserId);
    }

    @PostMapping("/reject")
    public void reject(@RequestBody RequestBody_ body) {
        service.rejectRequest(body.fromUserId, body.toUserId);
    }

    @DeleteMapping("/{friendId}")
    public void delete(@PathVariable String friendId, @RequestParam String userId) {
        service.deleteFriend(userId, friendId);
    }

    @PostMapping("/block")
    public void block(@RequestBody RequestBody_ body) {
        service.blockFriend(body.fromUserId, body.toUserId);
    }

    @PostMapping("/unblock")
    public void unblock(@RequestBody RequestBody_ body) {
        service.unblockFriend(body.fromUserId, body.toUserId);
    }

    @GetMapping
    public List<FriendVO> list(@RequestParam String userId) {
        return service.listFriends(userId).stream().map(this::toVO).collect(Collectors.toList());
    }

    @GetMapping("/pending")
    public List<FriendVO> pending(@RequestParam String userId) {
        return service.listPendingRequests(userId).stream().map(this::toVO).collect(Collectors.toList());
    }

    private FriendVO toVO(Friend f) {
        FriendVO vo = new FriendVO();
        vo.userId = f.getUserId().getValue();
        vo.friendId = f.getFriendId().getValue();
        vo.status = f.getStatus().name();
        vo.remark = f.getRemark();
        return vo;
    }

    @Data
    public static class FriendVO {
        String userId;
        String friendId;
        String status;
        String remark;
    }

    @Data
    static class RequestBody_ {
        String fromUserId;
        String toUserId;
    }
}
