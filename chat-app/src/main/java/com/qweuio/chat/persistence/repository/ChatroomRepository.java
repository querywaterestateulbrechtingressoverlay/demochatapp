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
    """
    { $facet: {
        user_match: [
          { $match: { users.userId': '?1' } },
          { $project: { users: { role: '$users.role' } } }
        ],
        not_found: [
          { $project: {  _id: 0, users: { role: 'NOT_A_MEMBER' } } }
        ]
      }
    }""",
    """
    { $project: { result: { $cond: {
      if: { $eq: [ { $size: "$user_match" }, 1 ] },
      then: { $arrayElemAt: [ "$user_match", 0 ] },
      else: { $arrayElemAt: [ "$not_found", 0 ] }
    } } } }""",
    "{ $replaceRoot: { newRoot: '$result.users' } }"
  })
  UserWithRoleEntity.UserRole getUserRole(String chatroomId, String userId);
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
//  void modifyUserRole(String chatroomId, String userId, UserWithRoleEntity.UserRole newRole);
}
