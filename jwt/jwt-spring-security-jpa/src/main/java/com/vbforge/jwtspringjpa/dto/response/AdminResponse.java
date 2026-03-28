package com.vbforge.jwtspringjpa.dto.response;

import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public static AdminResponse from(User admin) {

        return AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .build();
    }

}
