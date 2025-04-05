package com.qweuio.chat.core;

import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.core.exception.chatapp.*;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.MessageRequestDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MongoChatMessagingService implements ChatMessagingService<String, String> {
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
  private MongoTemplate mongoTemplate;

  public boolean verifyUserRole(String userId, String chatroomId, UserRole role) {
    return chatroomRepo
      .getUserRoleFromChatroomById(chatroomId, userId)
      .map(userRoleEntity -> UserRole.valueOf(userRoleEntity.role()))
      .orElse(UserRole.NOT_A_MEMBER)
      .equals(role);
  }

  public List<Chatroom> getUserChatrooms(String userId) {
    return chatUserRepo.getChatroomsByUser(userId);
  }

  public List<ChatMessage> getChatroomRecentHistory(String requestingUserId, String chatroomId) {
    try {
      if (verifyUserRole(requestingUserId, chatroomId, UserRole.NOT_A_MEMBER)) {
        throw new InsufficientPermissionsException("get recent history in chatroom " + chatroomId);
      }
    } catch (ChatAppException e) {
      throw new UserActionException(requestingUserId, e.getMessage(), e);
    }
    return msgPersistingService.getRecentMessages(chatroomId);
  }

  @Override
  public ChatUser getUserInfo(String userId) {
    return chatUserRepo.findById(userId).orElseThrow();
  }

  @Override
  public ChatMessage sendMessage(String senderId, String chatroomId, MessageRequestDTO message) {
    try {
      if (!chatroomRepo.existsById(chatroomId)) {
        throw new ChatroomNotFoundException(chatroomId);
      }
      if (verifyUserRole(senderId, chatroomId, UserRole.NOT_A_MEMBER)) {
        throw new InsufficientPermissionsException("send message to chatroom " + chatroomId);
      }
      return msgPersistingService.persistMessage(new MessageDTO(null, senderId, chatroomId, Instant.now(), message.message()));
    } catch (ChatAppException e) {
      throw new UserActionException(senderId, e.getMessage(), e);
    }
  }

  @Override
  public Chatroom addUserToChatroom(String addingUser, String userToAdd, String chatroomId) {
    try {
      if (!verifyUserRole(addingUser, chatroomId, UserRole.ADMIN)) {
        throw new InsufficientPermissionsException("add user " + userToAdd + " to chatroom " + chatroomId);
      }
      if (chatUserRepo.getChatroomCount(userToAdd).orElseThrow(() -> new UserNotFoundException(userToAdd)) >= 100) {
        throw new TooManyChatroomsException(userToAdd, chatroomId);
      }
      if (chatroomRepo.getUserCount(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(chatroomId)) >= 100) {
        throw new TooManyUsersException(userToAdd, chatroomId);
      }
      addUserToChatroom(chatroomId, userToAdd);
      Optional<Chatroom> chatroom = chatroomRepo.findById(chatroomId);
      assert chatroom.isPresent();
      return chatroom.get();
    } catch (ChatAppException e) {
      throw new UserActionException(addingUser, e.getMessage(), e);
    }
  }
  @Override
  public void removeUserFromChatroom(String removingUser, String userToRemove, String chatroomId) {
    try {
      if (!verifyUserRole(removingUser, chatroomId, UserRole.ADMIN) || verifyUserRole(userToRemove, chatroomId, UserRole.ADMIN)) {
        throw new InsufficientPermissionsException("remove user " + userToRemove + " from chatroom " + chatroomId);
      }
      removeUserFromChatroom(chatroomId, userToRemove);
    } catch (ChatAppException e) {
      throw new UserActionException(removingUser, e.getMessage(), e);
    }
  }
  @Override
  public Chatroom createChatroom(String creatorId, String chatroomName) {
    try {
      ChatUser creator = chatUserRepo.findById(creatorId).orElseThrow(() -> new RuntimeException("a ghost is trying to create a new chatroom"));
      if (creator.chatrooms().size() >= 100) {
        throw new TooManyChatroomsException(creatorId, null);
      }
      Chatroom newChatroom = chatroomRepo.save(
        new Chatroom(
          null, chatroomName,
          List.of(new UserWithRoleEntity(creatorId, "ADMIN")),
          Collections.emptyList()
        )
      );
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(creatorId)),
        new Update().addToSet("chatrooms", newChatroom.id()),
        "users");
      return newChatroom;
    } catch (ChatAppException e) {
      throw new UserActionException(creatorId, e.getMessage(), e);
    }
  }
  @Override
  public void deleteChatroom(String deletingUserId, String chatroomId) {
    try {
      if (!chatroomRepo.existsById(chatroomId)) {
        throw new ChatroomNotFoundException(chatroomId);
      }
      if (!verifyUserRole(deletingUserId, chatroomId, UserRole.ADMIN)) {
        throw new InsufficientPermissionsException("delete chatroom " + chatroomId);
      }
      deleteChatroom(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(deletingUserId, e.getMessage(), e);
    }
  }

  @Override
  public List<ChatUser> getChatroomUsers(String requestingUserId, String chatroomId) {
    try {
      if (verifyUserRole(requestingUserId, chatroomId, UserRole.NOT_A_MEMBER)) {
        throw new InsufficientPermissionsException("get user list of chatroom " + chatroomId);
      }
      return getChatroomUsers(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(requestingUserId, e.getMessage(), e);
    }
  }

  @Override
  public String createChatroom(String chatroomName) {
    return chatroomRepo.save(
      new Chatroom(
        null, chatroomName,
        Collections.emptyList(),
        Collections.emptyList()))
      .id();
  }

  @Override
  public void deleteChatroom(String chatroomId) {
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
  public void addUserToChatroom(String chatroomId, String userId) {
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userId)),
      new Update().addToSet("chatrooms", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userId)),
      new Update().addToSet("users", new UserWithRoleEntity(userId, "MEMBER")),
      "chatrooms");
  }

  @Override
  public void removeUserFromChatroom(String chatroomId, String userId) {
    UserWithRoleEntity kickTarget = chatroomRepo
      .getUserRoleFromChatroomById(chatroomId, userId)
      .orElseThrow(() -> new UserNotFoundException(userId));

    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(kickTarget.userId())),
      new Update().pull("chatrooms", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(chatroomId)),
      new Update().pull("users", Query.query(Criteria.where("userId").is(kickTarget.userId()))),
      "chatrooms");
  }

  @Override
  public List<ChatUser> getChatroomUsers(String chatroomId) {
    return chatroomRepo.getUsersByChatroom(chatroomId);
  }
}
