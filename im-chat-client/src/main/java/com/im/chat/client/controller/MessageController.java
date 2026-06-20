package com.im.chat.client.controller;

import com.im.chat.app.service.MessageApplicationService;
import com.im.chat.domain.message.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Message history (REST).
 * Sending via im-long-connection WebSocket → RocketMQ.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService service;

    /** Query private chat history. */
    @GetMapping("/private")
    public List<MessageVO> privateHistory(@RequestParam String userA, @RequestParam String userB,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return service.queryPrivateHistory(userA, userB, page, size)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** Query group chat history. */
    @GetMapping("/group")
    public List<MessageVO> groupHistory(@RequestParam String groupId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return service.queryGroupHistory(groupId, page, size)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private MessageVO toVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.id = msg.getId();
        vo.senderId = msg.getSenderId() != null ? msg.getSenderId().getValue() : null;
        vo.receiverId = msg.getReceiverId() != null ? msg.getReceiverId().getValue() : null;
        vo.content = msg.getContent();
        vo.type = msg.getType();
        vo.isRead = msg.getIsRead();
        vo.sendDate = msg.getSendDate() != null ? msg.getSendDate().toEpochMilli() : 0;
        return vo;
    }

    @Data
    public static class MessageVO {
        Long id;
        String senderId;
        String receiverId;
        String content;
        Integer type;
        Boolean isRead;
        long sendDate;
    }
}
