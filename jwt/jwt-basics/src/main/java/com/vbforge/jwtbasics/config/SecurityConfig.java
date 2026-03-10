package com.vbforge.jwtbasics.config;

import com.vbforge.jwtbasics.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — wires together Spring Security 6 with JWT.
 *
 * Key concepts demonstrated here:
 *
 * 1. SecurityFilterChain (replaces the old WebSecurityConfigurerAdapter)
 *    → Lambda-based DSL configuration in Spring Security 6
 *
 * 2. STATELESS session management
 *    → No HttpSession created or used
 *    → Each request must carry its own JWT
 *
 * 3. CSRF disabled
 *    → CSRF protection is for cookie/session-based auth
 *    → Stateless JWT APIs don't need it
 *
 * 4. AuthenticationProvider (DaoAuthenticationProvider)
 *    → Connects UserDetailsService + PasswordEncoder
 *    → Used by AuthenticationManager during login
 *
 * 5. JwtAuthFilter placement
 *    → Runs BEFORE UsernamePasswordAuthenticationFilter
 *    → This ensures JWT is validated before Spring's default login processing
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Define which endpoints are public vs protected
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // login & register are public
                .anyRequest().authenticated()                  // everything else requires JWT
            )

            // STATELESS — no session, no cookie
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Insert JwtAuthFilter BEFORE Spring's default auth filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * DaoAuthenticationProvider — ties together:
     *  - UserDetailsService (loads user from DB by username)
     *  - PasswordEncoder (BCrypt — verifies hashed password)
     *
     * Used by AuthenticationManager.authenticate() during login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — Spring Security's central auth gateway.
     * We expose it as a Bean so AuthService can call it directly during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCryptPasswordEncoder — industry-standard password hashing.
     * Never store plain-text passwords!
     *
     * BCrypt automatically handles:
     *  - Random salt generation
     *  - Adaptive cost factor (default strength = 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}