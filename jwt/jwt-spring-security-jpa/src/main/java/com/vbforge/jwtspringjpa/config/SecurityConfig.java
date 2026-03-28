package com.vbforge.jwtspringjpa.config;

import com.vbforge.jwtspringjpa.filter.JwtAuthenticationFilter;
import com.vbforge.jwtspringjpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String API_HEALTH = "/api/health";
    public static final String API_AUTH = "/api/auth/**";
    public static final String API_ADMIN = "/api/admin/**";
    public static final String API_USER = "/api/user/**";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ── Public ──────────────────────────────────────────────
                        .requestMatchers(API_HEALTH).permitAll()
                        .requestMatchers(API_AUTH).permitAll()

                        // ── Admin panel ───────────────────────────────────────────
                        .requestMatchers(API_ADMIN).hasRole("ADMIN")

                        // ── User panel ───────────────────────────────────────────
                        .requestMatchers(API_USER).hasRole("USER")

                        // ── to allow multiple roles for certain endpoints ──────────
                        .requestMatchers("/api/some-endpoint").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )

                // ── Exception handling ────────────────────────────────────────
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                handleUnauthorized(response, authException.getMessage())
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                handleForbidden(response)
                        )
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //for frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Your frontend URL
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    //helper methods

    // no/invalid authentication  → 401 (AuthenticationEntryPoint)
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                String.format("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", message)
        );
    }

    // authenticated but wrong role → 403 (AccessDeniedHandler)
    private void handleForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}"
        );
    }

}
