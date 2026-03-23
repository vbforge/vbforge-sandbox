package com.vbforge.jwtsecurity.filter;

import com.vbforge.jwtsecurity.service.CustomUserDetailService;
import com.vbforge.jwtsecurity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;
    private CustomUserDetailService customUserDetailService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        /*if(header == null || !header.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }*/

        if(header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            String username = jwtUtil.extractUsername(token);
            if(username != null){
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if(authentication == null){
                    UserDetails user = customUserDetailService.loadUserByUsername(username);
                    if(jwtUtil.isTokenValid(token, user)){
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(username, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /*@Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String servletPath = request.getServletPath();

        //"/api/health", "/api/auth/signup", "/api/auth/login", "/api/data", "/api/advance"

        return switch (servletPath) {
            case "/api/health", "/api/auth/signup", "/api/auth/login", "/api/auth/existUser" -> true;  //no filter
            default -> false;
        };

    }*/
}
