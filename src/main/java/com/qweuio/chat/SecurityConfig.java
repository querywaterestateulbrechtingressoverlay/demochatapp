package com.qweuio.chat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain chatAppSecurity(HttpSecurity http) throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> {
        auth
          .anyRequest().authenticated();
      })
      .httpBasic(Customizer.withDefaults())
      .build();
  }
  @Bean
  UserDetailsService userDetailsService() {
    UserDetailsManager uds = new InMemoryUserDetailsManager();
    uds.createUser(User.builder().username("user-1").password("{noop}password1").authorities("chat-user").build());
    uds.createUser(User.builder().username("user-2").password("{noop}password2").authorities("chat-user").build());
    return uds;
  }
}
