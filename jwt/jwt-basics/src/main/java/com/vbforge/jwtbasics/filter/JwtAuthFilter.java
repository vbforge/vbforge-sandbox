package com.vbforge.jwtbasics.filter;

import com.vbforge.jwtbasics.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter — intercepts every HTTP request and validates the JWT.
 *
 * Extends OncePerRequestFilter → guaranteed to run exactly once per request,
 * even in async dispatch scenarios.
 *
 * Flow:
 *  1. Extract the "Authorization" header
 *  2. If header is missing or doesn't start with "Bearer " → skip (let Spring handle it)
 *  3. Extract the JWT from the header (after "Bearer ")
 *  4. Extract username from the token
 *  5. If username found and no auth set in SecurityContext yet:
 *     a. Load UserDetails from DB
 *     b. Validate the token
 *     c. Build UsernamePasswordAuthenticationToken
 *     d. Set it in SecurityContextHolder
 *  6. Continue the filter chain
 *
 * Why SecurityContextHolder?
 *   Spring Security reads authentication from the SecurityContext.
 *   By setting it here, the rest of the request is treated as authenticated.
 *   Since sessions are STATELESS, this must happen on every request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Step 1: No header or wrong format → skip JWT processing
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract token (skip "Bearer " prefix — 7 characters)
        final String jwt = authHeader.substring(7);

        try {
            // Step 3: Extract username from token
            final String username = jwtUtil.extractUsername(jwt);

            // Step 4: Only process if username found and not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 5a: Load user from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5b: Validate token (checks username match + expiration)
                if (jwtUtil.isTokenValid(jwt, userDetails)) {

                    // Step 5c: Build authentication token
                    // credentials = null because JWT is already the proof of identity
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                          // no credentials needed
                                    userDetails.getAuthorities()   // empty list in Project 1
                            );

                    // Attach request details (IP, session id, etc.) — good practice
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 5d: Set authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user '{}' via JWT", username);
                }
            }

        } catch (Exception e) {
            // Invalid/expired token — log and continue without setting auth.
            // The request will be rejected by Spring Security as unauthenticated.
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        // Step 6: Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}