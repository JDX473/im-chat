package com.im.chat.infra.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.common.UserId;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessagePublisher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

    /** im-long-connection's Consumer-1 subscribes to this topic */
    private static final String TOPIC_DOWNSTREAM = "webchat_ugc_messages";

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQChatProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * Send a processed message to im-long-connection for push to clients.
     */
    @Override
    public void publish(Message message, Set<UserId> recipients) {
        try {
            for (UserId recipient : recipients) {
                // Format: UgcServerMessageDTO — what im-long-connection's IMMessageHandler expects
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("msgId", message.getMessageId().getValue());
                payload.put("senderId", message.getSenderId() != null ? message.getSenderId().getValue() : "SYSTEM");
                payload.put("receiverId", recipient.getValue());  // im-long-connection routes by this
                payload.put("message", message.getContent());
                payload.put("msgTime", message.getCreatedAt().toEpochMilli());
                payload.put("read", false);

                String json = mapper.writeValueAsString(payload);
                rocketMQTemplate.convertAndSend(TOPIC_DOWNSTREAM, json);
            }
            log.debug("Produced downstream: msgId={}, recipients={}", message.getMessageId(), recipients.size());

        } catch (Exception e) {
            log.error("Failed to produce downstream: msgId={}", message.getMessageId(), e);
        }
    }
}
