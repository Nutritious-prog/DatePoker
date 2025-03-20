package com.datepoker.dp_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiError {
    private int statusCode;
    private String error;
    private String details;
}

