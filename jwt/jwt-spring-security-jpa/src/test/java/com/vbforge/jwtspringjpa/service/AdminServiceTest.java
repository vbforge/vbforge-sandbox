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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllUsers_Success() {
        User user1 = User.builder().id(1L).username("user1").email("user1@example.com").role(Role.ROLE_USER).build();
        User user2 = User.builder().id(2L).username("user2").email("user2@example.com").role(Role.ROLE_ADMIN).build();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> users = adminService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_Success() {
        User user = User.builder().id(1L).username("user1").email("user1@example.com").role(Role.ROLE_USER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = adminService.getUserById(1L);

        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(1L));
    }

    @Test
    void deleteUserById_Success() {
        User user = User.builder().id(1L).username("user1").email("user1@example.com").role(Role.ROLE_USER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin");

            adminService.deleteUserById(1L);

            verify(userRepository, times(1)).delete(user);
        }
    }

    @Test
    void deleteUserById_SelfDeletion_ThrowsException() {
        User user = User.builder().id(1L).username("admin").email("admin@example.com").role(Role.ROLE_ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin");

            assertThrows(IllegalArgumentException.class, () -> adminService.deleteUserById(1L));
        }
    }

    @Test
    void createAdmin_Success() {
        SignupRequest request = new SignupRequest("newadmin", "newadmin@admin.com", "password123");
        when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin");

            AdminResponse response = adminService.createAdmin(request);

            assertNotNull(response);
            assertEquals(request.getUsername(), response.getUsername());
            assertEquals(Role.ROLE_ADMIN, response.getRole());
        }
    }

    @Test
    void createAdmin_InvalidEmail_ThrowsException() {
        SignupRequest request = new SignupRequest("newadmin", "newadmin@example.com", "password123");

        assertThrows(IllegalArgumentException.class, () -> adminService.createAdmin(request));
    }

    @Test
    void deleteAdmin_Success() {
        User admin = User.builder().id(1L).username("admin2").email("admin2@admin.com").role(Role.ROLE_ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin1");

            adminService.deleteAdmin(1L);

            verify(userRepository, times(1)).delete(admin);
        }
    }

    @Test
    void updateOwnAccount_Success() {
        UpdateRequest request = new UpdateRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");
        request.setPassword("newpassword123");

        User user = User.builder().id(1L).username("oldusername").email("oldemail@example.com").role(Role.ROLE_ADMIN).build();
        when(userRepository.findByUsername("oldusername")).thenReturn(Optional.of(user));
        when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedNewPassword");

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("oldusername");

            UserResponse response = adminService.updateOwnAccount(request);

            assertNotNull(response);
            assertEquals(request.getUsername(), response.getUsername());
            assertEquals(request.getEmail(), response.getEmail());
        }
    }
}