package com.qweuio.chat.security.dto;

public record JWTokenDTO(
    String token,
    Integer id,
    long expirySeconds
) {
}
