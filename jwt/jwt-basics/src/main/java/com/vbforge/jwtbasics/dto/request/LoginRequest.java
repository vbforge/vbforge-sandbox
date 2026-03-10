package com.vbforge.jwtbasics.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest — payload for POST /api/auth/login
 */
public record LoginRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {}