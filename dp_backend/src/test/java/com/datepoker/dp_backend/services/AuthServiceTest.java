package com.datepoker.dp_backend.services;
import com.datepoker.dp_backend.DTO.*;
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

import java.time.LocalDateTime;
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

    @Test
    void testActivateUser_Success() {
        String email = "john@example.com";
        String code = "123456";

        User user = new User();
        user.setEmail(email);
        user.setActivated(false);
        user.setActivationCode(code);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String result = authService.activateUser(email, code);

        assertTrue(user.isActivated());
        assertNull(user.getActivationCode());
        assertEquals("Account for john@example.com successfully activated!", result);
        verify(userRepository).save(user);
    }

    @Test
    void testActivateUser_InvalidCode_ThrowsException() {
        String email = "john@example.com";

        User user = new User();
        user.setEmail(email);
        user.setActivated(false);
        user.setActivationCode("123456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> authService.activateUser(email, "wrong-code"));

        assertEquals("Invalid activation code", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testActivateUser_AlreadyActivated_ThrowsException() {
        String email = "john@example.com";

        User user = new User();
        user.setEmail(email);
        user.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(IllegalStateException.class,
                () -> authService.activateUser(email, "123456"));

        assertEquals("Account already activated", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_ActivatedUser_Success() {
        String email = "john@example.com";
        String password = "rawPass";
        String hashedPassword = "hashedPass";
        String jwt = "mock.jwt.token";

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email)).thenReturn(jwt);

        LoginRequest request = new LoginRequest(email, password);
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());

        // 🔐 ensure the token is encrypted
        assertNotEquals(jwt, response.getToken());
        assertEquals(jwt, AESEncryptionUtil.decrypt(response.getToken()));
    }

    @Test
    void testLogin_UserNotActivated_ThrowsException() {
        String email = "john@example.com";
        String rawPassword = "hashedPass"; // (raw password used in login request)

        User user = new User();
        user.setEmail(email);
        user.setPassword(rawPassword); // mock stored hashed password
        user.setActivated(false); // ❗️ Not activated

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, rawPassword)).thenReturn(true); // ✅ mock password match

        LoginRequest request = new LoginRequest(email, rawPassword);

        Exception ex = assertThrows(AuthenticationException.class,
                () -> authService.login(request));

        assertEquals("Account not activated. Please check your email.", ex.getMessage());
    }

    @Test
    void testForgotPassword_Success() {
        String email = "john@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(mailService).sendPasswordResetEmail(eq(email), anyString());

        String result = authService.processForgotPassword(new ForgotPasswordRequest(email));

        assertEquals(email, result);
        assertNotNull(user.getResetCode());
        assertNotNull(user.getResetCodeExpiry());
        verify(userRepository).save(user);
        verify(mailService).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.processForgotPassword(new ForgotPasswordRequest("missing@example.com"));
        });

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testForgotPassword_UserNotActivated() {
        String email = "notactivated@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(IllegalStateException.class, () -> {
            authService.processForgotPassword(new ForgotPasswordRequest(email));
        });

        assertEquals("Account not activated", ex.getMessage());
    }

    @Test
    void testResetPassword_Success() {
        String email = "john@example.com";
        String code = "123456";
        String rawNewPassword = "newPass123";
        String encodedPassword = "encodedPass123";

        User user = new User();
        user.setEmail(email);
        user.setActivated(true);
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(rawNewPassword)).thenReturn(encodedPassword);

        ResetPasswordRequest req = new ResetPasswordRequest(email, code, rawNewPassword);
        String result = authService.processResetPassword(req);

        assertEquals(email, result);
        assertNull(user.getResetCode());
        assertNull(user.getResetCodeExpiry());
        assertEquals(encodedPassword, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testResetPassword_InvalidCode() {
        String email = "john@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(true);
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResetPasswordRequest req = new ResetPasswordRequest(email, "wrong-code", "newPass");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.processResetPassword(req);
        });

        assertEquals("Invalid reset code", ex.getMessage());
    }

    @Test
    void testResetPassword_ExpiredCode() {
        String email = "john@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(true);
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().minusMinutes(1)); // expired

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResetPasswordRequest req = new ResetPasswordRequest(email, "123456", "newPass");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.processResetPassword(req);
        });

        assertEquals("Reset code expired", ex.getMessage());
    }

}
