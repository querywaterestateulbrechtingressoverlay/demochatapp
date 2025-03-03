package com.qweuio.chat;

public record SuperMessage(
  String recipient,
  Message message
) {
}
