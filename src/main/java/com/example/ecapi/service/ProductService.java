package com.example.ecapi.service;

import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.dto.product.ProductResponse;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CategoryRepository;
import com.example.ecapi.repository.ProductRepository;
import com.example.ecapi.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductCreateRequest request, User user){
        checkWhetherUserExists(user);
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.SELLER){
            throw new AccessDeniedException("Only seller or admin can sell products.");
        }
        // that's it for today!
    }

    private void checkWhetherUserExists(User user){
        if(!userRepository.existsById(user.getId())){
            throw new ResourceNotFoundException("User not found");
        }
    }
}
