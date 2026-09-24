package com.example.ecapi.service;

import com.example.ecapi.dto.auth.LoginRequest;
import com.example.ecapi.dto.auth.LoginResponse;
import com.example.ecapi.entity.User;
import com.example.ecapi.exception.InvalidCredentialsException;
import com.example.ecapi.exception.InvalidTokenException;
import com.example.ecapi.repository.UserRepository;
import com.example.ecapi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()-> new InvalidCredentialsException("Invalid email or password"));
        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return new LoginResponse(jwtService.createToken(user.getId(), user.getRole()), "bearer");
    }
}
