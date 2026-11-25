package com.example.ecapi.fixture;

import com.example.ecapi.entity.Category;

import java.util.UUID;

public final class CategoryFixture {

    private CategoryFixture(){}

    public static Category category(){
        Category category = new Category();
        category.setName("test-category");
        category.setId(UUID.randomUUID());
        return category;
    }
}
