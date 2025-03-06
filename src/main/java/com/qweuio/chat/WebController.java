package com.qweuio.chat;

import com.qweuio.chat.persistence.Chatroom;
import com.qweuio.chat.persistence.ChatroomRepository;
import com.qweuio.chat.persistence.User;
import com.qweuio.chat.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@CrossOrigin
@RestController
public class WebController {
  Logger logger = LoggerFactory.getLogger(WebController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  UserRepository userRepo;
  @Autowired
  UserDetailsManager userDetailsService;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @GetMapping("/ping")
  String pong() {
    return "pong";
  }
  @GetMapping("/init")
  void init() {
    User user1 = new User(1, "user-1", List.of(1));
    User user2 = new User(2, "user-2", List.of(1, 2));
    User user3 = new User(3, "user-3", List.of(1, 2, 3));
    User user4 = new User(4, "user-4", List.of(2, 3));
    User user5 = new User(5, "user-5", List.of(3));
    Chatroom chat1 = new Chatroom(1, "chatroom-1", List.of(1, 2, 3));
    Chatroom chat2 = new Chatroom(2, "chatroom-2", List.of(2, 3, 4));
    Chatroom chat3 = new Chatroom(3, "chatroom-3", List.of(3, 4, 5));
    userRepo.saveAll(List.of(user1, user2, user3, user4, user5));
    chatroomRepo.saveAll(List.of(chat1, chat2, chat3));
    for (int i = 1; i <= 5; i++) {
      userDetailsService.createUser(org.springframework.security.core.userdetails.User.builder()
        .username(String.valueOf(i))
        .password(encoder.encode("password"))
        .authorities(new SimpleGrantedAuthority("chat"))
        .build());
    }
  }
  @GetMapping("/mychatrooms")
  List<Chatroom> getAvailableChatrooms(Principal principal) {
    return userRepo.findById(Integer.valueOf(principal.getName()))
        .orElseThrow(() -> new RuntimeException("wtf"))
        .chatroomIds().stream().map((id) -> chatroomRepo.findById(id).get()).toList();
  }
}
