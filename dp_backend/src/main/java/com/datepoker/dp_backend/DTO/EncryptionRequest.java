package com.datepoker.dp_backend.DTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class EncryptionRequest {
    private JsonNode payload;
}
