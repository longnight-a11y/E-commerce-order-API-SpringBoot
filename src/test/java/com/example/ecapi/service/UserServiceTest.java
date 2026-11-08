package com.example.ecapi.service;

import com.example.ecapi.dto.user.UserCreateRequest;
import com.example.ecapi.dto.user.UserResponse;
import com.example.ecapi.entity.Cart;
import com.example.ecapi.entity.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedpassword");
        when(cartRepository.save(any(Cart.class))).thenReturn(user.getCart());
        when(userRepository.save(any(User.class))).thenReturn(user);
        // Act
        UserResponse response = userService.createUser(request);
        // Assert: Response
        assertEquals(request.username(), response.username());
        // Assert: Repository Argument
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        // check whether userRepository.save() is called, and captor the saved contents
        verify(userRepository).save(captor.capture());
        verify(cartRepository).save(cartCaptor.capture());

        User captoredUser = captor.getValue();
        Cart captoredCart = cartCaptor.getValue();

        assertEquals("Mikasa", captoredUser.getUsername());
        assertEquals("testuser@example.com", captoredUser.getEmail());
        assertEquals("hashedpassword", captoredUser.getPassword());
        assertEquals(user.getId(), captoredCart.getUser().getId());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
    }
}
