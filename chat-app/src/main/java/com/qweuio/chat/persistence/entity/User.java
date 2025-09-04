package com.qweuio.chat.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("users")
public record ChatUser(
    @Id
    UUID id,
    String name
) {
}
