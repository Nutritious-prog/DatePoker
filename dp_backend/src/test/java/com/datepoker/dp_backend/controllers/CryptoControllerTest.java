package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.EncryptionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CryptoController.class)
public class CryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void testEncryptDecrypt() throws Exception {
//        // Encrypt data
//        String encryptedResponse = mockMvc.perform(post("/crypto/encrypt")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(
//                                new EncryptionRequest(objectMapper.readTree("{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\",\"password\":\"Secure123!\"}"))
//                        )))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        // Extract encrypted data
//        String encryptedData = objectMapper.readTree(encryptedResponse).get("data").asText();
//
//        // 🔓 Fix: Send correct decryption payload
//        String decryptPayload = objectMapper.writeValueAsString(Map.of("data", encryptedData));
//
//        mockMvc.perform(post("/crypto/decrypt")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(decryptPayload))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.name").value("John Doe"));
//    }
}

