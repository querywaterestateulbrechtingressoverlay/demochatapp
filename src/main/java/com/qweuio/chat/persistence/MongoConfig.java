package com.qweuio.chat.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig {
  @Autowired
  void disableClassHints(MappingMongoConverter mmc) {
    mmc.setTypeMapper(new DefaultMongoTypeMapper(null));
  }
}
