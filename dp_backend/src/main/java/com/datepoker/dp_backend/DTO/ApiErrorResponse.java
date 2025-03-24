package com.datepoker.dp_backend.DTO;

import lombok.Builder;

@Builder
public class ApiErrorResponse {
    private final boolean success = false;
    private final String message;
    private final ErrorDetails error;

    @Builder
    public record ErrorDetails(int statusCode, String error, String details) {}

    public static ApiErrorResponse of(Exception e) {
        return ApiErrorResponse.builder()
                .message("An error occurred")
                .error(ErrorDetails.builder()
                        .statusCode(400)
                        .error("Bad Request")
                        .details(e.getMessage())
                        .build())
                .build();
    }
}
