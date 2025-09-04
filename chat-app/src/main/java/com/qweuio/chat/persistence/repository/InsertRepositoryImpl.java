package com.qweuio.chat.persistence.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

public class InsertRepositoryImpl<E> implements InsertRepository<E> {
  @Autowired
  private JdbcAggregateTemplate templ;
  @Override
  public E insert(E entity) {
    return templ.insert(entity);
  }
  @Override
  public E update(E entity) {
    return templ.update(entity);
  }
}
