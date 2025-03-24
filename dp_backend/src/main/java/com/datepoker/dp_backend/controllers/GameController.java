package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.repositories.GameSettingsRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameController {

    private final UserProfileRepository userProfileRepository;
    private final GameSettingsRepository gameSettingsRepository;


}

