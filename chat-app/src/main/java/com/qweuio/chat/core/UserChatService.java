package com.qweuio.chat.core;

import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.core.exception.chatapp.*;
import com.qweuio.chat.persistence.entity.Message;
import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserRole;
import com.qweuio.chat.websocket.dto.inbound.MessageRequestDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserChatService extends ChatService {
  Logger logger = LoggerFactory.getLogger(UserChatService.class);

  public List<Message> getChatroomRecentHistory(UUID callingUserId, UUID chatroomId) {
    try {
      if (super.getUserRole(chatroomId, callingUserId) == UserRole.NOT_A_MEMBER) {
        throw new InsufficientPermissionsException("get recent history in chatroom " + chatroomId);
      }
      return super.msgPersistingService.getRecentMessages(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public User getUserInfo(UUID userId) {
    return userRepo.findById(userId).orElseThrow();
  }
  
  public Message saveMessage(UUID callingUserId, UUID chatroomId, MessageRequestDTO message) {
    logger.info("saving msg {} from {} in chatroom {}", message.toString(), callingUserId, chatroomId);
    try {
      if (super.getUserRole(chatroomId, callingUserId) == UserRole.NOT_A_MEMBER) {
        logger.info("user is not a member of the chatroom, throwing an exception");
        throw new InsufficientPermissionsException("send message to chatroom " + chatroomId);
      }
      logger.info("user is a member of the chatroom, saving the message");
      return msgPersistingService.persistMessage(new MessageDTO(null, callingUserId, chatroomId, Instant.now(), message.message()));
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }

  
  public Chatroom addUserToChatroom(UUID callingUserId, UUID userToAddId, UUID chatroomId) {
    try {
      if (super.getUserRole(chatroomId, callingUserId) != UserRole.ADMIN) {
        throw new InsufficientPermissionsException("add user " + userToAddId + " to chatroom " + chatroomId);
      }
      if (chatroomUserRepo.userChatroomCount(userToAddId) >= 100) {
        throw new TooManyChatroomsException(userToAddId, chatroomId);
      }
      if (chatroomUserRepo.chatroomUserCount(chatroomId) >= 100) {
        throw new TooManyUsersException(userToAddId, chatroomId);
      }
      super.addUserToChatroom(chatroomId, userToAddId, UserRole.MEMBER);
      Optional<Chatroom> chatroom = chatroomRepo.findById(chatroomId);
      assert chatroom.isPresent();
      return chatroom.get();
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public void removeUserFromChatroom(UUID callingUserId, UUID userToRemoveId, UUID chatroomId) {
    try {
      if (super.getUserRole(chatroomId, callingUserId) != UserRole.ADMIN ||
          super.getUserRole(userToRemoveId, chatroomId) == UserRole.ADMIN) {
        throw new InsufficientPermissionsException("remove user " + userToRemoveId + " from chatroom " + chatroomId);
      }
      super.removeUserFromChatroom(chatroomId, userToRemoveId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public Chatroom createChatroom(UUID callingUserId, String chatroomName) {
    try {
      return super.createChatroom(callingUserId, chatroomName);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
  
  public void deleteChatroom(UUID callingUserId, UUID chatroomId) {
    try {
      if (super.getUserRole(chatroomId, callingUserId) != UserRole.ADMIN) {
        throw new InsufficientPermissionsException("delete chatroom " + chatroomId);
      }
      super.deleteChatroom(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }

  public List<User> getChatroomUsers(UUID callingUserId, UUID chatroomId) {
    try {
      if (super.getUserRole(chatroomId, callingUserId) == UserRole.NOT_A_MEMBER) {
        throw new InsufficientPermissionsException("get user list of chatroom " + chatroomId);
      }
      return super.getChatroomUsers(chatroomId);
    } catch (ChatAppException e) {
      throw new UserActionException(callingUserId, e.getMessage(), e);
    }
  }
}
