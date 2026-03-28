package com.vbforge.jwtspringjpa.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vbforge.jwtspringjpa.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuthResponse {

    private String token;

    private String username;

    private String email;

    private Role role;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tokenExpiresAt;


}
