package com.vbforge.jwtspringjpa.service;

import com.vbforge.jwtspringjpa.config.SecurityConfig;
import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.request.UpdateRequest;
import com.vbforge.jwtspringjpa.dto.response.AdminResponse;
import com.vbforge.jwtspringjpa.dto.response.UserResponse;
import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import com.vbforge.jwtspringjpa.exception.ResourceNotFoundException;
import com.vbforge.jwtspringjpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    public static final String ADMIN_COM = "@admin.com"; // Restrict admin emails
    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User", id));
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Optional: Prevent self-deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself");
        }

        userRepository.delete(user);
        log.info("Deleted user by id: {}", id);
    }

    @Transactional
    public AdminResponse createAdmin(SignupRequest request) {
        if (!request.getEmail().endsWith(ADMIN_COM)) {
            throw new IllegalArgumentException("Invalid admin email domain");
        }
        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(securityConfig.passwordEncoder().encode(request.getPassword()))
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Admin {} created by {}", admin.getUsername(), SecurityContextHolder.getContext().getAuthentication().getName());
        return AdminResponse.from(admin);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", id));
        if (target.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }
        // Prevent self-deletion
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (target.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself");
        }
        userRepository.delete(target);
        log.info("Admin {} deleted by {}", target.getUsername(), currentUsername);
    }

    @Transactional
    public UserResponse updateOwnAccount(UpdateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUsername));

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(securityConfig.passwordEncoder().encode(request.getPassword()));
        }
        userRepository.save(user);
        log.info("User {} updated their account", currentUsername);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAdmins() {
        return userRepository.findByRole(Role.ROLE_ADMIN).stream()
                .map(AdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", id));
        if (admin.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }
        return AdminResponse.from(admin);
    }

}











