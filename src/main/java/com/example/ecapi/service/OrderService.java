package com.example.ecapi.service;

import com.example.ecapi.dto.order.CreateOrderRequest;
import com.example.ecapi.dto.order.OrderItemRequest;
import com.example.ecapi.dto.order.OrderItemResponse;
import com.example.ecapi.dto.order.OrderResponse;
import com.example.ecapi.entity.Order;
import com.example.ecapi.entity.OrderItem;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.OrderStatus;
import com.example.ecapi.exception.InvalidRequestException;
import com.example.ecapi.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Jwt jwt){
        User user = productService.getUserIfExists(jwt);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()){
            Product product = productService.getProductIfExists(itemRequest.productId());
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
            total.add(subtotal);

            // Reduce the product's stock quantity
            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------------------------------

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
