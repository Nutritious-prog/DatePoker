package com.datepoker.dp_backend.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserProfileUpdateRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String displayName;

    @Size(max = 2048)
    private String profilePictureUrl;
}

