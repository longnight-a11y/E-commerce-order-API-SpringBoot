package com.example.ecapi.service;


import com.example.ecapi.enums.Role;
import com.example.ecapi.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey key;
    private Clock clock;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp(){
        key = Keys.hmacShaKeyFor("this-is-a-very-long-secret-key-for-testing-purposes".getBytes(StandardCharsets.UTF_8));
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        jwtService = new JwtService(key, clock);
    }

    @Test
    void createToken_success(){
        UUID userId = UUID.randomUUID();
        Role role = Role.CUSTOMER;
        // Act
        String token = jwtService.createToken(userId, role);
        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .clock(()-> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(role.name(), claims.get("role", String.class));
    }
}
