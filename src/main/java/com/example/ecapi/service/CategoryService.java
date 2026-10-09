package com.example.ecapi.service;

import com.example.ecapi.dto.category.CategoryCreateRequest;
import com.example.ecapi.dto.category.CategoryResponse;
import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
import com.example.ecapi.exception.ConflictException;
import com.example.ecapi.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CategoryService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request, Jwt jwt){
        User user = productService.getUserIfExists(jwt);
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.SELLER){
            throw new AccessDeniedException("Only seller or admin can create new categories.");
        }
        if(categoryRepository.existsByName(request.name())){
            throw new ConflictException("Category already exists");
        }
        Category category = new Category();
        category.setName(request.name());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    public PageResponse<CategoryResponse> getCategories(int page, int size){
        Page<Category> result = categoryRepository.findAll(PageRequest.of(page - 1, size));
        List<CategoryResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }

    // add PATCH

    // -------------------------------------------------------------------------------------------------

    private CategoryResponse toResponse(Category category){
        return new CategoryResponse(category.getId(), category.getName());
    }
}
