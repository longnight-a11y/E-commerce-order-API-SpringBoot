package com.example.ecapi.dto.order;

import jakarta.validation.constraints.Email;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        @Email String email
) {
}
