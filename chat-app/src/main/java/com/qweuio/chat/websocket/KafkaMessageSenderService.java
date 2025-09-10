package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.Message;
import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.persistence.repository.UserRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.websocket.dto.messaging.ChatroomListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.outbound.ErrorDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import com.qweuio.chat.websocket.dto.messaging.UserListUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.qweuio.chat.websocket.dto.messaging.UserListUpdateDTO.Operation.ADD;
import static com.qweuio.chat.websocket.dto.messaging.UserListUpdateDTO.Operation.REMOVE;

@Service
public class KafkaMessageSenderService {
  Logger logger = LoggerFactory.getLogger(KafkaMessageSenderService.class);
  @Value("${chatapp.kafka.update-topic}")
  private String updateTopic;
  @Value("${chatapp.kafka.update-topic-partition.error}")
  private int errorPartition;
  @Value("${chatapp.kafka.update-topic-partition.message}")
  private int messagePartition;
  @Value("${chatapp.kafka.update-topic-partition.chatroom-list}")
  private int chatroomListPartition;
  @Value("${chatapp.kafka.update-topic-partition.chatroom-user-list}")
  private int chatroomUserListPartition;

  private final String messageHistoryDest = "/chatrooms/messages";

  @Autowired
  KafkaTemplate<Object, Object> kafkaMessageTemplate;

  @Autowired
  SimpMessagingTemplate template;

  @Autowired
  ChatroomRepository chatroomRepo;

  @Autowired
  UserRepository userRepo;

  public void sendErrorMessage(ErrorDTO errorDTO) {
    logger.info("error " + errorDTO.toString());
    kafkaMessageTemplate.send(updateTopic, errorPartition, null, errorDTO);
  }

  public void sendMessage(Message message, UUID chatroomId) {
    logger.info("sending message {} to chatroom {}", message, chatroomId);
    kafkaMessageTemplate.send(updateTopic, messagePartition, null, new MessageDTO(message.id(), message.senderId(), message.chatroomId(), message.sentAt(), message.contents()));
  }

  public void updateMessageHistory(UUID userId, UUID chatroomId, List<Message> history) {
    template.convertAndSendToUser(userId.toString(), messageHistoryDest, new ChatHistoryResponseDTO(chatroomId, history.stream().map(Converters::toDTO).toList()));
  }

  public void addChatroomToUser(UUID userId, ChatroomListDTO list) {
    kafkaMessageTemplate.send(updateTopic, chatroomListPartition, null, new ChatroomListUpdateDTO(userId, list.chatrooms(), ChatroomListUpdateDTO.Operation.ADD));
  }

  public void removeChatroomFromUser(UUID userId, UUID chatroomId) {
    kafkaMessageTemplate.send(updateTopic, chatroomListPartition, null, new ChatroomListUpdateDTO(userId, List.of(new ChatroomShortInfoDTO(chatroomId, null)), ChatroomListUpdateDTO.Operation.REMOVE));
  }

  public void addUserToChatroom(UUID chatroomId, List<UserShortInfoDTO> users) {
    var chatroomUsers = userRepo.findUsersByChatroomId(chatroomId).stream().map(User::id).toList();
    kafkaMessageTemplate.send(
        updateTopic,
        chatroomUserListPartition,
        null,
        new UserListUpdateDTO(
          chatroomUsers,
          chatroomId,
          users,
          ADD));
  }

  public void removeUserFromChatroom(UUID chatroomId, UUID userId) {
    var chatroomUsers = userRepo.findUsersByChatroomId(chatroomId).stream().map(User::id).toList();
    kafkaMessageTemplate.send(
        updateTopic,
        chatroomUserListPartition,
        null,
        new UserListUpdateDTO(
          chatroomUsers,
          chatroomId,
          List.of(new UserShortInfoDTO(userId, null)),
          REMOVE));
  }
}
