package com.qweuio.chat.security.data;

import com.qweuio.chat.persistence.repository.InsertRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserAuthorityRepository extends CrudRepository<UserAuthorities, UserAuthorities.UserAuthority>, InsertRepository<UserAuthorities> {
}
