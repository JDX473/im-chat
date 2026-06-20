package com.im.chat.domain.message;

import com.im.chat.common.UserId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Message aggregate — aligns with webchat's web_chat_message.
 * Private chat: sender=A, receiver=B.
 * Group chat: sender=A, receiver=groupId, proxySender sets the real sender.
 */
@Getter
@Setter
public class Message {

    private Long id;
    private UserId senderId;
    private UserId proxySenderId;   // group proxy sender
    private UserId receiverId;
    private Integer type;           // ChatMessageTypeEnum: 1=TEXT, 2=FILE, 3=RED_PACKET, 4=ARTICLE, 5=CARD
    private String content;
    private Boolean isRead;
    private Instant sendDate;
    private Instant updateDate;

    public Message() {}

    /** Create a private chat message. */
    public static Message createPrivate(UserId sender, UserId receiver, String content, Integer type) {
        Message m = new Message();
        m.senderId = sender;
        m.receiverId = receiver;
        m.content = content;
        m.type = type != null ? type : 1;  // default TEXT
        m.isRead = false;
        m.sendDate = Instant.now();
        return m;
    }

    /** Create a group chat message (proxy sender pattern). */
    public static Message createGroup(UserId sender, UserId groupId, String content, Integer type) {
        Message m = new Message();
        m.senderId = sender;
        m.receiverId = groupId;
        m.content = content;
        m.type = type != null ? type : 1;
        m.isRead = false;
        m.sendDate = Instant.now();
        return m;
    }

    /** Create a system message. */
    public static Message system(UserId receiverId, String content) {
        Message m = new Message();
        m.senderId = null;
        m.receiverId = receiverId;
        m.content = content;
        m.type = 1;
        m.isRead = false;
        m.sendDate = Instant.now();
        return m;
    }
}
