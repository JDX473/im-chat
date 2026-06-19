package com.im.chat.infra.persistence.po;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "im_friend", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "friendId"})
})
public class FriendPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String friendId;
    private String status;    // PENDING / ACCEPTED / BLOCKED / DELETED
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;
}
