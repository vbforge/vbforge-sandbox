package com.vbforge;

import com.vbforge.demo.JwtSigningDemo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * ValidationAndSignApplication
 *
 * Starts the Spring Boot app on port 9192.
 * On startup, runs JwtSigningDemo to print token experiments to the console.
 *
 * REST endpoints are also available — see ValidateTokenController.
 */
@SpringBootApplication
public class ValidationAndSignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationAndSignApplication.class, args);
    }

    /**
     * CommandLineRunner is called once after the application context is ready.
     * We delegate all demo logic to JwtSigningDemo (SRP — single responsibility).
     */
    @Bean
    public CommandLineRunner startupDemo(JwtSigningDemo demo) {
        return args -> demo.run();
    }
}
