package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.datepoker.dp_backend.services.AuthService;
import com.datepoker.dp_backend.services.MailService;
import com.datepoker.dp_backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository userRepository;

    private final AuthService authService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, AuthService authService, MailService mailService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.mailService = mailService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody EncryptionRequest request) {
        try {
            String decryptedJson = AESEncryptionUtil.decrypt(request.getPayload().asText());

            RegisterRequest registerRequest = objectMapper.readValue(decryptedJson, RegisterRequest.class);

            LOGGER.info("New (encrypted) registration request for {}", registerRequest.getEmail());

            String responseMessage = authService.registerUser(registerRequest);
            return ResponseEntity.ok(ApiResponse.success(responseMessage, null));

        } catch (Exception e) {
            LOGGER.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed", new ApiError(400, "Bad Request", e.getMessage())));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody EncryptionRequest request) {
        try {
            // 🔓 Decrypt the encrypted request payload
            String decryptedJson = AESEncryptionUtil.decrypt(request.getPayload().asText());

            // 🧾 Convert decrypted JSON string into LoginRequest
            LoginRequest loginRequest = objectMapper.readValue(decryptedJson, LoginRequest.class);

            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Login failed", new ApiError(400, "Bad Request", e.getMessage()))
            );
        }
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@Valid @RequestBody SocialAuthRequest request) {
        LoginResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@RequestBody EncryptionRequest request) {
        String result = authService.processEncryptedActivation(request);
        return ResponseEntity.ok(ApiResponse.success("Account activated!", result));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody EncryptionRequest request) {
        ForgotPasswordRequest decrypted = authService.decryptPayload(request.getPayload(), ForgotPasswordRequest.class);
        String result = authService.processForgotPassword(decrypted);
        return ResponseEntity.ok(ApiResponse.success("Reset code sent to email", result));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody EncryptionRequest request) {
        ResetPasswordRequest decrypted = authService.decryptPayload(request.getPayload(), ResetPasswordRequest.class);
        String result = authService.processResetPassword(decrypted);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", result));
    }




}
