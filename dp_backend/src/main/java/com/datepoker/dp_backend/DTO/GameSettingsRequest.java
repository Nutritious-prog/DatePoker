package com.datepoker.dp_backend.DTO;

import java.util.List;

public record GameSettingsRequest(
        List<String> selectedOptions
) {}
