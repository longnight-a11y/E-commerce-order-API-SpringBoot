package com.example.ecapi.controller;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.dto.user.UserResponse;
import com.example.ecapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
