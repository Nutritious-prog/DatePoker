package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.ApiError;
import com.datepoker.dp_backend.DTO.ApiResponse;
import com.datepoker.dp_backend.DTO.RegisterRequest;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody String encryptedPayload) {
        try {
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedPayload);
            RegisterRequest request = new ObjectMapper().readValue(decryptedJson, RegisterRequest.class);

            LOGGER.info("New (encrypted) registration request for {}", request.getEmail());

            String responseMessage = authService.registerUser(request);
            return ResponseEntity.ok(ApiResponse.success(responseMessage, null));

        } catch (RuntimeException e) {
            LOGGER.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed", new ApiError(400, "Bad Request", e.getMessage())));
        } catch (Exception e) {
            LOGGER.error("Decryption or processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal Server Error", new ApiError(500, "Server Error", e.getMessage())));
        }
    }

}

