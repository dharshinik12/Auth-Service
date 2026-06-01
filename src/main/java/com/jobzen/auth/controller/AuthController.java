package com.jobzen.auth.controller;
import com.jobzen.auth.dto.*;
import com.jobzen.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/profile")
    public String profile() {
        return "JWT Authentication Successful";
    }
    @GetMapping("/test")
    public String test() {
        return "Auth Service Working";
    }
}
