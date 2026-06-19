package com.im.chat.infrastructure.controller;

import com.im.chat.application.service.ConversationApplicationService;
import com.im.chat.domain.common.UserId;
import com.im.chat.domain.conversation.Conversation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationApplicationService service;

    @PostMapping("/private")
    public ConversationVO createPrivate(@RequestBody PrivateChatRequest body) {
        Conversation c = service.getOrCreatePrivateConversation(body.userA, body.userB);
        return toVO(c);
    }

    @PostMapping("/group")
    public ConversationVO createGroup(@RequestBody CreateGroupRequest body) {
        Conversation c = service.createGroup(body.name, body.ownerId, body.memberIds);
        return toVO(c);
    }

    @PostMapping("/{id}/members")
    public void addMember(@PathVariable String id, @RequestBody AddMemberRequest body) {
        service.addMember(id, body.operatorId, body.memberId);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public void removeMember(@PathVariable String id, @PathVariable String memberId,
                             @RequestParam String operatorId) {
        service.removeMember(id, operatorId, memberId);
    }

    @DeleteMapping("/{id}")
    public void disband(@PathVariable String id, @RequestParam String operatorId) {
        service.disband(id, operatorId);
    }

    @GetMapping
    public List<ConversationVO> list(@RequestParam String userId) {
        return service.listConversations(userId).stream().map(this::toVO).collect(Collectors.toList());
    }

    @GetMapping("/{id}/members")
    public Set<String> members(@PathVariable String id) {
        return service.getMembers(id).stream().map(UserId::getValue).collect(Collectors.toSet());
    }

    private ConversationVO toVO(Conversation c) {
        ConversationVO vo = new ConversationVO();
        vo.conversationId = c.getConversationId().getValue();
        vo.type = c.getType().name();
        vo.name = c.getName();
        vo.avatar = c.getAvatar();
        vo.memberCount = c.memberCount();
        vo.createdAt = c.getCreatedAt().toString();
        return vo;
    }

    @Data
    public static class ConversationVO {
        String conversationId;
        String type;
        String name;
        String avatar;
        int memberCount;
        String createdAt;
    }

    @Data
    static class PrivateChatRequest {
        String userA;
        String userB;
    }

    @Data
    static class CreateGroupRequest {
        String name;
        String ownerId;
        Set<String> memberIds;
    }

    @Data
    static class AddMemberRequest {
        String operatorId;
        String memberId;
    }
}
