package com.im.chat.infra.persistence.po;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * Maps to webchat's web_chat_message table.
 */
@Data
@Entity
@Table(name = "web_chat_message", indexes = {
        @Index(name = "idx_sender_receiver", columnList = "sender,receiver")
})
public class MessagePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** sender userId */
    private String sender;

    /** group proxy sender (null for P2P) */
    @Column(name = "proxy_sender")
    private String proxySender;

    /** receiver userId or groupId */
    private String receiver;

    /** 1=text, 2=image, 3=video, 4=file, 5=card */
    private Integer type;

    private String message;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "send_date")
    private Date sendDate;

    @Column(name = "update_date")
    private Date updateDate;

    @Version
    private Integer version;

    @PrePersist
    void prePersist() {
        if (sendDate == null) sendDate = new Date();
        if (isRead == null) isRead = false;
    }

    @PreUpdate
    void preUpdate() {
        updateDate = new Date();
    }
}
