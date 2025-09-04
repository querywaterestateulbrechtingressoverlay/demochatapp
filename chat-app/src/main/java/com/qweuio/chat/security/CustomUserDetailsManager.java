package com.qweuio.chat.security;

import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.repository.UserRepository;
import com.qweuio.chat.security.data.*;
import org.slf4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;


public class CustomUserDetailsManager implements UserDetailsManager {
  private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsManager.class);
  @Autowired
  private LoginDataRepository loginDataRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserAuthoritiesRepository userAuthoritiesRepository;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug("loading user {}", username);
    var loginData = loginDataRepository.findByLoginData(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " was not found"));
    var authorities = userAuthoritiesRepository
        .findByUserId(loginData.userId()).stream()
        .map(UserAuthorities::userAuthority)
        .map(UserAuthority::authority)
        .map(UserAuthority.Authority::toString)
        .map(SimpleGrantedAuthority::new)
        .toList();
    return org.springframework.security.core.userdetails.User.builder()
      .username(loginData.userId().toString())
      .password(loginData.encodedValue())
      .authorities(authorities)
      .build();
  }

  @Override
  public void createUser(UserDetails userDetails) {
    User user = userRepository.save(new User(null, userDetails.getUsername()));
    loginDataRepository.insert(new LoginData(user.id(), user.username(), userDetails.getPassword()));
    userDetails.getAuthorities()
        .stream()
        .map((auth) ->
          new UserAuthorities(
            new UserAuthority(user.id(), UserAuthority.Authority.valueOf(auth.getAuthority()))))
        .forEach((ua)-> userAuthoritiesRepository.insert(ua));
  }

  @Override
  public void updateUser(UserDetails user) {
    var loginData = loginDataRepository
        .findByLoginData(user.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("error while updating: username " + user.getUsername() + " not found"));

    loginDataRepository.save(new LoginData(loginData.userId(), user.getUsername(), user.getPassword()));

  }

  @Override
  public void deleteUser(String userId) {
    if (!loginDataRepository.existsById(UUID.fromString(userId))) {
      throw new UsernameNotFoundException("error while deleting: user with id " + userId + " not found");
    } else {
      loginDataRepository.deleteById(UUID.fromString(userId));
    }
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
  }

  @Override
  public boolean userExists(String username) {
    return userRepository.findByUsername(username).isPresent();
  }
}
