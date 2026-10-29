package com.example.ecapi.service;

import com.example.ecapi.dto.cart.AddCartItemRequest;
import com.example.ecapi.dto.cart.CartItemResponse;
import com.example.ecapi.dto.cart.CartResponse;
import com.example.ecapi.dto.cart.UpdateCartItemRequest;
import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.CartItem;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.exception.InvalidRequestException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CartItemRepository;
import com.example.ecapi.repository.CartRepository;
import com.example.ecapi.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductService productService, CartItemRepository cartItemRepository, UserService userService, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request, Jwt jwt){
        User user = userService.getUserIfExists(jwt);
        if(request.quantity() <= 0){
            throw new InvalidRequestException("Quantity must be greater than 0");
        }
        Product product = productService.getProductIfExists(request.itemId());
        if(request.quantity() >= product.getStockQuantity()){
            throw new InvalidRequestException("Insufficient stock: " + product.getName());
        }
        Cart cart = getCartIfExists(user);

        // Check whether the requested item already exists in a cart
        CartItem item = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(product.getId())).findFirst()
                .orElse(null);
        if(item == null){
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            newItem.setCart(cart);

            cartItemRepository.save(newItem);  // this is necessary for generating newItem's id before returning the response
            cart.getItems().add(newItem);
        }else {
            item.setQuantity(item.getQuantity() + request.quantity());
        }
        return toResponse(cart);
    }

    public CartResponse getCartItems(Jwt jwt){
        User user = userService.getUserIfExists(jwt);
        Cart cart = getCartIfExists(user);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Jwt jwt, UUID productId, UpdateCartItemRequest request){
        User user = userService.getUserIfExists(jwt);
        Product product = productService.getProductIfExistsForUpdate(productId);
        Cart cart = getCartIfExists(user);
        CartItem item = cart.getItems().stream().filter(i-> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(()-> new ResourceNotFoundException("The item not found in your cart"));
        if(request.quantity() <= 0){
            throw new InvalidRequestException("Quantity must be greater than 0");
        }
        if(request.quantity() >= product.getStockQuantity()){
            throw new InvalidRequestException("Insufficient stock: " + product.getName());
        }
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public Map<String, String> removeItem(Jwt jwt, UUID productId){
        User user = userService.getUserIfExists(jwt);
        Cart cart = getCartIfExists(user);
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found"));
        CartItem item = cart.getItems().stream().filter(i-> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(()-> new ResourceNotFoundException("The item not found in your cart"));
        cartItemRepository.delete(item);
        cartItemRepository.save(item);
        return Map.of("detail", "Cart item [ " + product.getName() + " ] was removed successfully.");
    }

    // -----------------------------------------------------------------------------------

    private Cart getCartIfExists(User user){
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponse toResponse(Cart cart){
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {  // This map converts each item into CartItemResponse
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
