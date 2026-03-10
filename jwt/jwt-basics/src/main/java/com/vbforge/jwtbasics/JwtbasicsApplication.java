package com.vbforge.jwtbasics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Project 1 — JWT Basics
 *
 * Demonstrates:
 *  - Stateless authentication using JSON Web Tokens (JWT)
 *  - Spring Security 6 filter chain configuration
 *  - Custom JwtFilter (OncePerRequestFilter)
 *  - Token generation and validation with JJWT (HS256)
 *  - User registration and login flows
 */
@SpringBootApplication
public class JwtbasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtbasicsApplication.class, args);
    }

}
