package com.im.chat.infra.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.chat.common.UserId;
import com.im.chat.domain.message.Message;
import com.im.chat.domain.message.MessagePublisher;
import com.im.chat.domain.push.SessionRouter;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Publishes to "server2client" with tag-based directed delivery.
 * Format: UgcServerMessageDTO — senderId, receiverId, message.
 */
@Component
public class RocketMQChatProducer implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RocketMQChatProducer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TOPIC = "server2client";

    private final RocketMQTemplate rocketMQTemplate;
    private final SessionRouter sessionRouter;

    public RocketMQChatProducer(RocketMQTemplate rocketMQTemplate, SessionRouter sessionRouter) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.sessionRouter = sessionRouter;
    }

    @Override
    public void publish(Message message, Set<UserId> recipients) {
        for (UserId recipient : recipients) {
            Optional<String> nodeTag = sessionRouter.findNodeTag(recipient);
            if (!nodeTag.isPresent()) {
                log.debug("Recipient {} offline (message in MySQL)", recipient.getValue());
                continue;
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("msgId", "MSG_" + UUID.randomUUID().toString().replace("-", ""));
            payload.put("senderId", message.getSenderId() != null ? message.getSenderId().getValue() : null);
            payload.put("receiverId", recipient.getValue());
            payload.put("message", message.getContent());
            payload.put("messageType", message.getType() != null ? message.getType() : 1);
            payload.put("msgTime", System.currentTimeMillis());
            payload.put("read", false);

            try {
                String json = mapper.writeValueAsString(payload);
                String destination = TOPIC + ":" + nodeTag.get();
                rocketMQTemplate.convertAndSend(destination, json);
            } catch (Exception e) {
                log.error("Push failed: recipient={}", recipient.getValue(), e);
            }
        }
    }
}
