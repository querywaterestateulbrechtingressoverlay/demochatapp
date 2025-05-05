package com.qweuio.chat.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Autowired
  JwtDecoder jwtDecoder;
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/messages", "/system", "/user");
    registry.setApplicationDestinationPrefixes("/chat");
  }
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/websocket").setAllowedOrigins("*");
  }
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
      new ExecutorChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          String authHeader = null;
          List<String> ah = accessor.getNativeHeader("Authorization");
          if (ah != null) {
            authHeader = ah.getFirst();
          }
          Jwt token = jwtDecoder.decode(authHeader);
          OAuth2TokenValidatorResult validatorResult = JwtValidators.createDefault().validate(token);
          if (validatorResult.hasErrors()) {
            throw new JwtValidationException("errors were found during the validation of Authorization header", validatorResult.getErrors());
          } else {
            JwtAuthenticationConverter c = new JwtAuthenticationConverter();
            Authentication auth = c.convert(token);
            accessor.setUser(auth);
          }
        }
        return message;
      }
    });
  }
}

