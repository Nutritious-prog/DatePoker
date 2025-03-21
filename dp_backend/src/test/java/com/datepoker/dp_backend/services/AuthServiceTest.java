package com.datepoker.dp_backend.services;
import com.datepoker.dp_backend.DTO.RegisterRequest;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.repositories.RoleRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.datepoker.dp_backend.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        // ✅ Arrange
        String email = "john.doe@example.com";
        String password = "Secure123!";
        RegisterRequest mockRequest = new RegisterRequest("John Doe", email, password);
        User mockUser = new User("John Doe", email, "encodedPassword123");

        Role mockRole = new Role(RoleName.ROLE_USER); // ✅ Ensure the role exists
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(mockRole)); // ✅ Mock role retrieval
        when(userRepository.existsByEmail(email)).thenReturn(false); // Email doesn't exist
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword123"); // Mock password encoding
        when(userRepository.save(any(User.class))).thenReturn(mockUser); // ✅ Return a User object

        // 🛠 Act
        String result = authService.registerUser(mockRequest);

        // ✅ Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(any(User.class)); // Ensure save is called once
    }

    @Test
    void testRegisterUser_Failure_EmailExists() {
        // ✅ Arrange
        String email = "john.doe@example.com";
        String password = "Secure123!";
        RegisterRequest mockRequest = new RegisterRequest("John Doe", email, password);

        Role mockRole = new Role(RoleName.ROLE_USER); // ✅ Ensure role exists
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(mockRole)); // ✅ Mock role retrieval

        when(userRepository.existsByEmail(email)).thenReturn(true); // ✅ Email already exists

        // 🛠 Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(mockRequest); // ✅ Now should throw IllegalArgumentException
        });

        assertEquals("Error: Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save is NOT called
    }


}
