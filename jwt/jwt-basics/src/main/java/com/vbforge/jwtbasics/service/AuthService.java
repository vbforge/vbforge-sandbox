package com.vbforge.jwtbasics.service;

import com.vbforge.jwtbasics.dto.request.LoginRequest;
import com.vbforge.jwtbasics.dto.request.RegisterRequest;
import com.vbforge.jwtbasics.dto.response.AuthResponse;
import com.vbforge.jwtbasics.entity.User;
import com.vbforge.jwtbasics.exception.UserAlreadyExistsException;
import com.vbforge.jwtbasics.repository.UserRepository;
import com.vbforge.jwtbasics.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — handles registration and login business logic.
 *
 * Register flow:
 *  1. Check for duplicate username/email
 *  2. Hash the password with BCrypt
 *  3. Save user to DB
 *  4. Generate JWT
 *  5. Return token
 *
 * Login flow:
 *  1. Delegate to AuthenticationManager (which uses DaoAuthenticationProvider)
 *     → Internally calls UserDetailsService.loadUserByUsername()
 *     → Then verifies the raw password against the BCrypt hash
 *  2. If authentication succeeds, load user and generate JWT
 *  3. Return token
 *
 * Why use AuthenticationManager for login?
 *  It's Spring Security's standard mechanism — it handles all edge cases
 *  (locked accounts, disabled users, bad credentials) and fires the
 *  appropriate events. No need to manually verify passwords.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        // Guard: no duplicate usernames
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Username '" + request.username() + "' is already taken"
            );
        }

        // Guard: no duplicate emails
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email '" + request.email() + "' is already registered"
            );
        }

        // Build and save the user — password is hashed with BCrypt
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))  // NEVER store plain text
                .email(request.email())
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Generate JWT for immediate use after registration
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, user.getUsername(), "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager verifies credentials internally
        // Throws BadCredentialsException if wrong — handled in GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // If we reach here, credentials are valid — load user and generate token
        User user = userRepository.findByUsername(request.username())
                .orElseThrow();  // Safe — authentication above already confirmed user exists

        String token = jwtUtil.generateToken(user);
        log.info("User logged in: {}", user.getUsername());

        return new AuthResponse(token, user.getUsername(), "Login successful");
    }
}