package com.example.ecapi.service;

import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.dto.product.ProductResponse;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CategoryRepository;
import com.example.ecapi.repository.ProductRepository;
import com.example.ecapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, User user){
        checkWhetherUserExists(user);
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.SELLER){
            throw new AccessDeniedException("Only seller or admin can sell products.");
        }
        Category category = getCategoryIfExists(request.categoryId());
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
        product.setSeller(user);
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public PageResponse<ProductResponse> getProducts(int page, int size){
        Page<Product> result = productRepository.findAll(PageRequest.of(page - 1, size));
        List<ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }


    private void checkWhetherUserExists(User user){
        if(!userRepository.existsById(user.getId())){
            throw new ResourceNotFoundException("User not found");
        }
    }
    private Category getCategoryIfExists(UUID categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category not found"));
    }

    private ProductResponse toResponse(Product product){
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStockQuantity(), product.getCategory().getId(),
                product.getSeller().getId(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
