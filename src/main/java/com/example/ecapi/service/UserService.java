package com.example.ecapi.service;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.dto.user.UserResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.ConflictException;
import com.example.ecapi.exception.ResourceNotFoundException;
import com.example.ecapi.repository.CartRepository;
import com.example.ecapi.repository.CategoryRepository;
import com.example.ecapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new ConflictException("User already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(Role.CUSTOMER);
        User saved = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(saved);
        Cart savedCart = cartRepository.save(cart);
        
        saved.setCart(savedCart);

        return toResponse(saved);
    }

    public UserResponse getMe(Jwt jwt){
        UUID userId = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));
        return toResponse(user);
    }

    public UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
