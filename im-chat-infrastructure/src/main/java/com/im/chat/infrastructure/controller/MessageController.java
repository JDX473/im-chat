package com.im.chat.infrastructure.controller;

import com.im.chat.application.service.MessageApplicationService;
import com.im.chat.domain.message.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Message history query (REST).
 * <p>
 * Note: message SENDING is done through WebSocket, not REST.
 * Connect to ws://host:8080/im/ws?userId=xxx and send:
 * <pre>{ "conversationId": "xxx", "content": "hello", "messageType": "TEXT" }</pre>
 * senderId is derived from the WebSocket handshake userId — no spoofing.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService service;

    /** Query message history (paginated, newest first). */
    @GetMapping
    public List<MessageVO> history(@RequestParam String conversationId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return service.queryHistory(conversationId, page, size)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private MessageVO toVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.messageId = msg.getMessageId().getValue();
        vo.conversationId = msg.getConversationId().getValue();
        vo.senderId = msg.getSenderId() != null ? msg.getSenderId().getValue() : null;
        vo.content = msg.getContent();
        vo.type = msg.getType().name();
        vo.status = msg.getStatus().name();
        vo.createdAt = msg.getCreatedAt().toEpochMilli();
        return vo;
    }

    @Data
    public static class MessageVO {
        String messageId;
        String conversationId;
        String senderId;
        String content;
        String type;
        String status;
        long createdAt;
    }
}
