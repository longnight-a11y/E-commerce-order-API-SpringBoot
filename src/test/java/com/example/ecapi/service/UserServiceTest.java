package com.example.ecapi.service;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.dto.user.UserResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.User;
import com.example.ecapi.enums.Role;
import com.example.ecapi.exception.ConflictException;
import com.example.ecapi.fixture.CartFixture;
import com.example.ecapi.fixture.UserFixture;
import com.example.ecapi.repository.CartRepository;
import com.example.ecapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private UserCreateRequest request;
    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp(){
        request = UserFixture.request();
        user = UserFixture.customer();
    }

    @Test
    void createUser_success(){
        Cart savedCart = CartFixture.cart(user);
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedpassword");
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);
        when(userRepository.save(any(User.class))).thenReturn(user);
        // Act
        UserResponse response = userService.createUser(request);
        // Assert: Response
        assertEquals(request.username(), response.username());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        // check whether userRepository.save() is called, and captor the saved contents
        verify(userRepository).save(captor.capture());
        User capturedUser = captor.getValue();

        assertEquals(request.username(), capturedUser.getUsername());
        assertEquals("testuser@example.com", capturedUser.getEmail());
        assertEquals("hashedpassword", capturedUser.getPassword());
        assertEquals(Role.CUSTOMER, capturedUser.getRole());

        // Check Cart passed to cartRepository.save()
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart capturedCart = cartCaptor.getValue();

        assertSame(user, capturedCart.getUser());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void createUser_emailAlreadyExists(){
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(true);
        // Assert & Act
        ConflictException exception = assertThrows(ConflictException.class, ()->userService.createUser(request));
        assertEquals("User already exists", exception.getMessage());
        verify(userRepository).existsByEmail(request.email());
        // Check whether these methods were never called
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }
}
