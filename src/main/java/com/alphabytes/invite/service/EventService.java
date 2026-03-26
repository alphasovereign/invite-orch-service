package com.alphabytes.invite.service;

import com.alphabytes.invite.domain.Event;
import com.alphabytes.invite.repositoryservice.EventRepositoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepositoryService eventRepositoryService;

    public EventService(EventRepositoryService eventRepositoryService) {
        this.eventRepositoryService = eventRepositoryService;
    }

    @Transactional
    public Event createEvent(Event event) {
        return eventRepositoryService.saveEvent(event);
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByOwner(String ownerId) {
        return eventRepositoryService.getEventsByOwner(ownerId);
    }
}
