package com.qweuio.chat.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Integer> {
  Optional<User> findByName(String name);
}
