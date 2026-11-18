package com.example.ecapi.service;

import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.dto.product.ProductResponse;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.AccessDeniedException;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

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

    @BeforeEach
    void setUp(){
        user = UserFixture.user(Role.SELLER);
        jwt = JwtFixture.jwt(user);
        category = CategoryFixture.category();
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
}
