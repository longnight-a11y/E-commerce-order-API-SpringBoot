package com.example.ecapi.repository;

import com.example.ecapi.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userid);

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    Optional<Order> findById(UUID id);

    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.orderItems JOIN FETCH o.orderItems.product WHERE o.user.id = :userId")
    Page<Order> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
