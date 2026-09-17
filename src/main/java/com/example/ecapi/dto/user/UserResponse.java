package com.example.ecapi.dto.user;

import com.example.ecapi.enums.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Role role
) {
}
