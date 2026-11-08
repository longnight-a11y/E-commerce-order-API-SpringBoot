package com.example.ecapi.fixture;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;

import java.util.UUID;

public final class UserFixture {

    private UserFixture(){}  // prevents instantiation because all methods here are static

    public static User customer(){
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Mikasa");
        user.setPassword("testpassword");
        user.setEmail("testuser@example.com");
        user.setRole(Role.CUSTOMER);
        user.setCart(CartFixture.cart(user));
        return user;
    }

    public static UserCreateRequest request(){
        return new UserCreateRequest("Mikasa", "testuser@example.com", "testpassword");
    }
}
