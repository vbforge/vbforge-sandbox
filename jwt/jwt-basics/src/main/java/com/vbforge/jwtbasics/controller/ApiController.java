package com.vbforge.jwtbasics.controller;

import com.vbforge.jwtbasics.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ApiController — protected endpoints (JWT required).
 *
 * These routes require a valid JWT in the Authorization header:
 *   Authorization: Bearer <your_token>
 *
 * If no token or invalid token → 403 Forbidden (Spring Security blocks it
 * before it even reaches this controller).
 *
 * @AuthenticationPrincipal — injects the currently authenticated UserDetails
 * directly from the SecurityContext. Spring resolves this automatically.
 * No need to manually parse the JWT in the controller.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * Public-ish hello — just tests that the server is running.
     * Actually protected by JWT (all /api/** except /api/auth/** requires auth).
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(Map.of(
                "message", "Hello, " + currentUser.getUsername() + "! Your JWT is valid.",
                "username", currentUser.getUsername(),
                "email", currentUser.getEmail()
        ));
    }

    /**
     * Returns the current user's profile info.
     * Demonstrates how @AuthenticationPrincipal gives you full User access.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(Map.of(
                "id", currentUser.getId(),
                "username", currentUser.getUsername(),
                "email", currentUser.getEmail()
        ));
    }
}