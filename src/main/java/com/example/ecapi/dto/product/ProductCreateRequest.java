package com.example.ecapi.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull @PositiveOrZero Integer stockQuantity,
        @NotNull UUID categoryId
        ) {
}
