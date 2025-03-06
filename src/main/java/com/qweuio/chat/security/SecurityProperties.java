package com.qweuio.chat.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties("chatapp.security")
public record SecurityProperties(RSAPrivateKey rsaPrivate, RSAPublicKey rsaPublic) {
}
