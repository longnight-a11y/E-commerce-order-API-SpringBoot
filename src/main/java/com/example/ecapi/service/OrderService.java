package com.example.ecapi.service;

import com.example.ecapi.dto.order.CreateOrderRequest;
import com.example.ecapi.dto.order.OrderItemRequest;
import com.example.ecapi.dto.order.OrderItemResponse;
import com.example.ecapi.dto.order.OrderResponse;
import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.entity.*;
import com.example.ecapi.enums.OrderStatus;
import com.example.ecapi.exception.InvalidRequestException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CartRepository;
import com.example.ecapi.repository.OrderRepository;
import com.example.ecapi.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, ProductService productService, ProductRepository productRepository, UserService userService, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.userService = userService;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Jwt jwt){
        User user = productService.getUserIfExists(jwt);
        Order order = newOrder(user);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()){
            Product product = productRepository.findByIdForUpdate(itemRequest.productId())
                    .orElseThrow(()-> new ResourceNotFoundException("Product not found"));
            if(itemRequest.quantity() <= 0){
                throw new InvalidRequestException("Quantity must be greater than 0");
            }
            if(product.getStockQuantity() < itemRequest.quantity()){
                throw new InvalidRequestException("Insufficient stock");
            }
            BigDecimal unitPrice = product.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(unitPrice);

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            total = total.add(subtotal);

            // Reduce the product's stock quantity
            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse checkout(Jwt jwt){
        User user = userService.getUserWithCartIfExists(jwt);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));
        if(cart.getItems().isEmpty()){
            throw new InvalidRequestException("No items in your cart");
        }
        Order order = newOrder(user);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()){
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(()-> new ResourceNotFoundException("Product not found"));
            if(item.getQuantity() <= 0){
                throw new InvalidRequestException("Quantity must be greater than 0");
            }
            if(product.getStockQuantity() < item.getQuantity()){
                throw new InvalidRequestException("Insufficient stock: " + product.getName());
            }
            BigDecimal unitPrice = product.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(unitPrice);

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            // Reduce the product's stock quantity
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());

            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);
        // Remove checked-out items from the cart
        cart.getItems().clear();  // This can remove cartItems because orphanRemoval of items field in cart entity is true
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse buyNow(Jwt jwt, OrderItemRequest request){
        User user = userService.getUserIfExists(jwt);
        Product product = productRepository.findByIdForUpdate(request.productId())
                .orElseThrow(()-> new InvalidRequestException("Product not found"));
        if(request.quantity() <= 0){
            throw new InvalidRequestException("Quantity must be greater than 0");
        }
        if(product.getStockQuantity() < request.quantity()){
            throw new InvalidRequestException("Insufficient stock: " + product.getName());
        }
        Order order = newOrder(user);
        BigDecimal unitPrice = product.getPrice();

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.quantity());
        orderItem.setUnitPrice(unitPrice);

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));

        // Reduce the product's stock quantity
        product.setStockQuantity(product.getStockQuantity() - request.quantity());

        order.getOrderItems().add(orderItem);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public PageResponse<OrderResponse> getOrders(Jwt jwt, int page, int size){
        User user = userService.getUserIfExists(jwt);
        Page<Order> result = orderRepository.findAllByUserId(user.getId(), PageRequest.of(page -1, size));
        List<OrderResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }

    public OrderResponse getSpecificOrder(Jwt jwt, UUID orderId){
        User user = userService.getUserIfExists(jwt);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    // ------------------------------------------------------------------------------------------

    private Order newOrder(User user){
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private OrderResponse toResponse(Order order){
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(orderItem -> {
                    BigDecimal unitPrice = orderItem.getUnitPrice();
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                    return new OrderItemResponse(orderItem.getProduct().getId(), orderItem.getProduct().getName(),
                            orderItem.getQuantity(), unitPrice, subtotal);
                }).toList();
        return new OrderResponse(order.getId(), order.getUser().getId(), orderItems, order.getTotalPrice(),
                order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
