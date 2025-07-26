package com.qweuio.chat.security;

import com.qweuio.chat.security.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class SecurityController {
  private final Logger logger = LoggerFactory.getLogger(SecurityController.class);
  private final long expirySeconds = 360000L;
  private final PasswordEncoder passwordEncoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Autowired
  JwtEncoder encoder;
  @Autowired
  UserDetailsManager customUDS;

  @CrossOrigin
  @PostMapping("/token")
  public JWTokenDTO login(Authentication authentication) {
    String scope = authentication.getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.joining(" "));
    Instant now = Instant.now();
    Instant expiryInstant = now.plusSeconds(expirySeconds);
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer("self")
      .issuedAt(now)
      .expiresAt(expiryInstant)
      .subject(authentication.getName())
      .claim("scope", scope)
      .build();
    return new JWTokenDTO(encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), authentication.getName(), expirySeconds);
  }

  @CrossOrigin
  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody LoginInfoDTO loginInfoDTO) {
    if (!customUDS.userExists(loginInfoDTO.login())) {
      customUDS.createUser(User.builder()
        .username(loginInfoDTO.login())
        .password(loginInfoDTO.password())
        .passwordEncoder(passwordEncoder::encode)
        .authorities("SCOPE_chat")
        .build());
      return ResponseEntity.ok("registration successful");
    } else {
      return new ResponseEntity<>("user with the provided username already exists", HttpStatus.BAD_REQUEST);
    }
  }
}
