package com.im.chat.infra.persistence.po;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * Maps to webchat's web_chat_group_user table exactly.
 */
@Data
@Entity
@Table(name = "web_chat_group_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
public class GroupUserPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    /** 0=normal, 1=owner, 2=admin */
    private Integer status;

    // BaseEntity fields
    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "update_date")
    private Date updateDate;

    @Version
    private Integer version;
}
