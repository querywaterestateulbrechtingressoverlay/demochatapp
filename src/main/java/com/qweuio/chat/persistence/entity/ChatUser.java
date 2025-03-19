package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document("users")
public record ChatUser(
    @Id
    String id,
    String name,
    List<String> chatrooms
) {
}
