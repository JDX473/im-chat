package com.im.chat.infrastructure.persistence.po;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "im_conversation")
public class ConversationPO {
    @Id
    private String conversationId;
    private String type;      // PRIVATE / GROUP
    private String name;
    private String avatar;
    private Instant createdAt;
    private Instant updatedAt;
}
