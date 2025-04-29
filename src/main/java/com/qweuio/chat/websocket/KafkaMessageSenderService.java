package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.websocket.dto.outbound.ChatroomListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.ErrorDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import com.qweuio.chat.websocket.dto.outbound.UserListUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qweuio.chat.websocket.dto.outbound.UserListUpdateDTO.Operation.ADD;

@Service
public class KafkaMessageSenderService {
  Logger logger = LoggerFactory.getLogger(KafkaMessageSenderService.class);
  @Value("${chatapp.kafka.message-topic}")
  private String sendMsgTopic;
  @Value("${chatapp.kafka.user-list-update-topic}")
  private String userListTopic;
  @Value("${chatapp.kafka.chatroom-list-update-topic}")
  private String chatroomListTopic;
  @Value("${chatapp.kafka.error-topic}")
  private String errorTopic;

  private final String messageHistoryDest = "/chatrooms/messages";

  @Autowired
  KafkaTemplate<String, Object> kafkaMessageTemplate;

  @Autowired
  SimpMessagingTemplate template;

  @Autowired
  ChatroomRepository chatroomRepo;

  public void sendErrorMessage(ErrorDTO errorDTO) {
    kafkaMessageTemplate.send(errorTopic, errorDTO);
  }

  public void sendMessage(ChatMessage message, String chatroomId) {
    logger.info("sending message " + message + " to chatroom " + chatroomId);
    kafkaMessageTemplate.send(sendMsgTopic, new MessageDTO(message.id(), message.senderId(), message.chatroomId(), message.sentAt(), message.contents()));
  }

  public void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history) {
    template.convertAndSendToUser(userId, messageHistoryDest, new ChatHistoryResponseDTO(chatroomId, history.stream().map(Converters::toDTO).toList()));
  }

  public void addChatroomToUser(String userId, ChatroomListDTO list) {
    kafkaMessageTemplate.send(chatroomListTopic, new ChatroomListUpdateDTO(userId, list.chatrooms(), ChatroomListUpdateDTO.Operation.ADD));
  }

  public void removeChatroomFromUser(String userId, String chatroomId) {
    kafkaMessageTemplate.send(chatroomListTopic, new ChatroomListUpdateDTO(userId, List.of(new ChatroomShortInfoDTO(chatroomId, null)), ChatroomListUpdateDTO.Operation.REMOVE));
  }

  public void addUserToChatroom(String chatroomId, List<UserShortInfoDTO> users) {
    var chatroomUsers = chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList();
    kafkaMessageTemplate.send(userListTopic, new UserListUpdateDTO(chatroomUsers, chatroomId, users, ADD));
  }

  public void removeUserFromChatroom(String chatroomId, String userId) {
    var chatroomUsers = chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList();
    kafkaMessageTemplate.send(userListTopic, new UserListUpdateDTO(chatroomUsers, chatroomId, List.of(new UserShortInfoDTO(userId, null)), UserListUpdateDTO.Operation.REMOVE));
  }
}
