package com.qweuio.chat.security.dto;

public record JWTokenDTO(
    String token,
    long expirySeconds
) {
}
