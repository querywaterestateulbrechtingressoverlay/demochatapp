package com.qweuio.chat.core;

import com.qweuio.chat.core.exception.chatapp.ChatroomNotFoundException;
import com.qweuio.chat.core.exception.chatapp.UserIdNotFoundException;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.*;
import com.qweuio.chat.persistence.repository.UserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.persistence.repository.ChatroomUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChatService {
  Logger logger = LoggerFactory.getLogger(ChatService.class);
  @Autowired
  protected MessagePersistingService msgPersistingService;
  @Autowired
  protected ChatroomRepository chatroomRepo;
  @Autowired
  protected ChatroomUserRepository chatroomUserRepo;
  @Autowired
  protected UserRepository userRepo;

  public UserRole getUserRole(UUID chatroomId, UUID userId) {
    return chatroomUserRepo
        .findById(new ChatroomAndUser(chatroomId, userId))
        .orElseThrow(() -> new ChatroomNotFoundException(chatroomId))
        .role();
  }

  public List<Chatroom> getUserChatrooms(UUID userId) {
    return chatroomRepo.findChatroomsByUserId(userId);
  }

  public Chatroom createChatroom(UUID creatorId, String chatroomName) {
    Chatroom newChatroom = chatroomRepo.save(new Chatroom(null, chatroomName));
    ChatroomUsers creator = ChatroomUsers.fromData(newChatroom.id(), creatorId, UserRole.ADMIN);
    chatroomUserRepo.insert(creator);

    return newChatroom;
  }

  public void deleteChatroom(UUID chatroomId) {
    if (!chatroomRepo.existsById(chatroomId)) throw new ChatroomNotFoundException(chatroomId);

    chatroomUserRepo.deleteAll(chatroomUserRepo.findByChatroomId(chatroomId));
    chatroomRepo.deleteById(chatroomId);
  }

  public Optional<User> findUserByUsername(String username) {
    return userRepo.findByUsername(username);
  }

  public void modifyUserRole(UUID chatroomId, UUID userId, UserRole newUserRole) {
    if (!chatroomUserRepo.existsById(new ChatroomAndUser(chatroomId, userId))) throw new UserIdNotFoundException(userId);
    chatroomUserRepo.update(ChatroomUsers.fromData(chatroomId, userId, newUserRole));
  }

  public void addUserToChatroom(UUID chatroomId, UUID userId, UserRole role) {
    chatroomUserRepo.insert(ChatroomUsers.fromData(chatroomId, userId, role));
  }

  public void removeUserFromChatroom(UUID chatroomId, UUID userId) {
    chatroomUserRepo.deleteById(new ChatroomAndUser(chatroomId, userId));
  }

  public List<User> getChatroomUsers(UUID chatroomId) {
    return userRepo.findUsersByChatroomId(chatroomId);
  }
}
