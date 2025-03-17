package com.qweuio.chat.core;

import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.ChatUserListDTO;
import com.qweuio.chat.websocket.dto.Converters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class MongoAndWebSocketChatMessagingService implements ChatMessagingService<String, String> {
  enum UserRole {
    NOT_A_MEMBER, MEMBER, ADMIN
  }
  @Autowired
  private ChatroomRepository chatroomRepo;
  @Autowired
  private ChatUserRepository chatUserRepo;
  @Autowired
  private ChatMessageRepository chatMsgRepo;
  @Autowired
  private MongoTemplate mongoTemplate;


  private UserRole getUserRole(String userId, String chatroomId) {
    if (!chatUserRepo.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    Optional<UserWithRoleEntity> role = chatroomRepo.getUserRoleFromChatroomById(chatroomId, userId);
    return role.map(userRoleEntity -> UserRole.valueOf(userRoleEntity.role())).orElse(UserRole.NOT_A_MEMBER);
  }

  public void addUserToChatroom(String addingUser, String userToAdd, String chatroomId) {
    if (getUserRole(addingUser, chatroomId) != UserRole.ADMIN) {
      throw new InsufficientPermissionsException(addingUser);
    }
    ChatUser chatUser = chatUserRepo.findById(userToAdd).orElseThrow(() -> new UserNotFoundException(userToAdd));
    if (chatUser.chatroomIds().size() >= 100) {
      throw new TooManyChatroomsException(userToAdd);
    }
    Chatroom chatroom = chatroomRepo.findById(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(chatroomId));
    if (chatroom.users().size() >= 100) {
      throw new TooManyUsersException(chatroomId);
    }
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userToAdd)),
      new Update().addToSet("chatroomIds", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userToAdd)),
      new Update().addToSet("users", new UserWithRoleEntity(userToAdd, "MEMBER")),
      "chatrooms");
  }
  public void removeUserFromChatroom(String removingUser, String userToRemove, String chatroomId) throws UserNotFoundException, ChatroomNotFoundException {
    if (getUserRole(removingUser, chatroomId) != UserRole.ADMIN || getUserRole(userToRemove, chatroomId) == UserRole.ADMIN) {
      throw new InsufficientPermissionsException(removingUser);
    }
  }
  public void getUserChatrooms(String userId) throws UserNotFoundException, ChatroomNotFoundException {

  }
  public void createChatroom(String creatorId, String chatroomName) throws UserNotFoundException, ChatroomNotFoundException {

  }
  public void deleteChatroom(String deletingUserId, String chatroomId) throws UserNotFoundException, ChatroomNotFoundException {

  }
  public void getChatroomUsers(String chatroomId) throws UserNotFoundException, ChatroomNotFoundException {

  }
}
