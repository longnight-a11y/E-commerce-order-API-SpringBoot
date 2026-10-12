package com.example.ecapi.service;

import com.example.ecapi.dto.category.CategoryResponse;
import com.example.ecapi.dto.category.CategoryUpdateRequest;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
import com.example.ecapi.exception.ConflictException;
import com.example.ecapi.exception.InvalidRequestException;
import com.example.ecapi.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public AdminService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Transactional
    public CategoryResponse updateCategory(CategoryUpdateRequest request, UUID categoryId, Jwt jwt){
        User user = productService.getUserIfExists(jwt);
        if(user.getRole() != Role.ADMIN){
            throw new AccessDeniedException("Only admin can update category");
        }
        if(categoryRepository.existsByName(request.name())){
            throw new ConflictException("The same name is already used for another category");
        }

        Category category = productService.getCategoryIfExists(categoryId);
        if(request.name() == null){
            throw new InvalidRequestException("No field is provided");
        } else {
            if(request.name().isBlank()){
                throw new InvalidRequestException("Name must not be blank");
            }
        }
        category.setName(request.name());
        return new CategoryResponse(categoryId, category.getName());
    }
}
