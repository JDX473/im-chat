package com.im.chat.infra.persistence.po;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "im_message", indexes = {
        @Index(name = "idx_conv_time", columnList = "conversationId,createdAt")
})
public class MessagePO {
    @Id
    private String messageId;
    private String conversationId;
    private String senderId;
    @Column(length = 5000)
    private String content;
    private String type;      // TEXT / IMAGE / SYSTEM
    private String status;    // SENT / DELIVERED / READ
    private Instant createdAt;
}
