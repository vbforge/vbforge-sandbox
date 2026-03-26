package com.vbforge.jwtspringjpa.config;

import com.vbforge.jwtspringjpa.entity.Role;
import com.vbforge.jwtspringjpa.entity.User;
import com.vbforge.jwtspringjpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;



    @Override
    public void run(ApplicationArguments args) {

        //check if any admin already exist
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            log.info("Admin account already exists — skipping bootstrap.");
            return;
        }

        //if no admin yet create one
        User admin = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_ADMIN)
                .build();

        //save into db
        userRepository.save(admin);
        log.info("Bootstrap: admin account '{}' created.", username);

    }
}
