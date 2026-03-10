package com.vbforge.jwtbasics.controller;

import com.vbforge.jwtbasics.dto.request.LoginRequest;
import com.vbforge.jwtbasics.dto.request.RegisterRequest;
import com.vbforge.jwtbasics.dto.response.AuthResponse;
import com.vbforge.jwtbasics.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — public endpoints (no JWT required).
 *
 * Mapped under /api/auth/** — permitted in SecurityConfig.
 *
 * POST /api/auth/register  → create account, returns JWT
 * POST /api/auth/login     → verify credentials, returns JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}