package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, Integer> {

}
