package com.vbforge.jwtspringjap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtSpringSecurityJpaApplication {

    public static void main(String[] args) {
        System.out.println("Jwt Spring Security Jpa Application started!");
        SpringApplication.run(JwtSpringSecurityJpaApplication.class, args);
    }

}
