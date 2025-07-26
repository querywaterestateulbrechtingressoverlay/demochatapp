package com.qweuio.chat.core;

import com.mongodb.client.result.UpdateResult;
import com.qweuio.chat.core.exception.chatapp.ChatroomNotFoundException;
import com.qweuio.chat.core.exception.chatapp.TooManyChatroomsException;
import com.qweuio.chat.core.exception.chatapp.UserNotFoundException;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatService {
  Logger logger = LoggerFactory.getLogger(ChatService.class);
  @Autowired
  protected MessagePersistingService msgPersistingService;
  @Autowired
  protected ChatroomRepository chatroomRepo;
  @Autowired
  protected ChatUserRepository chatUserRepo;
  @Autowired
  protected MongoTemplate mongoTemplate;

  public UserWithRoleEntity.UserRole getUserRole(String chatroomId, String userId) {
    if (!chatroomRepo.existsById(chatroomId)) throw new ChatroomNotFoundException(chatroomId);
    return chatroomRepo.getUserRole(chatroomId, userId);
  }

  public List<Chatroom> getUserChatrooms(String userId) {
    return chatUserRepo.getChatroomsByUser(userId);
  }



  public Chatroom createChatroom(String firstUserId, String chatroomName) {
    ChatUser creator = chatUserRepo.findById(firstUserId).orElseThrow(() -> new RuntimeException("a ghost is trying to create a new chatroom"));
    if (creator.chatrooms().size() >= 100) {
      throw new TooManyChatroomsException(firstUserId, null);
    }
    Chatroom newChatroom = chatroomRepo.save(
      new Chatroom(
        null, chatroomName,
        List.of(new UserWithRoleEntity(firstUserId, UserWithRoleEntity.UserRole.ADMIN)),
        Collections.emptyList()
      )
    );
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(firstUserId)),
      new Update().addToSet("chatrooms", newChatroom.id()),
      "users");

    return newChatroom;
  }

  public void deleteChatroom(String chatroomId) {
    if (!chatroomRepo.existsById(chatroomId)) throw new ChatroomNotFoundException(chatroomId);
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

  public void modifyUserRole(String chatroomId, String userId, UserWithRoleEntity.UserRole newUserRole) {
    if (getUserRole(chatroomId, userId) == UserWithRoleEntity.UserRole.NOT_A_MEMBER) throw new UserNotFoundException(userId);
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(chatroomId)).addCriteria(Criteria.where("users.userId").is(userId)),
      new Update().set("users.userId", newUserRole),
      "chatrooms"
    );
  }

  public void addUserToChatroom(String chatroomId, String userId, UserWithRoleEntity.UserRole role) {
    UpdateResult u1 = mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userId)),
      new Update().addToSet("chatrooms", chatroomId),
      "users");
    logger.info(u1.toString());
    UpdateResult u2 = mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(chatroomId)),
      new Update().addToSet("users", new UserWithRoleEntity(userId, role)),
      "chatrooms");
    logger.info(u2.toString());
  }

  public void removeUserFromChatroom(String chatroomId, String userId) {
    if (chatroomRepo.getUsersByChatroom(chatroomId).stream().anyMatch(cu -> Objects.equals(cu.id(), userId))) {
      throw new UserNotFoundException(userId);
    }
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(userId)),
      new Update().pull("chatrooms", chatroomId),
      "users");
    mongoTemplate.updateFirst(
      new Query(Criteria.where("_id").is(chatroomId)),
      new Update().pull("users", Query.query(Criteria.where("userId").is(userId))),
      "chatrooms");
  }

  public List<ChatUser> getChatroomUsers(String chatroomId) {
    logger.info("getting users from chatroom {}",chatroomId);
    List<ChatUser> users = chatroomRepo.getUsersByChatroom(chatroomId);
    logger.info("users: {}", users.toString());
    return users;
  }
}
