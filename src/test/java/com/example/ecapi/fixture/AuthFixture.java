package com.example.ecapi.fixture;

import com.example.ecapi.dto.auth.LoginRequest;
import com.example.ecapi.entity.User;

public final class AuthFixture {

    private AuthFixture(){}

    public static LoginRequest request(String email){
        return new LoginRequest(email, "testpassword");
    }
}
