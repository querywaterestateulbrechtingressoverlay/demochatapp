package com.qweuio.chat.core;

import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WebSocketChatMessagingService implements ChatMessagingService<String, String> {
  public enum UserRole {
    NOT_A_MEMBER, MEMBER, ADMIN
  }
  @Autowired
  MessagePersistingService msgPersistingService;
  @Autowired
  private ChatroomRepository chatroomRepo;
  @Autowired
  private ChatUserRepository chatUserRepo;
  @Autowired
  private ChatMessageRepository chatMsgRepo;
  @Autowired
  private MongoTemplate mongoTemplate;

  public boolean verifyUserRole(String userId, String chatroomId, UserRole role) {
    if (!chatUserRepo.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    return chatroomRepo
      .getUserRoleFromChatroomById(chatroomId, userId)
      .map(userRoleEntity -> UserRole.valueOf(userRoleEntity.role()))
      .orElse(UserRole.NOT_A_MEMBER)
      .equals(role);
  }
  public List<Chatroom> getUserChatrooms(String userId) {
    return chatUserRepo.getChatroomsByUser(userId);
  }
  public List<ProcessedMessageDTO> getChatroomRecentHistory(String requestingUserId, String chatroomId) {
    if (verifyUserRole(requestingUserId, chatroomId, UserRole.NOT_A_MEMBER)) {
      throw new InsufficientPermissionsException(requestingUserId);
    }
    return msgPersistingService.getRecentMessages(chatroomId);
  }

  @Override
  public void addUserToChatroom(String addingUser, String userToAdd, String chatroomId) {
    if (!verifyUserRole(addingUser, chatroomId, UserRole.ADMIN)) {
      throw new InsufficientPermissionsException(addingUser);
    }
    ChatUser chatUser = chatUserRepo.findById(userToAdd).orElseThrow(() -> new UserNotFoundException(userToAdd));
    if (chatUser.chatrooms().size() >= 100) {
      throw new TooManyChatroomsException(userToAdd);
    }
    Chatroom chatroom = chatroomRepo.findById(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(chatroomId));
    if (chatroom.users().size() >= 100) {
      throw new TooManyUsersException(chatroomId);
    }
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userToAdd)),
      new Update().addToSet("chatrooms", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userToAdd)),
      new Update().addToSet("users", new UserWithRoleEntity(userToAdd, "MEMBER")),
      "chatrooms");
  }
  @Override
  public void removeUserFromChatroom(String removingUser, String userToRemove, String chatroomId) {
    if (getUserRole(removingUser, chatroomId) != UserRole.ADMIN || getUserRole(userToRemove, chatroomId) == UserRole.ADMIN) {
      throw new InsufficientPermissionsException(removingUser);
    }

    Optional<UserWithRoleEntity> kickTarget = chatroomRepo.getUserRoleFromChatroomById(chatroomId, userToRemove);

    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(kickTarget.get().userId())),
      new Update().pull("chatrooms", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(chatroomId)),
      new Update().pull("users", Query.query(Criteria.where("userId").is(kickTarget.get().userId()))),
      "chatrooms");
  }
  @Override
  public String createChatroom(String creatorId, String chatroomName) {
    ChatUser creator = chatUserRepo.findById(creatorId).orElseThrow(() -> new RuntimeException("a ghost is trying to create a new chatroom"));
    if (creator.chatrooms().size() >= 100) {
      throw new TooManyChatroomsException(creatorId);
    }

    String newChatroomId = chatroomRepo.save(
      new Chatroom(
        null, chatroomName,
        List.of(new UserWithRoleEntity(creatorId, "ADMIN")),
        Collections.emptyList())).id();
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(creatorId)),
      new Update().addToSet("chatrooms", newChatroomId),
      "users");
    return newChatroomId;
  }
  @Override
  public void deleteChatroom(String deletingUserId, String chatroomId) {
    if (!chatroomRepo.existsById(chatroomId)) {
      throw new ChatroomAccessException(deletingUserId, chatroomId);
    }
    if (getUserRole(deletingUserId, chatroomId) != UserRole.ADMIN) {
      throw new InsufficientPermissionsException(deletingUserId);
    }

    List<String> chatUsers = chatroomRepo
      .getUsersByChatroom(chatroomId)
      .stream()
      .map(ChatUser::id)
      .toList();

    mongoTemplate.updateMulti(
      new Query(Criteria.where("_id").in(chatUsers)),
      new Update().pull("chatrooms", chatroomId),
      "users");
    chatroomRepo.deleteById(chatroomId);
  }

  @Override
  public List<ChatUser> getChatroomUsers(String requestingUserId, String chatroomId) {
    if (getUserRole(requestingUserId, chatroomId) == UserRole.NOT_A_MEMBER) {
      throw new InsufficientPermissionsException(requestingUserId);
    }
    return getChatroomUsers(chatroomId);
  }

  @Override
  public List<ChatUser> getChatroomUsers(String chatroomId) {
    return chatroomRepo.getUsersByChatroom(chatroomId);
  }
}
