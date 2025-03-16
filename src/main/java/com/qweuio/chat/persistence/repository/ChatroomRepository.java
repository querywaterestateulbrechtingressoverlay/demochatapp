package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserRole;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatroomRepository extends MongoRepository<Chatroom, String> {
  @Aggregation({
    "{ $match : { '_id' : '?0' } }",
    "{ $unwind : '$users' }",
    "{ $match : { 'users.userId' : '?1' } }",
    "{ $project : { _id: 0, userId: '$users.userId', role: '$users.role' } }"
  })
  Optional<UserRole> getUserRoleFromChatroomById(String chatroomId, String userId);
  @Aggregation({
    "{ $match: { '_id': ?0 } }",
    "{ $unwind: '$users' }",
    """
    { $lookup: {
      from: 'users',
      localField: 'users.userId',
      foreignField: '_id',
      as: 'users' }
    }""",
    "{ $unwind: '$users' }",
    "{ $replaceRoot: { newRoot: '$users' } }"
  })
  List<ChatUser> getUsersByChatroom(String chatroomId);
}
