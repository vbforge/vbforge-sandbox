package com.vbforge.jwtspringjpa.controller;

import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.request.UpdateRequest;
import com.vbforge.jwtspringjpa.dto.response.AdminResponse;
import com.vbforge.jwtspringjpa.dto.response.UserResponse;
import com.vbforge.jwtspringjpa.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void listUsers_Success() {
        UserResponse user1 = UserResponse.builder().id(1L).username("user1").build();
        UserResponse user2 = UserResponse.builder().id(2L).username("user2").build();
        when(adminService.getAllUsers()).thenReturn(List.of(user1, user2));

        ResponseEntity<List<UserResponse>> response = adminController.listUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUser_Success() {
        UserResponse expectedResponse = UserResponse.builder().id(1L).username("user1").build();
        when(adminService.getUserById(1L)).thenReturn(expectedResponse);

        ResponseEntity<UserResponse> response = adminController.getUser(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void createAdmin_Success() {
        SignupRequest request = new SignupRequest("newadmin", "newadmin@admin.com", "password123");
        AdminResponse expectedResponse = AdminResponse.builder()
                .username("newadmin")
                .email("newadmin@admin.com")
                .build();
        when(adminService.createAdmin(any(SignupRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<AdminResponse> response = adminController.createAdmin(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void deleteAdmin_Success() {
        ResponseEntity<Void> response = adminController.deleteAdmin(1L);

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void updateOwnAccount_Success() {
        UpdateRequest request = new UpdateRequest();
        request.setUsername("newusername");
        UserResponse expectedResponse = UserResponse.builder()
                .username("newusername")
                .build();
        when(adminService.updateOwnAccount(any(UpdateRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<UserResponse> response = adminController.updateOwnAccount(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void getAdmins_Success() {
        AdminResponse admin1 = AdminResponse.builder().id(1L).username("admin1").build();
        AdminResponse admin2 = AdminResponse.builder().id(2L).username("admin2").build();
        when(adminService.getAdmins()).thenReturn(List.of(admin1, admin2));

        ResponseEntity<List<AdminResponse>> response = adminController.getAdmins();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAdminById_Success() {
        AdminResponse expectedResponse = AdminResponse.builder().id(1L).username("admin1").build();
        when(adminService.getAdminById(1L)).thenReturn(expectedResponse);

        ResponseEntity<AdminResponse> response = adminController.getAdminById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
    }
}