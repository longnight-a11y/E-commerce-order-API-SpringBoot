package com.example.ecapi.controller;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.dto.user.UserResponse;
import com.example.ecapi.entity.User;
import com.example.ecapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create User")
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request){
        return userService.createUser(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get My Information")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt){
        return userService.getMe(jwt);
    }
}
