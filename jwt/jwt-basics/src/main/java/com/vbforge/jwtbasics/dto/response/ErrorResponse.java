package com.vbforge.jwtbasics.dto.response;

import java.time.LocalDateTime;

/**
 * ErrorResponse — consistent error payload for all error scenarios.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now());
    }
}
