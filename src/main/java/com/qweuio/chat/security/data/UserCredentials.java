package com.qweuio.chat.security.data;

import org.springframework.data.annotation.Id;

import java.util.List;

public record UserCredentials(
    @Id
    String id,
    String password,
    List<String> authorities
) {
}
