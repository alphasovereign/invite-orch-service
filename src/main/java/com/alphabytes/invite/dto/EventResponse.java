package com.alphabytes.invite.dto;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String ownerId,
        String title,
        String description,
        String location,
        LocalDateTime eventDate,
        LocalDateTime createdAt
) {
}
