package com.qweuio.chat.security.data;


import com.qweuio.chat.persistence.repository.InsertRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginDataRepository extends CrudRepository<LoginData, UUID>, InsertRepository<LoginData> {
  Optional<LoginData> findByLoginData(String loginData);
}
