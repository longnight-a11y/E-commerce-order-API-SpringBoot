package com.example.ecapi.repository;

import com.example.ecapi.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category")
    Page<Product> findAllWithUserAndCategory(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithUserAndCategory(@Param("id") UUID id);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category WHERE p.seller.id = :sellerId")
    Page<Product> findAllBySellerIdWithUserAndCategory(@Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category WHERE p.category.id = :categoryId")
    Page<Product> findAllByCategoryIdWithUserAndCategory(@Param("categoryId") UUID categoryId, Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
}
