package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.ApiError;
import com.datepoker.dp_backend.DTO.ApiResponse;
import com.datepoker.dp_backend.DTO.EncryptionRequest;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crypto")
public class CryptoController {

    private final ObjectMapper objectMapper;

    public CryptoController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<ApiResponse<String>> encrypt(@RequestBody EncryptionRequest request) {
        try {
            // 🔄 Convert JSON object to a string
            String jsonString = objectMapper.writeValueAsString(request.getPayload());

            // 🔐 Encrypt the JSON string
            String encryptedText = AESEncryptionUtil.encrypt(jsonString);

            return ResponseEntity.ok(ApiResponse.success("Encryption successful", encryptedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Encryption failed", new ApiError(400, "Encryption Error", e.getMessage())));
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse<JsonNode>> decrypt(@RequestBody EncryptionRequest request) {
        try {
            // 🔓 Decrypt the data
            String decryptedJson = AESEncryptionUtil.decrypt(request.getPayload().asText());

            // 🔄 Convert decrypted string back to JSON object
            JsonNode jsonResponse = objectMapper.readTree(decryptedJson);

            return ResponseEntity.ok(ApiResponse.success("Decryption successful", jsonResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<JsonNode>error("Decryption failed", new ApiError(400, "Decryption Error", e.getMessage())));
        }
    }

    @PostMapping("/decrypt_JWT")
    public ResponseEntity<ApiResponse<String>> decryptJWT(@RequestBody EncryptionRequest request) {
        try {
            String encrypted = request.getPayload().asText();
            String decrypted = AESEncryptionUtil.decrypt(encrypted); // 🔐 This is a plain JWT string

            // ✅ Do NOT try to parse it with Jackson — it’s not JSON
            return ResponseEntity.ok(ApiResponse.success("Decryption successful", decrypted));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Decryption failed",
                            new ApiError(400, "Decryption Error", e.getMessage()))
            );
        }
    }

}

