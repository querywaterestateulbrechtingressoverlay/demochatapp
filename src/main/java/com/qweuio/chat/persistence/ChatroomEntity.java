package com.qweuio.chat.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.beans.Transient;

@Table("chatrooms")
public class ChatroomEntity implements Persistable<Integer> {
    @Id
    @Column("id")
    private final Integer id;
    @Column("name")
    private final String name;

    private boolean isNew;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public ChatroomEntity(String name, Integer id) {
        this.name = name;
        this.id = id;
        this.isNew = false;
    }

    public ChatroomEntity(String name, Integer id, boolean isNew) {
        this.name = name;
        this.id = id;
        this.isNew = isNew;
    }

    @Transient
    public String getName() {
        return name;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
