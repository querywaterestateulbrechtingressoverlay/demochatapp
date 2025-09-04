package com.qweuio.chat.security;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.security.data.*;
import com.qweuio.chat.security.data.UserCredentials;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class CustomUserDetailsService implements UserDetailsManager {
  private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
  @Autowired
  private LoginDataRepository loginDataRepository;
  @Autowired
  private ChatUserRepository userRepository;
  @Autowired
  private UserAuthoritiesRepository userAuthoritiesRepository;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug("loading user {}", username);
    var loginData = loginDataRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " was not found"));
    var authorities = userAuthoritiesRepository
        .findByUserId(loginData.userId()).stream()
        .map(UserAuthorities::userAuthority)
        .map(UserAuthority::authority)
        .map(UserAuthority.Authority::toString)
        .map(SimpleGrantedAuthority::new)
        .toList();
    return User.builder()
      .username(loginData.userId().toString())
      .password(loginData.encodedData())
      .authorities(authorities)
      .build();
  }

  @Override
  public void createUser(UserDetails userDetails) {
    ChatUser user = userRepository.save(new ChatUser(null, userDetails.getUsername()));
    loginDataRepository.save(new LoginData(user.id(), user.name(), userDetails.getPassword()));
    userAuthoritiesRepository.saveAll(userDetails.getAuthorities()
        .stream()
        .map((auth) ->
          new UserAuthorities(
            new UserAuthority(user.id(), UserAuthority.Authority.valueOf(auth.getAuthority()))))
        .toList());
  }

  @Override
  public void updateUser(UserDetails user) {
    var loginData = loginDataRepository
        .findByUsername(user.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("error while updating: username " + user.getUsername() + " not found"));

    loginDataRepository.save(new LoginData(loginData.userId(), user.getUsername(), user.getPassword()));

  }

  @Override
  public void deleteUser(String userId) {
    if (!loginDataRepository.existsById(userId)) {
      throw new UsernameNotFoundException("error while deleting: user with id " + userId + " not found");
    } else {
      loginDataRepository.deleteById(userId);
    }
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
  }

  @Override
  public boolean userExists(String username) {
    return userRepository.findByName(username).isPresent();
  }
}
