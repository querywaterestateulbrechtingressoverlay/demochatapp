package com.qweuio.chat.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class AppSecurityConfig {
  @Autowired
  SecurityProperties securityProperties;

  @Bean
  @Order(1)
  SecurityFilterChain securityConfigCsrfEndpoint(HttpSecurity http) throws Exception {
    return http
      .securityMatchers((r) -> r.requestMatchers("/csrf/**"))
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(
        (auth) -> auth
          .requestMatchers(HttpMethod.GET, "/csrf").permitAll())
      .build();
  }

  @Bean
  @Order(1)
  SecurityFilterChain securityConfigAuth(HttpSecurity http) throws Exception {
    return http
      .securityMatchers((r) -> r.requestMatchers("/token/**", "/websocket/**"))
      .csrf(AbstractHttpConfigurer::disable)
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(
        (auth) -> auth
          .requestMatchers(HttpMethod.POST, "/token").authenticated()
          .requestMatchers(HttpMethod.GET, "/websocket").permitAll())
      .httpBasic(Customizer.withDefaults())
      .build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain securityConfigMain(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers(HttpMethod.GET, "/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/ping").access(hasScope("chat"))
        .requestMatchers(HttpMethod.GET, "/**", "/js/**", "/css/**").access(hasScope("chat"))
      )
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .exceptionHandling((exceptions) -> exceptions
        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
      )
      .build();
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(securityProperties.rsaPublic()).build();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey.Builder(securityProperties.rsaPublic()).privateKey(securityProperties.rsaPrivate()).build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSource);
  }
  @Bean
  UserDetailsManager uds() {
    return new CustomUserDetailsService();
  }

}
