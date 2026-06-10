package com.jobzen.auth.service;

import com.jobzen.auth.dto.LoginRequest;
import com.jobzen.auth.dto.RegisterRequest;
import com.jobzen.auth.entity.User;
import com.jobzen.auth.repository.UserRepository;
import com.jobzen.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerShouldSaveNewUserAndReturnSuccessMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        String result = authService.register(request);

        assertEquals("User registered successfully", result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(org.mockito.Mockito.any(User.class));
        verify(passwordEncoder, never()).encode(org.mockito.Mockito.anyString());
    }

    @Test
    void loginShouldReturnJwtTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);
        when(jwtService.generateToken("user@example.com"))
                .thenReturn("jwt-token");

        String result = authService.login(request);

        assertEquals("jwt-token", result);
        verify(jwtService).generateToken("user@example.com");
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("User not found", exception.getMessage());
        verify(passwordEncoder, never()).matches(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString());
        verify(jwtService, never()).generateToken(org.mockito.Mockito.anyString());
    }

    @Test
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid password", exception.getMessage());
        verify(jwtService, never()).generateToken(org.mockito.Mockito.anyString());
    }
}
