package com.qweuio.chat.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record User(
    @Id
    Integer id,
    String name
) {
}
