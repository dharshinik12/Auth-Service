package com.jobzen.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateTokenShouldContainEmailAndBeValidBeforeExpiry() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mysecretkeymysecretkeymysecretkey12345");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        String token = jwtService.generateToken("user@example.com");

        assertEquals("user@example.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "user@example.com"));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void tokenShouldBeExpiredWhenExpirationIsInThePast() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mysecretkeymysecretkeymysecretkey12345");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);

        String token = jwtService.generateToken("user@example.com");

        assertTrue(jwtService.isTokenExpired(token));
        assertTrue(jwtService.extractExpiration(token).before(new Date()));
        assertFalse(jwtService.isTokenValid(token, "user@example.com"));
    }
}
