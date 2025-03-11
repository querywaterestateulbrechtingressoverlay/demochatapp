package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.Chatroom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatroomRepository extends MongoRepository<Chatroom, String> {

}
