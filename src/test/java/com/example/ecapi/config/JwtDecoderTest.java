package com.example.ecapi.config;

import com.example.ecapi.exception.InvalidCredentialsException;
import com.example.ecapi.exception.InvalidTokenException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtDecoderTest {

    private JwtDecoder jwtDecoder;
    private SecretKey key;

    @BeforeEach
    void setUp(){
        key = Keys.hmacShaKeyFor("this-is-a-very-long-secret-key-for-testing-purposes".getBytes(StandardCharsets.UTF_8));
        SecurityConfig securityConfig = new SecurityConfig();
        jwtDecoder = securityConfig.jwtDecoder(key);
    }

    @Test
    void jwtDecoder_success(){
        Instant now = Instant.now();
        String token = createToken(key, now, now.plusSeconds(600));

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("test-user-id", jwt.getSubject());
        assertEquals("CUSTOMER", jwt.getClaimAsString("role"));
    }

    @Test
    void jwtDecoder_wrongKey(){
        Instant now = Instant.now();
        SecretKey wrongKey = Keys.hmacShaKeyFor("this-is-a-very-long-another-test-secret-key-for-testing-purposes".
                getBytes(StandardCharsets.UTF_8));
        String token = createToken(wrongKey, now, now.plusSeconds(600));

        assertThrows(BadJwtException.class, ()-> jwtDecoder.decode(token));
    }

    @Test
    void jwtDecoder_expiredToken(){
        Instant now = Instant.now();
        String token = createToken(key, now.minusSeconds(1200), now.minusSeconds(600));

        assertThrows(BadJwtException.class, ()-> jwtDecoder.decode(token));
    }


    private String createToken(SecretKey key, Instant issuedAt, Instant expiration){
        return Jwts.builder()
                .subject("test-user-id")
                .claim("role", "CUSTOMER")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)  // must specify HS256
                .compact();
    }
}
