package com.vbforge.jwtsecurity.service;

import com.vbforge.jwtsecurity.repository.UserRepository;
import com.vbforge.jwtsecurity.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;

    public void saveUser(String username, String password) {
        userRepository.createUser(username, password, "ROLE_USER");
    }

    public String loginUser(String login, String password) {
        boolean exist = userRepository.existUser(login, password);
        if (!exist) {
            throw new UsernameNotFoundException("Invalid username or password");
        }

        return jwtUtil.generateToken(login, "ROLE_USER");
    }
}
