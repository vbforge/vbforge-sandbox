package com.vbforge.jwtsecurity.controller;

import com.vbforge.jwtsecurity.dto.LoginDTO;
import com.vbforge.jwtsecurity.dto.SignupDTO;
import com.vbforge.jwtsecurity.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;

    //api/demo/hello
    @GetMapping("/hello")
    public String demo() {
        return "Hello World!";
    }

    //api/demo/signup
    @PostMapping("/signup")
    public void signup(@RequestBody SignupDTO signupDTO) {
        userService.saveUser(signupDTO.getLogin(), signupDTO.getPassword());
    }

    //api/demo/login
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO) {
         return userService.loginUser(loginDTO.getLogin(), loginDTO.getPassword());
    }



}
