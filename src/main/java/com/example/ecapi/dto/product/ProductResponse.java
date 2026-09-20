package com.example.ecapi.dto.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        UUID categoryId,
        UUID sellerId,
        Instant createdAt,
        Instant updatedAt
) {
}
