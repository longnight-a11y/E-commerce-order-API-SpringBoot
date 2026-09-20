package com.example.ecapi.dto.auth;

import java.util.UUID;

public record LoginResponse(
        String token,
        String tokenType
) {
}
