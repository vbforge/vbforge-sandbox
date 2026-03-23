package com.vbforge.jwtsecurity.service;

import com.vbforge.jwtsecurity.entity.User;
import com.vbforge.jwtsecurity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userByUsername = userRepository.findUserByUsername(username);

        String role = userByUsername.getRole();

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role);

        return new org.springframework.security.core.userdetails.User(userByUsername.getUsername(), userByUsername.getPassword(), List.of(grantedAuthority));

    }
}
