package com.vbforge.org.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CreatePersonDTO {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;
    
    @NotBlank(message = "Alias is required")
    @Size(min = 3, max = 50, message = "Alias must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Alias must contain only letters and numbers")
    private String alias;
    
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$", message = "Invalid phone number format")
    private String phone;
    
    // Constructors
    public CreatePersonDTO() {
    }
    
    public CreatePersonDTO(String name, String email, String alias, String phone) {
        this.name = name;
        this.email = email;
        this.alias = alias;
        this.phone = phone;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}