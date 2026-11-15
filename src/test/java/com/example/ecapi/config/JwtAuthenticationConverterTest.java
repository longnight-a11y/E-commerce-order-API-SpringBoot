package com.example.ecapi.config;

import com.example.ecapi.enums.Role;
import com.example.ecapi.security.JwtServiceTest;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthenticationConverterTest {

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp(){
        SecurityConfig securityConfig = new SecurityConfig();
        converter = securityConfig.jwtAuthenticationConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "CUSTOMER", "SELLER"})
    void convert_roleToAuthority(String role){
        Jwt jwt = createJwt(role);

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertNotNull(authentication);

        List<String> authorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_")).toList();

        assertEquals(List.of("ROLE_" + role), authorities);
    }


    private Jwt createJwt(String role){
        Instant now = Instant.now();
        // Build Jwt directly, so secret key is not required
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("user-id")
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .build();
    }
}
