package com.example.ecapi.service;

import com.example.ecapi.dto.cart.AddCartItemRequest;
import com.example.ecapi.dto.cart.CartItemResponse;
import com.example.ecapi.dto.cart.CartResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.CartItem;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request, Jwt jwt){
        User user = productService.getUserIfExists(jwt);
        Product product = productService.getProductIfExists(request.itemId());
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));

        // Check whether the requested item already exists in a cart
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(product.getId())).findFirst()
                .orElse(null);
        if(item == null){
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            cart.getItems().add(newItem);
        }else {
            item.setQuantity(item.getQuantity() + request.quantity());
        }
        return toResponse(cart);
    }

    // -----------------------------------------------------------------------------------

    private CartResponse toResponse(Cart cart){
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getProduct().getPrice();
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(item.getId(), item.getProduct().getId(),
                            item.getProduct().getName(), unitPrice, item.getQuantity(), subtotal);
                }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), cart.getUser().getId(), items, total);
    }
}
