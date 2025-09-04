package com.qweuio.chat.persistence.repository;

public interface InsertRepository<E> {
  E insert(E entity);

  E update(E entity);
}
