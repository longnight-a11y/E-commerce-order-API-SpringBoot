package com.example.ecapi.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateRequest(
        @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @DecimalMin(value = "0.01") BigDecimal price,
        @PositiveOrZero Integer stockQuantity,
        UUID categoryId
) {
}
