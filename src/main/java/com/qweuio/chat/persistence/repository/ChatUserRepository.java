package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends MongoRepository<ChatUser, String> {
  Optional<ChatUser> findByName(String name);

  @Aggregation({
    "{ $match: { '_id': ?0 } }",
    "{ $unwind: '$chatrooms' }",
    """
    { $lookup: {
      from: 'chatrooms',
      localField: 'chatrooms',
      foreignField: '_id',
      as: 'chatrooms' }
    }""",
    "{ $unwind: '$chatrooms' }",
    "{ $replaceRoot: { newRoot: '$chatrooms' } }"
  })
  List<Chatroom> getChatroomsByUser(String userId);

  @Aggregation({
    "{ $match: { '_id': ?0 } }",
    "{ $project: { count: { $size: '$chatrooms' }, _id: 0 } }"
  })
  Optional<Integer> getChatroomCount(String userToAdd);
}
