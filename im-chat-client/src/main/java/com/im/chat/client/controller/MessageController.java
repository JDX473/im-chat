package com.im.chat.client.controller;

import com.im.chat.app.service.MessageApplicationService;
import com.im.chat.domain.message.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Message history and read status (REST).
 * <p>
 * Sending is via im-long-connection WebSocket → RocketMQ("im_chat_upstream") → im-chat.
 * Receiving is via im-long-connection WebSocket push ← RocketMQ("im_chat_downstream") ← im-chat.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService service;

    /** Query message history (cache-aside: Redis ZSET → MySQL). */
    @GetMapping
    public List<MessageVO> history(@RequestParam String conversationId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return service.queryHistory(conversationId, page, size)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** Get unread count for a user in a conversation. */
    @GetMapping("/unread")
    public UnreadVO unread(@RequestParam String conversationId, @RequestParam String userId) {
        int count = service.getUnreadCount(conversationId, userId);
        UnreadVO vo = new UnreadVO();
        vo.conversationId = conversationId;
        vo.count = count;
        return vo;
    }

    /** Mark all messages in a conversation as read. */
    @PostMapping("/read")
    public void markRead(@RequestParam String conversationId, @RequestParam String userId) {
        service.markRead(conversationId, userId);
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

    @Data
    public static class UnreadVO {
        String conversationId;
        int count;
    }
}
