package com.vbforge.jwtspringjpa.service;

import com.vbforge.jwtspringjpa.dto.response.UserResponse;
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

    private final UserRepository userRepository;

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

}
