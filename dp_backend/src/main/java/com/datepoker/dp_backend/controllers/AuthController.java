package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthController(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
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
        try {
            // 🔓 Decrypt payload
            String encrypted = request.getPayload().asText(); // from JsonNode
            String decryptedJson = AESEncryptionUtil.decrypt(encrypted);

            // 📦 Convert decrypted JSON to ActivationRequest
            ActivationRequest activationRequest = objectMapper.readValue(decryptedJson, ActivationRequest.class);

            // ✅ Activate the user
            String resultMessage = authService.activateUser(
                    activationRequest.getEmail(),
                    activationRequest.getCode()
            );

            return ResponseEntity.ok(ApiResponse.success("Account activated successfully!", resultMessage));
        } catch (Exception e) {
            LOGGER.error("Activation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Activation failed",
                            new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage()))
            );
        }
    }

}
