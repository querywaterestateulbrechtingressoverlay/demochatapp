package com.qweuio.chat.persistence.repository;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
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
  Optional<UserWithRoleEntity> getUserRoleFromChatroomById(String chatroomId, String userId);
  @Aggregation({
    "{ $match: { $expr: { $eq: ['$_id', { $toObjectId: '?0' }] } } }",
    "{ $unwind: '$users' }",
    "{ $addFields: { 'userId': { $toObjectId: '$users.userId' } } }",
    """
    { $lookup: {
      from: 'users',
      localField: 'userId',
      foreignField: '_id',
      as: 'users' }
    }""",
    "{ $unwind: '$users' }",
    "{ $replaceRoot: { newRoot: '$users' } }"
  })
  List<ChatUser> getUsersByChatroom(String chatroomId);
  @Aggregation({
    "{ $match: { '_id': '?0' } }",
    "{ $project: { count: { $size: '$users' }, _id: 0 } }"
  })
  Optional<Integer> getUserCount(String chatroomId);
}
