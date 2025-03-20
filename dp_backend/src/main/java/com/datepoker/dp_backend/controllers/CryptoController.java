package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.ApiError;
import com.datepoker.dp_backend.DTO.ApiResponse;
import com.datepoker.dp_backend.DTO.EncryptionRequest;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crypto")
public class CryptoController {

    @PostMapping("/encrypt")
    public ResponseEntity<ApiResponse<String>> encrypt(@RequestBody EncryptionRequest request) {
        try {
            String encryptedText = AESEncryptionUtil.encrypt(request.getPayload());
            return ResponseEntity.ok(ApiResponse.success("Encryption successful", encryptedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Encryption failed", new ApiError(400, "Encryption Error", e.getMessage())));
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse<String>> decrypt(@RequestBody EncryptionRequest request) {
        try {
            String decryptedText = AESEncryptionUtil.decrypt(request.getPayload());
            return ResponseEntity.ok(ApiResponse.success("Decryption successful", decryptedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Decryption failed", new ApiError(400, "Decryption Error", e.getMessage())));
        }
    }
}

