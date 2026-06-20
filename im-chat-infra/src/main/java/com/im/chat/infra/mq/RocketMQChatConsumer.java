package com.im.chat.infra.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.domain.message.InboundMessageHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Consumes from "client2server" (im-long-connection upstream topic).
 * Message format: NettyServerMessageDTO — senderId, receiverId, message, messageType.
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
            int typeCode = node.has("messageType") ? node.get("messageType").asInt() : 1;

            if (senderId == null || receiverId == null || content == null) {
                log.warn("Invalid message format: {}", body);
                return;
            }

            messageHandler.handle(senderId, receiverId, content, typeCode);

        } catch (Exception e) {
            log.error("Error consuming message: {}", body, e);
            throw new RuntimeException("Consume failed, will retry", e);
        }
    }
}
