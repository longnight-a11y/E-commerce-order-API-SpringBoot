package com.example.ecapi.controller;

import com.example.ecapi.dto.cart.AddCartItemRequest;
import com.example.ecapi.dto.cart.CartResponse;
import com.example.ecapi.dto.cart.UpdateCartItemRequest;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "Add Item to My Cart")
    public CartResponse addItem(@RequestBody @Valid AddCartItemRequest request,
                                @AuthenticationPrincipal Jwt jwt){
        return cartService.addItem(request, jwt);
    }

    @GetMapping
    @Operation(summary = "Get My Cart Items")
    public CartResponse getCartItems(@AuthenticationPrincipal Jwt jwt){
        return cartService.getCartItems(jwt);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update Quantity of Cart Item")
    public CartResponse updateQuantity(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID productId,
                                       @RequestBody @Valid UpdateCartItemRequest request){
        return cartService.updateQuantity(jwt, productId, request);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove Cart Item")
    public Map<String, String> removeItem(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable UUID productId){
        return cartService.removeItem(jwt, productId);
    }

}
