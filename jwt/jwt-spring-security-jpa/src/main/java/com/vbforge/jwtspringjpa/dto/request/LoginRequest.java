package com.vbforge.jwtspringjpa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 100, message = "Username must be 3–100 characters")
    private String username;

    @NotBlank(message = "Password must contain at least 8 characters with one digit, one letter, and one special character")
    private String password;
}
