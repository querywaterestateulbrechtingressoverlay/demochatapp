package com.qweuio.chat.security.data;


import org.springframework.data.repository.CrudRepository;

public interface UserCredentialsRepository extends CrudRepository<LoginData, String> {
}
