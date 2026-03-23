package com.vbforge.jwtsecurity.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/security")
public class ApiSecurityDataController {

    @GetMapping()
    public Map getSecurityData(Authentication authentication) {

        String name = authentication.getName();

        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();

        Set<String> roles = new HashSet<String>();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            roles.add(grantedAuthority.getAuthority());
        }


        return Map.of("username", name, "roles", roles);



    }

}

