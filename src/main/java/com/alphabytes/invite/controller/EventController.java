package com.alphabytes.invite.controller;

import com.alphabytes.invite.domain.Event;
import com.alphabytes.invite.dto.CreateEventRequest;
import com.alphabytes.invite.dto.EventResponse;
import com.alphabytes.invite.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = toDomain(request);
        return toResponse(eventService.createEvent(event));
    }

    @GetMapping("/owner/{ownerId}")
    public List<EventResponse> getEventsByOwner(@PathVariable String ownerId) {
        return eventService.getEventsByOwner(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Event toDomain(CreateEventRequest request) {
        Event event = new Event();
        event.setOwnerId(request.ownerId());
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setEventDate(request.eventDate());
        return event;
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getOwnerId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDate(),
                event.getCreatedAt()
        );
    }
}
