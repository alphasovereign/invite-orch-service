package com.alphabytes.invite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "ownerId is required")
        @Size(max = 100, message = "ownerId must be at most 100 characters")
        String ownerId,

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        @Size(max = 255, message = "location must be at most 255 characters")
        String location,

        @NotNull(message = "eventDate is required")
        LocalDateTime eventDate
) {
}
