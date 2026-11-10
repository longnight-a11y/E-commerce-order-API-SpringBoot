package com.example.ecapi.service;

import com.example.ecapi.dto.auth.LoginRequest;
import com.example.ecapi.dto.auth.LoginResponse;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.InvalidCredentialsException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.fixture.AuthFixture;
import com.example.ecapi.fixture.UserFixture;
import com.example.ecapi.repository.UserRepository;
import com.example.ecapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp(){
        user = UserFixture.customer();
    }

    @Test
    void login_success(){
        LoginRequest request = AuthFixture.request(user.getEmail());
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.createToken(user.getId(), user.getRole())).thenReturn("jwt-token");
        // Act
        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("bearer", response.tokenType());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
        verify(jwtService).createToken(user.getId(), user.getRole());
    }

    @Test
    void login_emailNotFound(){
        LoginRequest request = AuthFixture.request("notfound@example.com");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, ()-> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).createToken(any(UUID.class), any(Role.class));
    }

    @Test
    void login_wrongPassword(){
        LoginRequest request = new LoginRequest(user.getEmail(), "wrongpassword");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);
        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, ()-> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
        verify(jwtService, never()).createToken(any(UUID.class), any(Role.class));
    }
}
