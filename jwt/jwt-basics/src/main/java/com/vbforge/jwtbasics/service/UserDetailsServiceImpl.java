package com.vbforge.jwtbasics.service;

import com.vbforge.jwtbasics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsServiceImpl — Spring Security uses this to load a user by username.
 *
 * Called in two places:
 *  1. During login  → AuthenticationManager calls it to verify credentials
 *  2. In JwtAuthFilter → to load UserDetails for token validation
 *
 * Our User entity already implements UserDetails, so we return it directly.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username
                ));
    }
}