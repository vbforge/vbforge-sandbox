package com.vbforge.jwtspringjpa.controller;

import com.vbforge.jwtspringjpa.dto.request.SignupRequest;
import com.vbforge.jwtspringjpa.dto.request.UpdateRequest;
import com.vbforge.jwtspringjpa.dto.response.AdminResponse;
import com.vbforge.jwtspringjpa.dto.response.UserResponse;
import com.vbforge.jwtspringjpa.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUserById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateOwnAccount(@Valid @RequestBody UpdateRequest request) {
        return ResponseEntity.ok(adminService.updateOwnAccount(request));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminResponse>> getAdmins() {
        return ResponseEntity.ok(adminService.getAdmins());
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

}

