package com.im.chat.client.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.app.service.MessageApplicationService;
import com.im.chat.domain.message.Message;
import com.im.chat.common.enums.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for IM real-time messaging.
 * <p>
 * Connection: ws://host:8080/im/ws?userId=alice
 * <p>
 * After connection, the client sends JSON messages via this WebSocket
 * and receives server pushes through the same connection.
 *
 * <h3>Inbound format (client → server):</h3>
 * <pre>
 * { "conversationId": "xxx", "content": "hello", "messageType": "TEXT" }
 * </pre>
 * senderId is taken from the WebSocket connection's userId param (no spoofing).
 *
 * <h3>Outbound format (server → client):</h3>
 * <pre>
 * { "messageId":"MSG_xxx", "conversationId":"xxx", "senderId":"alice",
 *   "content":"hello", "type":"TEXT", "createdAt":1718765432000 }
 * </pre>
 */
@Component
public class ImWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /** userId → session (for push) */
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final MessageApplicationService messageService;

    public ImWebSocketHandler(MessageApplicationService messageService) {
        this.messageService = messageService;
    }

    // ── Connection lifecycle ──────────────────────────────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId == null) {
            log.warn("WS connection without userId — closing");
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        sessions.put(userId, session);
        log.info("WS connected: userId={}, total online={}", userId, sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdBySession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WS disconnected: userId={}, status={}, total online={}",
                    userId, status, sessions.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WS transport error", exception);
        String userId = getUserIdBySession(session);
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    // ── Message handling ──────────────────────────────────────────

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = getUserIdBySession(session);
        if (userId == null) {
            sendError(session, "Not authenticated");
            return;
        }

        try {
            JsonNode node = mapper.readTree(message.getPayload());
            String conversationId = node.get("conversationId").asText();
            String content = node.get("content").asText();
            String typeStr = node.has("messageType") ? node.get("messageType").asText() : "TEXT";

            if (conversationId == null || conversationId.isEmpty() || content == null || content.isEmpty()) {
                sendError(session, "conversationId and content are required");
                return;
            }

            MessageType messageType = MessageType.valueOf(typeStr);
            Message msg = messageService.sendMessage(userId, conversationId, content, messageType);

            // Send ack with messageId back to sender
            sendToSession(session, ackJson(msg));

        } catch (IllegalArgumentException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing WS message from userId={}", userId, e);
            sendError(session, "Internal error: " + e.getMessage());
        }
    }

    // ── Push helpers (used by PushServiceImpl) ─────────────────────

    static WebSocketSession getSession(String userId) {
        WebSocketSession s = sessions.get(userId);
        return (s != null && s.isOpen()) ? s : null;
    }

    static int onlineCount() {
        return sessions.size();
    }

    // ── Internal ──────────────────────────────────────────────────

    private void sendToSession(WebSocketSession session, String json) {
        try {
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to send WS message to session={}", session.getId(), e);
        }
    }

    private void sendError(WebSocketSession session, String error) {
        sendToSession(session, "{\"error\":\"" + error.replace("\"", "\\\"") + "\"}");
    }

    private String ackJson(Message msg) {
        return "{\"type\":\"ack\",\"messageId\":\"" + msg.getMessageId() + "\"}";
    }

    private String extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "userId".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private String getUserIdBySession(WebSocketSession session) {
        for (java.util.Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
