package com.qweuio.chat.persistence.entity;

public record UserWithRoleEntity(
  String userId,
  UserRole role
) {
  public enum UserRole {
    NOT_A_MEMBER, MEMBER, ADMIN
  }
}
