package com.example.ecapi.controller;

import com.example.ecapi.dto.order.CreateOrderRequest;
import com.example.ecapi.dto.order.OrderItemRequest;
import com.example.ecapi.dto.order.OrderResponse;
import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create Order")
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request,
                                     @AuthenticationPrincipal Jwt jwt){
        return orderService.createOrder(request, jwt);
    }
    @PostMapping("/checkout")
    @Operation(summary = "Checkout All Cart Items")
    public OrderResponse checkout(@AuthenticationPrincipal Jwt jwt){
        return orderService.checkout(jwt);
    }

    @PostMapping("/buy-now")
    @Operation(summary = "Buy a Product Right Now")
    public OrderResponse buyNow(@AuthenticationPrincipal Jwt jwt,
                                @RequestBody @Valid OrderItemRequest request){
        return orderService.buyNow(jwt, request);
    }

    @GetMapping
    @Operation(summary = "Get My Orders")
    public PageResponse<OrderResponse> getOrders(@AuthenticationPrincipal Jwt jwt,
                                                 @RequestParam(defaultValue = "1") @Min(value = 1) int page,
                                                 @RequestParam(defaultValue = "10") @Max(value = 50) int size){
        return orderService.getOrders(jwt, page, size);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get an Order")
    public OrderResponse getSpecificOrder(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable UUID orderId){
        return orderService.getSpecificOrder(jwt, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an Order")
    public Map<String, String> cancelOrder(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID orderId){
        return orderService.cancelOrder(jwt, orderId);
    }
}
