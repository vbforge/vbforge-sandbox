package com.vbforge.jwtsecurity.service;

import com.vbforge.jwtsecurity.repository.UserRepository;
import com.vbforge.jwtsecurity.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;

    public void saveUser(String username, String password) {
        userRepository.createUser(username, passwordEncoder.encode(password), "ROLE_USER");
    }

    public String loginUser(String login, String password) {
        String stored = userRepository.findPasswordByUsername(login);
        if(!passwordEncoder.matches(password, stored)) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return jwtUtil.generateToken(login, "ROLE_USER");
    }

    public boolean isUserExists(String login, String password) {
        return userRepository.existUser(login, password);
    }
}
