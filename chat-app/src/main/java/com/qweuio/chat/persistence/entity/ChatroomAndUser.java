package com.qweuio.chat.persistence.entity;

import java.util.UUID;

public record ChatroomAndUser(
    UUID chatroomId,
    UUID userId
) {
}
