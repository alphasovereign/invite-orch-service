package com.alphabytes.invite.repository;

import com.alphabytes.invite.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findByOwnerIdOrderByEventDateAsc(String ownerId);
}
