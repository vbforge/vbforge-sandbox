package com.vbforge.jwtspringjpa.controller;

import com.vbforge.jwtspringjpa.dto.request.UpdateRequest;
import com.vbforge.jwtspringjpa.dto.response.UserResponse;
import com.vbforge.jwtspringjpa.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AdminService adminService;  // Reuse AdminService for updates

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateOwnAccount(@Valid @RequestBody UpdateRequest request) {
        return ResponseEntity.ok(adminService.updateOwnAccount(request));
    }

}
