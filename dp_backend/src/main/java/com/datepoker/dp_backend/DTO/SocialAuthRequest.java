package com.datepoker.dp_backend.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SocialAuthRequest {

    @NotBlank(message = "Provider is required")
    private String provider; // "google" or "facebook"

    @NotBlank(message = "Token is required")
    private String token; // OAuth token from the frontend
}

