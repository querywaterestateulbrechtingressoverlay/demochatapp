package com.qweuio.chat.security.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("login_data")
public record LoginData(
  @Id
  UUID userId,
  String loginData,
  String encodedValue
) {
}
