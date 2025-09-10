package com.qweuio.chat.websocket.dto.outbound;

import com.qweuio.chat.websocket.dto.UserShortInfoDTO;

import java.util.List;
import java.util.UUID;

public record UserListDTO(
    UUID chatroomId,
    List<UserShortInfoDTO> userListUpdate) {
}
