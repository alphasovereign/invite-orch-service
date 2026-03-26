package com.alphabytes.invite.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<FieldValidationError> fieldErrors
) {

    public record FieldValidationError(String field, String message) {
    }
}
