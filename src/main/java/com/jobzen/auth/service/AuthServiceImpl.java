package com.jobzen.auth.service;
import com.jobzen.auth.dto.LoginRequest;
import com.jobzen.auth.dto.RegisterRequest;
import com.jobzen.auth.entity.User;
import com.jobzen.auth.repository.UserRepository;
import com.jobzen.auth.security.RevokedTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jobzen.auth.security.JwtService;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService  {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;

    @Override
    public String register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>());

        userRepository.save(user);

        return "User registered successfully";
    }

    @Override
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtService.generateToken(user.getEmail());
    }

    @Override
    public String logout(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token is required");
        }

        revokedTokenService.revokeToken(token);
        return "User logged out successfully";
    }
}
