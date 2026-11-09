package com.example.ecapi.service;

import com.example.ecapi.entity.User;
import com.example.ecapi.fixture.UserFixture;
import com.example.ecapi.repository.UserRepository;
import com.example.ecapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}
