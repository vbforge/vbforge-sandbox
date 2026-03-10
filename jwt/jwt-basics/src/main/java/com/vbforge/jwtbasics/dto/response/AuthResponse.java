package com.vbforge.jwtbasics.dto.response;

/**
 * AuthResponse — returned after successful login or register.
 *
 * The client stores this token (localStorage, memory, etc.)
 * and sends it back in every request as:
 *   Authorization: Bearer <token>
 */
public record AuthResponse(
        String token,
        String username,
        String message
) {}