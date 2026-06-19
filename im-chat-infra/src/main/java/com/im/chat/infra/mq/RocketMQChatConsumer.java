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
@Component
@RocketMQMessageListener(
        topic = "im_chat_upstream",
        consumerGroup = "im_chat_business",
        selectorExpression = "*"
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
            String conversationId = node.get("conversationId").asText();
            String content = node.get("content").asText();
            String typeStr = node.has("messageType") ? node.get("messageType").asText() : "TEXT";

            if (senderId == null || conversationId == null || content == null) {
                log.warn("Invalid message format: {}", body);
                return;
            }

            MessageType type = MessageType.valueOf(typeStr);
            messageHandler.handle(senderId, conversationId, content, type);

        } catch (Exception e) {
            log.error("Error consuming message: {}", body, e);
            throw new RuntimeException("Consume failed, will retry", e);
        }
    }
}
