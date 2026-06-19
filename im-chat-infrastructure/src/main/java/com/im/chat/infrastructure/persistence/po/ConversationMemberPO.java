package com.im.chat.infrastructure.persistence.po;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "im_conversation_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"conversationId", "userId"})
})
public class ConversationMemberPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conversationId;
    private String userId;
    private String role;      // OWNER / ADMIN / MEMBER
    private Instant joinedAt;
}
