package com.datepoker.dp_backend.controllers;
import com.datepoker.dp_backend.DTO.EncryptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void testRegisterUserWithEncryption() throws Exception {
//        // Prepare JSON user data
//        String userData = "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\",\"password\":\"Secure123!\"}";
//
//        // Encrypt user data
//        String encryptedResponse = mockMvc.perform(post("/crypto/encrypt")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(new EncryptionRequest(objectMapper.readTree(userData)))))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        // Extract encrypted data
//        String encryptedData = objectMapper.readTree(encryptedResponse).get("data").asText();
//
//        // Send registration request with encrypted data
//        mockMvc.perform(post("/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(new EncryptionRequest(objectMapper.readTree("{\"payload\":\"" + encryptedData + "\"}"))
//                        )))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("User registered successfully!"));
//    }
}

