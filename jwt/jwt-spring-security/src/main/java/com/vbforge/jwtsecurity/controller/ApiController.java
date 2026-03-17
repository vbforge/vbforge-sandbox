package com.vbforge.jwtsecurity.controller;

import com.vbforge.jwtsecurity.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/data")
public class ApiController {

    private JwtUtil jwtUtil;

    @GetMapping()
    public Map<String, String> getData(HttpServletRequest request, HttpServletResponse response) {

        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        String token = null;

        try{
            token = header.substring(7);
            jwtUtil.extractUsername(token);
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

