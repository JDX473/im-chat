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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Publishes processed messages to im-long-connection via tag-directed RocketMQ.
 * <p>
 * Before sending, queries {@link SessionRouter} (Redis) to find which Netty node
 * the recipient is connected to, then sends with that node's MD5 as the RocketMQ tag.
 * Only the target node's Consumer (subscribing to its own MD5 tag) receives the message.
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
            // 1. Look up which node the recipient is connected to
            Optional<String> nodeTag = sessionRouter.findNodeTag(recipient);

            if (!nodeTag.isPresent()) {
                log.debug("Recipient {} is offline, skip push (message in MySQL)", recipient.getValue());
                continue;
            }

            // 2. Build payload (UgcServerMessageDTO format)
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("msgId", message.getMessageId().getValue());
            payload.put("senderId", message.getSenderId() != null ? message.getSenderId().getValue() : "SYSTEM");
            payload.put("receiverId", recipient.getValue());
            payload.put("message", message.getContent());
            payload.put("msgTime", message.getCreatedAt().toEpochMilli());
            payload.put("read", false);

            try {
                String json = mapper.writeValueAsString(payload);
                // 3. Send to the target node's tag — only that node's Consumer receives it
                String destination = TOPIC + ":" + nodeTag.get();
                rocketMQTemplate.convertAndSend(destination, json);
                log.debug("Push directed: msgId={} → recipient={} tag={}", message.getMessageId(), recipient.getValue(), nodeTag.get());
            } catch (Exception e) {
                log.error("Failed to push: msgId={} → recipient={}", message.getMessageId(), recipient.getValue(), e);
            }
        }
    }
}
