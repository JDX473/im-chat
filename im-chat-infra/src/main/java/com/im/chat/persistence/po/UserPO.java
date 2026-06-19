package com.im.chat.infra.persistence.po;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "im_user")
public class UserPO {
    @Id
    private String userId;
    private String nickname;
    private String avatar;
    private boolean online;
}
