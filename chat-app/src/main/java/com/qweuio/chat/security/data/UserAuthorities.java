package com.qweuio.chat.security.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_authorities")
public record UserAuthorities(
    @Id
    UserAuthority userAuthority
) {
}
