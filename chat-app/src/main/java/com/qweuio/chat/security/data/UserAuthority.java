package com.qweuio.chat.security.data;

import java.util.UUID;

public record UserAuthority(
    UUID userId,
    Authority authority
) {
  public enum Authority {
    SCOPE_chat
  }
}
