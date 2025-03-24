package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.DateCardRecommendation;
import com.datepoker.dp_backend.DTO.GameSettingsRequest;
import com.datepoker.dp_backend.services.FuzzyDateMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class GameRecommendationController {

    private final FuzzyDateMatcher fuzzyMatcher;

    @PostMapping
    public ResponseEntity<List<DateCardRecommendation>> getRecommendations(
            @RequestBody GameSettingsRequest request
    ) {
        var topMatches = fuzzyMatcher.getTopMatches(request.selectedOptions(), 5)
                .stream()
                .map(DateCardRecommendation::from)
                .toList();

        return ResponseEntity.ok(topMatches);
    }
}