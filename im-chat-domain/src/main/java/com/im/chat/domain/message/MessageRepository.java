package com.im.chat.domain.message;

import java.util.List;

/**
 * Repository port for messages (web_chat_message).
 */
public interface MessageRepository {

    Message save(Message message);

    /** Private chat history between two users. */
    List<Message> findPrivateMessages(String userA, String userB, int page, int size);

    /** Group chat history. */
    List<Message> findGroupMessages(String groupId, int page, int size);
}
