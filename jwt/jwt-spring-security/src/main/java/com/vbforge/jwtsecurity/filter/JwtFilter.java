package com.vbforge.jwtsecurity.filter;

import com.vbforge.jwtsecurity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = null;

        try{
            token = header.substring(7);
            jwtUtil.extractUsername(token);
        }catch(Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String role = jwtUtil.extractUserRole(token);

        if (!"ROLE_USER".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String servletPath = request.getServletPath();

        //"/api/health", "/api/auth/signup", "/api/auth/login", "/api/data", "/api/advance"

        return switch (servletPath) {
            case "/api/health", "/api/auth/signup", "/api/auth/login" -> true;  //no filter
            default -> false;
        };

    }
}
