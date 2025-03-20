package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.RegisterRequest;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.services.AuthService;
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
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        LOGGER.info("New registration request for {}", request.getEmail());

        try {
            String responseMessage = authService.registerUser(request);
            LOGGER.info("Registration successful for {}", request.getEmail());
            return ResponseEntity.ok(responseMessage);
        } catch (RuntimeException e) {
            LOGGER.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

