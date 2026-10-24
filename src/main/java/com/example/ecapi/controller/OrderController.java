package com.example.ecapi.controller;

import com.example.ecapi.dto.order.CreateOrderRequest;
import com.example.ecapi.dto.order.OrderResponse;
import com.example.ecapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create Order")
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request,
                                     @AuthenticationPrincipal Jwt jwt){
        return orderService.createOrder(request, jwt);
    }
    @PostMapping("/checkout")
    @Operation(summary = "Checkout All Cart Items")
    public OrderResponse checkout(@AuthenticationPrincipal Jwt jwt){
        return orderService.checkout(jwt);
    }
}
