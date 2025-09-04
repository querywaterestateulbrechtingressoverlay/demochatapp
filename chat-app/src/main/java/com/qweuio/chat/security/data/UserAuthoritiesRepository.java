package com.qweuio.chat.security.data;

import com.qweuio.chat.persistence.repository.InsertRepository;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface UserAuthoritiesRepository extends CrudRepository<UserAuthorities, UserAuthority>, InsertRepository<UserAuthorities> {
  @Query("""
    SELECT * FROM user_authorities
    WHERE user_id = :userId""")
  List<UserAuthorities> findByUserId(UUID userId);
  @Modifying
  @Query("""
    DELETE FROM user_authorities
    WHERE user_id = :userId""")
  void deleteByUserId(UUID userId);
}
