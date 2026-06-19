package com.im.chat.infra.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessagePublisher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sends processed messages back to im-long-connection for push delivery.
 * <p>
 * Downstream flow:
 * im-chat (this producer) → RocketMQ("im_chat_downstream") → im-long-connection → WebSocket push to client
 *
 * @see RocketMQChatConsumer for the upstream (consume) side
 */
@Component
public class RocketMQChatProducer implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RocketMQChatProducer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String TOPIC_DOWNSTREAM = "im_chat_downstream";

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQChatProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * Send a processed message to im-long-connection for push to clients.
     */
    @Override
    public void publish(Message message) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messageId", message.getMessageId().getValue());
            payload.put("conversationId", message.getConversationId().getValue());
            payload.put("senderId", message.getSenderId() != null ? message.getSenderId().getValue() : "SYSTEM");
            payload.put("content", message.getContent());
            payload.put("type", message.getType().name());
            payload.put("createdAt", message.getCreatedAt().toEpochMilli());

            String json = mapper.writeValueAsString(payload);

            // Send to all nodes (broadcast) — im-long-connection will route to the right one
            rocketMQTemplate.convertAndSend(TOPIC_DOWNSTREAM, json);

            log.debug("Produced downstream message: msgId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("Failed to produce downstream message: msgId={}", message.getMessageId(), e);
        }
    }
}
