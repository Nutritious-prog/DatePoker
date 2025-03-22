package com.datepoker.dp_backend.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivationRequest {
    private String email;
    private String code;
}
