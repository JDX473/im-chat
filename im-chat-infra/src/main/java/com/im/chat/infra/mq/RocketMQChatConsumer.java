package com.im.chat.infra.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.domain.message.InboundMessageHandler;
import com.im.chat.common.enums.MessageType;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Consumes client messages from im-long-connection via RocketMQ.
 * <p>
 * Upstream flow:
 * Client → WebSocket → im-long-connection → RocketMQ("im_chat_upstream") → this consumer
 *
 * @see RocketMQChatProducer for the downstream (push) side
 */
/**
 * Consumes client messages from im-long-connection via RocketMQ.
 * <p>
 * Topic: "client2server" (im-long-connection's producer topic, tag "im_chat").
 * Message format: NettyServerMessageDTO JSON — senderId, receiverId, message, messageType.
 */
@Component
@RocketMQMessageListener(
        topic = "client2server",
        consumerGroup = "im_chat_business",
        selectorExpression = "im_chat"
)
public class RocketMQChatConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(RocketMQChatConsumer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final InboundMessageHandler messageHandler;

    public RocketMQChatConsumer(InboundMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onMessage(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            String senderId = node.get("senderId").asText();
            String receiverId = node.get("receiverId").asText();
            String content = node.get("message").asText();
            int typeCode = node.has("messageType") ? node.get("messageType").asInt() : 3;

            if (senderId == null || receiverId == null || content == null) {
                log.warn("Invalid message format: {}", body);
                return;
            }

            // Map im-long-connection's messageType codes (MallImMessageTypeEnum) to im-chat's MessageType
            MessageType type = mapType(typeCode);
            messageHandler.handle(senderId, receiverId, content, type);

        } catch (Exception e) {
            log.error("Error consuming message: {}", body, e);
            throw new RuntimeException("Consume failed, will retry", e);
        }
    }

    /** Map MallImMessageTypeEnum codes to MessageType. */
    private MessageType mapType(int code) {
        switch (code) {
            case 4: return MessageType.IMAGE;
            default: return MessageType.TEXT;
        }
    }
}
