package com.vbforge.jwtspringjpa.controller;

import com.vbforge.jwtspringjpa.dto.request.LoginRequest;
import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.response.AuthResponse;
import com.vbforge.jwtspringjpa.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void signup_Success() {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123");
        AuthResponse expectedResponse = AuthResponse.builder()
                .token("testToken")
                .username("testuser")
                .email("test@example.com")
                .build();
        when(authService.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.signup(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse expectedResponse = AuthResponse.builder()
                .token("testToken")
                .username("testuser")
                .email("test@example.com")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }
}