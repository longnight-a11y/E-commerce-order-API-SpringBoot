package com.example.ecapi.controller;

import com.example.ecapi.dto.category.CategoryCreateRequest;
import com.example.ecapi.dto.category.CategoryResponse;
import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Create New Category")
    public CategoryResponse createCategory(@RequestBody @Valid CategoryCreateRequest request, @AuthenticationPrincipal Jwt jwt){
        return categoryService.createCategory(request, jwt);
    }

    @GetMapping
    @Operation(summary = "Get All Categories")
    public PageResponse<CategoryResponse> getCategories(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "50") int size){
        return categoryService.getCategories(page, size);
    }
}
