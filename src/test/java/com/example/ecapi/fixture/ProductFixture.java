package com.example.ecapi.fixture;

import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.entity.Category;
import com.example.ecapi.entity.Product;
import com.example.ecapi.entity.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ProductFixture {

    private ProductFixture(){}

    public static Product product(User user, Category category, Instant now){
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("This is for test");
        product.setStockQuantity(100);
        product.setPrice(BigDecimal.valueOf(1000));
        product.setCategory(category);
        product.setSeller(user);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return product;
    }

    public static ProductCreateRequest request(Category category){
        return new ProductCreateRequest("Test Product Name", "This is a test product.",
                new BigDecimal(100), 10, category.getId());
    }
}
