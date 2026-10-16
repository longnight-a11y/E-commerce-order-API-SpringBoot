package com.example.ecapi.controller;

import com.example.ecapi.dto.cart.AddCartItemRequest;
import com.example.ecapi.dto.cart.CartResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "Add Item to My Cart")
    public CartResponse addItem(@RequestBody AddCartItemRequest request,
                                @AuthenticationPrincipal Jwt jwt){
        return cartService.addItem(request, jwt);
    }

}
