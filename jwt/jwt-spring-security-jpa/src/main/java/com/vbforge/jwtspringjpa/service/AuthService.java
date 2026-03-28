package com.vbforge.jwtspringjpa.service;

import com.vbforge.jwtspringjpa.dto.request.LoginRequest;
import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.response.AuthResponse;
import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import com.vbforge.jwtspringjpa.exception.UserAlreadyExistException;
import com.vbforge.jwtspringjpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;


    // ----- signup ---------------------------
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        if(userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistException(signupRequest.getUsername());
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new UserAlreadyExistException("Email " + signupRequest.getEmail() + " is already in use");
        }

        User user = User.builder()
                .username(signupRequest.getUsername().trim())
                .email(signupRequest.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        log.info("New User: {} signed up successfully",  user.getUsername());

        return buildAuthResponse(user);
    }


    // ----- login ---------------------------
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user);
    }


    // ----- helper method ---------------------------

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        LocalDateTime expirationDate = jwtService.extractExpiration(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tokenExpiresAt(expirationDate)
                .build();
    }

}























