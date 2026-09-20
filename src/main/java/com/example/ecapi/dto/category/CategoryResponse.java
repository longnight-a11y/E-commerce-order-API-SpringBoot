package com.example.ecapi.dto.category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name
) {
}
