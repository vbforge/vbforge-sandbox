package com.vbforge.jwtsecurity.controller;

import com.vbforge.jwtsecurity.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/data")
public class ApiController {

    @GetMapping()
    public Map<String, String> getData(HttpServletRequest request, HttpServletResponse response) {




        return Map.of("key", "value");


    }

}

