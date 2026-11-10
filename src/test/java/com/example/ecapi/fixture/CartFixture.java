package com.example.ecapi.fixture;

import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.User;

import java.util.UUID;

public final class CartFixture {

    private CartFixture(){}

    public static Cart cart(User user){
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);
        return cart;
    }
}
