package com.example.ecapi.security;

import com.example.ecapi.exception.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final Clock clock;

    private static final long EXPIRE_MINUTES = 30;

    public JwtService(@Value("${app.jwt.secret}") String key, Clock clock){
        this.key = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    public String createToken(UUID userId){

        Instant now = clock.instant();
        Instant expiry = now.plus(Duration.ofMinutes(EXPIRE_MINUTES));

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public UUID decodeToken(String token){
        try {
            String sub = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return UUID.fromString(sub);
        } catch (ExpiredJwtException e){
            throw new InvalidTokenException("Token has expired", e);
        } catch (JwtException | IllegalArgumentException e){
            throw new InvalidTokenException("Invalid token", e);
        }
    }
}
