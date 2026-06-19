package com.im.chat.infrastructure.ws;

import com.im.chat.domain.common.UserId;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

/**
 * WebSocket-based push service implementation.
 */
@Service
public class PushServiceImpl implements PushService {

    private static final Logger log = LoggerFactory.getLogger(PushServiceImpl.class);

    @Override
    public boolean pushToUser(UserId userId, Message message) {
        WebSocketSession session = ImWebSocketHandler.getSession(userId.getValue());
        if (session == null) {
            log.debug("User {} offline, message {} will be caught up on reconnect",
                    userId, message.getMessageId());
            return false;
        }

        try {
            session.sendMessage(new TextMessage(toJson(message)));
            return true;
        } catch (IOException e) {
            log.error("Push failed to user {}", userId, e);
            return false;
        }
    }

    @Override
    public void pushToConversation(Set<UserId> memberIds, Message message, UserId excludeSender) {
        for (UserId memberId : memberIds) {
            if (excludeSender != null && memberId.equals(excludeSender)) {
                continue;
            }
            pushToUser(memberId, message);
        }
    }

    private String toJson(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"messageId\":\"").append(msg.getMessageId()).append("\",");
        sb.append("\"conversationId\":\"").append(msg.getConversationId()).append("\",");
        sb.append("\"senderId\":\"").append(msg.getSenderId() != null ? msg.getSenderId() : "SYSTEM").append("\",");
        sb.append("\"content\":\"").append(escape(msg.getContent())).append("\",");
        sb.append("\"type\":\"").append(msg.getType()).append("\",");
        sb.append("\"createdAt\":").append(msg.getCreatedAt().toEpochMilli());
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
