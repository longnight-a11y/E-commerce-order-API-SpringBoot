package com.example.ecapi.repository;

import com.example.ecapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM users u JOIN FETCH u.cart WHERE u.id = :id")
    Optional<User> findByIdWithCart(@Param("id") UUID id);

    boolean existsByEmail(String email);
}
