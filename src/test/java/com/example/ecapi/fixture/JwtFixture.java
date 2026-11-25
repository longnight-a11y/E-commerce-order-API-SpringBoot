package com.example.ecapi.fixture;

import com.example.ecapi.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

public final class JwtFixture {

    private JwtFixture(){}

    public static Jwt jwt(User user){
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(user.getId().toString())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .build();
    }
}
