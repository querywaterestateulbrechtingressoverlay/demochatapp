/*
package com.qweuio.chat.websocket.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.*;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.Builder.Constraint;
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {
  @Bean
  AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
    messages
      .simpDestMatchers("/user/**").hasRole("USER");

    return messages.build();
  }
}
*/
