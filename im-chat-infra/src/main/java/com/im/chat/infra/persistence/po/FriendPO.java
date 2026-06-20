package com.im.chat.infra.persistence.po;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * Maps to webchat's web_chat_friend table.
 */
@Data
@Entity
@Table(name = "web_chat_friend")
public class FriendPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "friend_id", nullable = false)
    private String friendId;

    @Column(name = "apply_date")
    private Date applyDate;

    @Column(name = "handle_date")
    private Date handleDate;

    private Integer status;
    private String remark;

    @Version
    private Integer version;
}
