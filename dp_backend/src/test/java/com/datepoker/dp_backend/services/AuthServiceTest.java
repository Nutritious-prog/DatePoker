package com.datepoker.dp_backend.services;
import com.datepoker.dp_backend.DTO.LoginRequest;
import com.datepoker.dp_backend.DTO.LoginResponse;
import com.datepoker.dp_backend.DTO.RegisterRequest;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.exceptions.AuthenticationException;
import com.datepoker.dp_backend.repositories.RoleRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.datepoker.dp_backend.security.JwtUtil;
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

    @Mock
    private MailService mailService;


    @Mock
    private JwtUtil jwtUtil;

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

        Role mockRole = new Role(RoleName.ROLE_USER);

        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(mockRole));
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // We’re not testing the mail service, just verify it's called
        doNothing().when(mailService).sendActivationEmail(eq(email), anyString(), anyString());

        // 🛠 Act
        String result = authService.registerUser(mockRequest);

        // ✅ Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailService, times(1)).sendActivationEmail(eq(email), anyString(), anyString());
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

    @Test
    void testLogin_Success_ReturnsEncryptedToken() {
        // ✅ Arrange
        String email = "john.doe@example.com";
        String rawPassword = "Secure123!";
        String hashedPassword = "hashedPassword123";
        String jwtToken = "mock-jwt-token";
        String encryptedToken = AESEncryptionUtil.encrypt(jwtToken); // 🔐 Encrypt mock JWT

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(hashedPassword);
        mockUser.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email)).thenReturn(jwtToken);

        // 🛠 Act
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        LoginResponse response = authService.login(loginRequest);

        // ✅ Assert
        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getToken());
        assertNotEquals(jwtToken, response.getToken(), "Token should be encrypted");

        // 🔐 Ensure the token can be decrypted properly
        String decryptedToken = AESEncryptionUtil.decrypt(response.getToken());
        assertEquals(jwtToken, decryptedToken, "Decrypted token should match original JWT");

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
        verify(jwtUtil, times(1)).generateToken(email);
    }


    @Test
    void testLogin_Failure_InvalidCredentials() {
        // ✅ Arrange
        String email = "john.doe@example.com";
        String rawPassword = "WrongPassword";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword("hashedPassword123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, mockUser.getPassword())).thenReturn(false);

        // 🛠 Act & Assert
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, mockUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        // ✅ Arrange
        String email = "nonexistent@example.com";
        String rawPassword = "Secure123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // 🛠 Act & Assert
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

}
