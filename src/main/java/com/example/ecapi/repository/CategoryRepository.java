package com.example.ecapi.repository;

import com.example.ecapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);  // for checking whether the category name already exists
}
