package com.qweuio.chat.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("chatroomIds")
public record Chatroom(
    @Id
    Integer id,
    String name,
    List<Integer> userIds
) {
}
