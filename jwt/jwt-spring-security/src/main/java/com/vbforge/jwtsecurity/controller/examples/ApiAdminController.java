package com.vbforge.jwtsecurity.controller.examples;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAdminInfo(){
        return "This is the Admin API";
    }

}
