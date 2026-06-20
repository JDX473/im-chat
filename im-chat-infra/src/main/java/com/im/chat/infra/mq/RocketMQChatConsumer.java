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

            // Map MallImMessageTypeEnum (protocol) to ChatMessageTypeEnum (DB)
            // Protocol: 1=START_INPUT, 2=EXIT_INPUT, 3=TEXT, 4=IMAGE, 5=CARD, 6=IMAGE_TEXT
            // DB:       1=TEXT, 2=FILE, 3=RED_PACKET, 4=ARTICLE...
            // Forwarding types (1,2) are already handled by im-long-connection directly.
            // For persistence: map 3→1(TEXT), 4→2(FILE), 5→3(RED_PACKET), 6→4(ARTICLE)
            int dbType;
            switch (typeCode) {
                case 4: dbType = 2; break;   // IMAGE → FILE
                case 5: dbType = 5; break;   // CARD → keep as is
                case 6: dbType = 6; break;   // IMAGE_TEXT → keep
                default: dbType = 1;          // TEXT (3→1) and everything else
            }
            messageHandler.handle(senderId, receiverId, content, dbType);

        } catch (Exception e) {
            log.error("Error consuming message: {}", body, e);
            throw new RuntimeException("Consume failed, will retry", e);
        }
    }
}
