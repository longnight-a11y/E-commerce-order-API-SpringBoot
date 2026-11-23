package com.example.ecapi.service;

import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.dto.product.ProductResponse;
import com.example.ecapi.dto.product.ProductUpdateRequest;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.fixture.CategoryFixture;
import com.example.ecapi.fixture.JwtFixture;
import com.example.ecapi.fixture.ProductFixture;
import com.example.ecapi.fixture.UserFixture;
import com.example.ecapi.repository.CategoryRepository;
import com.example.ecapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductService productService;

    private User user;
    private Jwt jwt;
    private Category category;
    private Product product;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp(){
        user = UserFixture.user(Role.SELLER);
        jwt = JwtFixture.jwt(user);
        category = CategoryFixture.category();
        product = ProductFixture.product(user, category, NOW);
    }

    @Test
    void createProduct_success(){
        ProductCreateRequest request = ProductFixture.request(category);
        when(userService.getUserIfExists(jwt)).thenReturn(user);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        ProductResponse response = productService.createProduct(request, jwt);
        // Assert (for response)
        assertNotNull(response);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product capturedProduct = captor.getValue();
        // Assert (saved product)
        assertNotNull(capturedProduct);
        assertEquals(request.name(), capturedProduct.getName());
        assertEquals(request.description(), capturedProduct.getDescription());
        assertEquals(request.price(), capturedProduct.getPrice());
        assertEquals(request.stockQuantity(), capturedProduct.getStockQuantity());
        assertEquals(request.categoryId(), capturedProduct.getCategory().getId());

        assertSame(user, capturedProduct.getSeller());

        verify(userService).getUserIfExists(jwt);
        verify(categoryRepository).findById(category.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_customerTryToSell(){
        user = UserFixture.user(Role.CUSTOMER);
        jwt = JwtFixture.jwt(user);
        ProductCreateRequest request = ProductFixture.request(category);
        when(userService.getUserIfExists(jwt)).thenReturn(user);
        // Act & Throw
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, ()-> productService.createProduct(request, jwt));
        assertEquals("Only seller or admin can sell products.", exception.getMessage());

        verifyNoInteractions(categoryRepository, productRepository);
    }

    @Test
    void createProduct_categoryNotFound(){
        ProductCreateRequest request = ProductFixture.request(category);
        when(userService.getUserIfExists(jwt)).thenReturn(user);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());
        // Act & Throw
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, ()-> productService.createProduct(request, jwt));

        assertEquals("Category not found", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProducts_success(){
        Product product2 = ProductFixture.product(user, category, NOW);
        Page<Product> page = new PageImpl<>(List.of(product, product2),
                PageRequest.of(0, 10), 25);
        when(productRepository.findAllWithUserAndCategory(PageRequest.of(0, 10))).thenReturn(page);
        // Act
        PageResponse<ProductResponse> response = productService.getProducts(1, 10);
        // Assert
        assertEquals(2, response.item().size());  // number of products in the current page (page 1)
        assertEquals(25, response.total());  // total number of products across all pages
        assertEquals(1, response.page());
        assertEquals(10, response.size());  // maximum number of products per page

        assertEquals("Test Product", response.item().getFirst().name());

        verify(productRepository).findAllWithUserAndCategory(PageRequest.of(0, 10));
    }

    @Test
    void getSingleProduct_success(){
        when(productRepository.findByIdWithUserAndCategory(product.getId())).thenReturn(Optional.of(product));
        // Act
        ProductResponse response = productService.getSingleProduct(product.getId());

        assertEquals("Test Product", response.name());
        assertEquals("This is for test", response.description());
        assertEquals(100, response.stockQuantity());
        assertEquals(BigDecimal.valueOf(1000), response.price());
        assertEquals(category.getId(), response.categoryId());
        assertEquals(user.getId(), response.sellerId());
        assertEquals(NOW, response.createdAt());
        assertEquals(NOW, response.updatedAt());

        verify(productRepository).findByIdWithUserAndCategory(product.getId());
    }

    @Test
    void getSingleProduct_NotFound(){
        when(productRepository.findByIdWithUserAndCategory(product.getId())).thenReturn(Optional.empty());
        // Act & Throw
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, ()-> productService.getSingleProduct(product.getId()));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getProductsOfSpecificSeller_success(){
        Product product2 = ProductFixture.product(user, category, NOW);
        Page<Product> page = new PageImpl<>(List.of(product, product2),
                PageRequest.of(0, 10), 2);
        when(productRepository.
                findAllBySellerIdWithUserAndCategory(user.getId(), PageRequest.of(0, 10))).thenReturn(page);
        // Act
        PageResponse<ProductResponse> response = productService.getProductsOfSpecificSeller(1, 10, user.getId());
        // Assert
        assertEquals(2, response.item().size());
        assertEquals(2, response.total());
        assertEquals(1, response.page());
        assertEquals(10, response.size());

        assertEquals("Test Product", response.item().getFirst().name());
        assertEquals(user.getId(), response.item().getFirst().sellerId());

        verify(productRepository).findAllBySellerIdWithUserAndCategory(user.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getProductsOfSpecificCategory_success(){
        Page<Product> page = new PageImpl<>(List.of(product),
                PageRequest.of(0, 10), 1);
        when(productRepository.
                findAllByCategoryIdWithUserAndCategory(category.getId(), PageRequest.of(0, 10))).thenReturn(page);
        // Act
        PageResponse<ProductResponse> response = productService.getProductsOfSpecificCategory(1, 10, category.getId());
        // Assert
        assertEquals(1, response.item().size());
        assertEquals(1, response.total());
        assertEquals(1, response.page());
        assertEquals(10, response.size());

        assertEquals("Test Product", response.item().getFirst().name());
        assertEquals(category.getId(), response.item().getFirst().categoryId());
        assertTrue(response.item().stream()
                .allMatch(item -> item.categoryId().equals(category.getId())));

        verify(productRepository).findAllByCategoryIdWithUserAndCategory(category.getId(), PageRequest.of(0, 10));
    }

    @Test
    void updateProduct_success(){
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Product", "Updated Description",
                BigDecimal.valueOf(2000), null, null);
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(userService.getUserIfExists(jwt)).thenReturn(user);
        // Act
        ProductResponse response = productService.updateProduct(request, product.getId(), jwt);
        // Assert
        assertEquals("Updated Product", response.name());
        assertEquals("Updated Description", response.description());
        assertEquals(BigDecimal.valueOf(2000), response.price());
        assertEquals(100, response.stockQuantity());
        assertEquals(category.getId(), response.categoryId());

        verify(productRepository).findByIdForUpdate(product.getId());
        verify(userService).getUserIfExists(jwt);
        verify(productRepository, never()).save(product);  // 必須ではない
    }

    @Test
    void updateProduct_success_adminCanUpdateOthersProduct(){
        User admin = UserFixture.user(Role.ADMIN);
        jwt = JwtFixture.jwt(admin);
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Product", "Updated Description",
                BigDecimal.valueOf(2000), null, null);
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(userService.getUserIfExists(jwt)).thenReturn(admin);
        // Act
        ProductResponse response = productService.updateProduct(request, product.getId(), jwt);
        // Assert
        assertEquals("Updated Product", response.name());
        assertEquals("Updated Description", response.description());
        assertEquals(BigDecimal.valueOf(2000), response.price());
        assertEquals(100, response.stockQuantity());
        assertEquals(category.getId(), response.categoryId());

        assertEquals(user.getId(), response.sellerId());

        verify(productRepository).findByIdForUpdate(product.getId());
        verify(userService).getUserIfExists(jwt);
        verify(productRepository, never()).save(product);
    }

    @Test
    void updateProduct_sellerTryToUpdateOthersProduct(){
        User anotherUser = UserFixture.user(Role.SELLER);
        Jwt anotherJwt = JwtFixture.jwt(anotherUser);
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Product", "Updated Description",
                BigDecimal.valueOf(2000), null, null);
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(userService.getUserIfExists(anotherJwt)).thenReturn(anotherUser);
        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                ()-> productService.updateProduct(request, product.getId(), anotherJwt));

        assertEquals("Not authorized to update this product", exception.getMessage());
        assertNotEquals("Updated Product", product.getName());
        verify(productRepository).findByIdForUpdate(product.getId());
        verify(userService).getUserIfExists(anotherJwt);
        verify(productRepository, never()).save(product);
    }

    @Test
    void updateProduct_productNotFound(){
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Product", "Updated Description",
                BigDecimal.valueOf(2000), null, null);
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.empty());
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()-> productService.updateProduct(request, product.getId(), jwt));
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findByIdForUpdate(product.getId());
        verifyNoInteractions(userService);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_categoryNotFound(){
        UUID missingCategoryId = UUID.randomUUID();
        ProductUpdateRequest request = new ProductUpdateRequest(
                null, null, null, null, missingCategoryId);
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(userService.getUserIfExists(jwt)).thenReturn(user);
        when(categoryRepository.findById(missingCategoryId)).thenReturn(Optional.empty());
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()-> productService.updateProduct(request, product.getId(), jwt));
        assertEquals("Category not found", exception.getMessage());

        verify(productRepository).findByIdForUpdate(product.getId());
        verify(userService).getUserIfExists(jwt);
        verify(categoryRepository).findById(missingCategoryId);
        verify(productRepository, never()).save(product);
    }

    // updateProduct_allFieldsAreNull

}
