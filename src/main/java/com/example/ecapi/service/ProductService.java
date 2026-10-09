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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    public ProductResponse createProduct(ProductCreateRequest request, Jwt jwt){
        User user = getUserIfExists(jwt);
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
        Page<Product> result = productRepository.findAllWithUserAndCategory(PageRequest.of(page - 1, size));
        List<ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }

    public ProductResponse getSingleProduct(UUID productId){
        return toResponse(getProductIfExists(productId));
    }

    public  PageResponse<ProductResponse> getProductsOfSpecificSeller(int page, int size, UUID sellerId){
        Page<Product> result = productRepository.findAllBySellerIdWithUserAndCategory(sellerId, PageRequest.of(page - 1, size));
        List<ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }

    public PageResponse<ProductResponse> getProductsOfSpecificCategory(int page, int size, UUID categoryId) {
        Page<Product> result = productRepository.findAllByCategoryIdWithUserAndCategory(categoryId, PageRequest.of(page - 1, size));
        List<ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, result.getNumberOfElements(), page, size);
    }

    // --------------------------------------------------------------------------

    private Product getProductIfExists(UUID productId){
        return productRepository.findByIdWithUserAndCategory(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found"));
    }

    User getUserIfExists(Jwt jwt){
        UUID userId = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));
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
