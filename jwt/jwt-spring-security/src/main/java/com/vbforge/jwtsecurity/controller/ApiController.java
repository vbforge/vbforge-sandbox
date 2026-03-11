package com.vbforge.jwtsecurity.controller;

import com.vbforge.jwtsecurity.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class ApiController {

    private JwtUtil jwtUtil;

    @GetMapping("/data")
    public Map<String, String> getData(HttpServletRequest request, HttpServletResponse response) {

        String header = request.getHeader("Authorization");

        String token = header.substring(7);

        String username;

        try{
            username = jwtUtil.extractUsername(token);
        }catch(Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        String role = jwtUtil.extractUserRole(token);

        if (!"ROLE_USER".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return Map.of("key", "value");


    }

}

