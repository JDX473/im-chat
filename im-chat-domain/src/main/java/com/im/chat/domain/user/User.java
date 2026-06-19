package com.im.chat.domain.user;

import com.im.chat.common.UserId;
import lombok.Getter;

/**
 * User aggregate root.
 */
@Getter
public class User {

    private UserId userId;
    private String nickname;
    private String avatar;
    private boolean online;

    // For persistence reconstruction
    protected User() {}

    public User(UserId userId, String nickname, String avatar) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.online = false;
    }

    public void updateProfile(String nickname, String avatar) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            this.avatar = avatar;
        }
    }

    public void markOnline() {
        this.online = true;
    }

    public void markOffline() {
        this.online = false;
    }
}
