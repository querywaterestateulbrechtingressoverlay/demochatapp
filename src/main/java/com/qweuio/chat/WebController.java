package com.qweuio.chat;

import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
public class WebController {
  Logger logger = LoggerFactory.getLogger(WebController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  ChatUserRepository userRepo;
  @Autowired
  UserDetailsManager userDetailsService;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @GetMapping("/ping")
  String pong() {
    return "pong";
  }
  @GetMapping("/mockinit")
  void init() {
    ChatUser user1 = new ChatUser("1", "user-1", List.of("1"));
    ChatUser user2 = new ChatUser("2", "user-2", List.of("1", "2"));
    ChatUser user3 = new ChatUser("3", "user-3", List.of("1", "2", "3"));
    ChatUser user4 = new ChatUser("4", "user-4", List.of("2", "3"));
    ChatUser user5 = new ChatUser("5", "user-5", List.of("3"));
    Chatroom chat1 = new Chatroom("1", "chatroom-1", List.of(new UserWithRoleEntity("1", "ADMIN"), new UserWithRoleEntity("2", "MEMBER"), new UserWithRoleEntity("3", "MEMBER")), Collections.emptyList());
    Chatroom chat2 = new Chatroom("2", "chatroom-2", List.of(new UserWithRoleEntity("2", "ADMIN"), new UserWithRoleEntity("3", "MEMBER"), new UserWithRoleEntity("4", "MEMBER")), Collections.emptyList());
    Chatroom chat3 = new Chatroom("3", "chatroom-3", List.of(new UserWithRoleEntity("3", "ADMIN"), new UserWithRoleEntity("4", "MEMBER"), new UserWithRoleEntity("5", "MEMBER")), Collections.emptyList());
    userRepo.saveAll(List.of(user1, user2, user3, user4, user5));
    chatroomRepo.saveAll(List.of(chat1, chat2, chat3));
    for (int i = 1; i <= 5; i++) {
      userDetailsService.createUser(User.builder()
        .username(String.valueOf(i))
        .password(encoder.encode("password"))
        .authorities(new SimpleGrantedAuthority("chat"))
        .build());
    }
  }
}
