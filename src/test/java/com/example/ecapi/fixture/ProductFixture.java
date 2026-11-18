package com.example.ecapi.fixture;

import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.entity.Category;

import java.math.BigDecimal;

public final class ProductFixture {

    private ProductFixture(){}

    public static ProductCreateRequest request(Category category){
        return new ProductCreateRequest("Test Product Name", "This is a test product.",
                new BigDecimal(100), 10, category.getId());
    }
}
