package com.vbforge.jwtsecurity.controller.examples;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class ApiUserController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public String getUserInfo(){
        return "This is the User API";
    }

}
