package com.example.ecapi.dto.order;

import com.example.ecapi.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userid,
        List<OrderItemResponse> items,
        BigDecimal total,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
