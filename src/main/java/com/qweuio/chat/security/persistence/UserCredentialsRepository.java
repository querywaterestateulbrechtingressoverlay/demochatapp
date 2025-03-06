package com.qweuio.chat.security.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCredentialsRepository extends MongoRepository<UserCredentials, Integer> {
}
