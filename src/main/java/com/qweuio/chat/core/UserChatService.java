package com.qweuio.chat.core;

import com.mongodb.client.result.UpdateResult;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserChatService extends ChatService {
  Logger logger = LoggerFactory.getLogger(UserChatService.class);

  public List<ChatMessage> getChatroomRecentHistory(String callingUserId, String chatroomId) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) == UserWithRoleEntity.UserRole.NOT_A_MEMBER) {
        throw new InsufficientPermissionsException("get recent history in chatroom " + chatroomId);
      }
      return super.msgPersistingService.getRecentMessages(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public ChatUser getUserInfo(String userId) {
    return chatUserRepo.findById(userId).orElseThrow();
  }
  
  public ChatMessage saveMessage(String callingUserId, String chatroomId, MessageRequestDTO message) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) == UserWithRoleEntity.UserRole.NOT_A_MEMBER) {
        throw new InsufficientPermissionsException("send message to chatroom " + chatroomId);
      }
      return msgPersistingService.persistMessage(new MessageDTO(null, callingUserId, chatroomId, Instant.now(), message.message()));
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }

  
  public Chatroom addUserToChatroom(String callingUserId, String userToAddId, String chatroomId) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) != UserWithRoleEntity.UserRole.ADMIN) {
        throw new InsufficientPermissionsException("add user " + userToAddId + " to chatroom " + chatroomId);
      }
      if (chatUserRepo.getChatroomCount(userToAddId).orElseThrow(() -> new UserNotFoundException(userToAddId)) >= 100) {
        throw new TooManyChatroomsException(userToAddId, chatroomId);
      }
      if (chatroomRepo.getUserCount(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(chatroomId)) >= 100) {
        throw new TooManyUsersException(userToAddId, chatroomId);
      }
      super.addUserToChatroom(chatroomId, userToAddId, UserWithRoleEntity.UserRole.MEMBER);
      Optional<Chatroom> chatroom = chatroomRepo.findById(chatroomId);
      assert chatroom.isPresent();
      return chatroom.get();
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public void removeUserFromChatroom(String callingUserId, String userToRemoveId, String chatroomId) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) != UserWithRoleEntity.UserRole.ADMIN ||
          super.getUserRole(userToRemoveId, chatroomId) == UserWithRoleEntity.UserRole.ADMIN) {
        throw new InsufficientPermissionsException("remove user " + userToRemoveId + " from chatroom " + chatroomId);
      }
      super.removeUserFromChatroom(chatroomId, userToRemoveId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public Chatroom createChatroom(String callingUserId, String chatroomName) {
    try {
      return super.createChatroom(callingUserId, chatroomName);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public void deleteChatroom(String callingUserId, String chatroomId) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) != UserWithRoleEntity.UserRole.ADMIN) {
        throw new InsufficientPermissionsException("delete chatroom " + chatroomId);
      }
      super.deleteChatroom(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }

  public List<ChatUser> getChatroomUsers(String callingUserId, String chatroomId) {
    try {
      if (super.getUserRole(callingUserId, chatroomId) == UserWithRoleEntity.UserRole.NOT_A_MEMBER) {
        throw new InsufficientPermissionsException("get user list of chatroom " + chatroomId);
      }
      return super.getChatroomUsers(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
}
