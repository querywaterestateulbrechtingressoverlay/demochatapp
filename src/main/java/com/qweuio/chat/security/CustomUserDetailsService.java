package com.qweuio.chat.security;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.security.data.UserCredentials;
import com.qweuio.chat.security.data.UserCredentialsRepository;
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


public class CustomUserDetailsService implements UserDetailsManager {
  private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
  @Autowired
  private UserCredentialsRepository userCredentialsRepository;
  @Autowired
  private ChatUserRepository userRepository;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.info("loading user {}", username);
    var userInfo = userRepository.findByName(username)
        .orElseThrow(() -> new UsernameNotFoundException("user " + username + " was not found"));
    var userCredentials = userCredentialsRepository.findById(userInfo.id()).orElseThrow(RuntimeException::new);
    return User.builder()
      .username(userCredentials.id())
      .password(userCredentials.password())
      .authorities(userCredentials.authorities().stream().map(SimpleGrantedAuthority::new).toList())
      .build();
  }

  @Override
  public void createUser(UserDetails userDetails) {
    ChatUser user = userRepository.save(new ChatUser(null, userDetails.getUsername(), Collections.emptyList()));
    userCredentialsRepository.insert(new UserCredentials(user.id(), userDetails.getPassword(), userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
  }

  @Override
  public void updateUser(UserDetails user) {
    var userCredentials = userCredentialsRepository
        .findById(user.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("error while updating: username " + user.getUsername() + " not found"));
    userCredentialsRepository.save(new UserCredentials(userCredentials.id(), user.getPassword(), user.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList()));
  }

  @Override
  public void deleteUser(String userId) {
    if (!userCredentialsRepository.existsById(userId)) {
      throw new UsernameNotFoundException("error while deleting: user with id " + userId + " not found");
    } else {
      userCredentialsRepository.deleteById(userId);
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
