package com.qweuio.chat.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document("users")
public record User(
    @Id
    Integer id,
    String name,
    List<Integer> chatroomIds
) {
}
