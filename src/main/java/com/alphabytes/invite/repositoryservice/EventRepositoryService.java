package com.alphabytes.invite.repositoryservice;

import com.alphabytes.invite.domain.Event;
import com.alphabytes.invite.entity.EventEntity;
import com.alphabytes.invite.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventRepositoryService {

    private final EventRepository eventRepository;

    public EventRepositoryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event saveEvent(Event event) {
        EventEntity eventEntity = toEntity(event);
        if (eventEntity.getCreatedAt() == null) {
            eventEntity.setCreatedAt(LocalDateTime.now());
        }

        EventEntity savedEvent = eventRepository.save(eventEntity);
        return toDomain(savedEvent);
    }

    public List<Event> getEventsByOwner(String ownerId) {
        return eventRepository.findByOwnerIdOrderByEventDateAsc(ownerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private EventEntity toEntity(Event event) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(event.getId());
        eventEntity.setOwnerId(event.getOwnerId());
        eventEntity.setTitle(event.getTitle());
        eventEntity.setDescription(event.getDescription());
        eventEntity.setLocation(event.getLocation());
        eventEntity.setEventDate(event.getEventDate());
        eventEntity.setCreatedAt(event.getCreatedAt());
        return eventEntity;
    }

    private Event toDomain(EventEntity eventEntity) {
        Event event = new Event();
        event.setId(eventEntity.getId());
        event.setOwnerId(eventEntity.getOwnerId());
        event.setTitle(eventEntity.getTitle());
        event.setDescription(eventEntity.getDescription());
        event.setLocation(eventEntity.getLocation());
        event.setEventDate(eventEntity.getEventDate());
        event.setCreatedAt(eventEntity.getCreatedAt());
        return event;
    }
}
