package com.vbforge.jwtspringjpa.service;

import com.vbforge.jwtspringjpa.dto.request.LoginRequest;
import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.response.AuthResponse;
import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import com.vbforge.jwtspringjpa.exception.UserAlreadyExistException;
import com.vbforge.jwtspringjpa.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;


    @Test
    void signup_Success() {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123");
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate ID assignment
            return savedUser;
        });

        // Stub userDetailsService to return the user
        when(userDetailsService.loadUserByUsername(request.getUsername())).thenReturn(user);

        // Stub JwtService to return a token and expiration
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("testToken");
        when(jwtService.extractExpiration("testToken")).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        AuthResponse response = authService.signup(request);

        assertNotNull(response);
        assertEquals(request.getUsername(), response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_UsernameExists_ThrowsException() {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistException.class, () -> authService.signup(request));
    }

    @Test
    void signup_EmailExists_ThrowsException() {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistException.class, () -> authService.signup(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("testToken");
        when(jwtService.extractExpiration("testToken")).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(request.getUsername(), response.getUsername());
    }

    @Test
    void login_InvalidUsername_ThrowsException() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}