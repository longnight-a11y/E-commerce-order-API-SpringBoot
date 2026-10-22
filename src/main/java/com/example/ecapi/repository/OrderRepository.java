package com.example.ecapi.repository;

import com.example.ecapi.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userid);

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.prduct"})
    Optional<Order> findById(UUID id);
}
