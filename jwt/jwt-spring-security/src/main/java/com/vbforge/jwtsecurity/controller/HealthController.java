package com.vbforge.jwtsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    //http://localhost:8080/api/health
    @GetMapping
    public String healthCheck() {
        return "App running!";
    }
}
