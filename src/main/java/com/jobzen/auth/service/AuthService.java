package com.jobzen.auth.service;
import com.jobzen.auth.dto.LoginRequest;
import com.jobzen.auth.dto.RegisterRequest;
public interface AuthService {
    String register(RegisterRequest request);

    String login(LoginRequest request);
}
