package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.ApiResponse;
import com.datepoker.dp_backend.DTO.PlaceRecommendation;
import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.DateCardRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.services.PlaceRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceRecommendationController {

    private final DateCardRepository dateCardRepository;
    private final PlaceRecommendationService placeRecommendationService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/recommendations/{cardId}")
    public ResponseEntity<ApiResponse<List<PlaceRecommendation>>> getRecommendations(
            @PathVariable Long cardId,
            @CurrentUser User user) {

        DateCard card = dateCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DateCard not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        List<PlaceRecommendation> results = placeRecommendationService.recommendPlaces(card, profile);
        return ResponseEntity.ok(ApiResponse.success("Found recommended places", results));
    }

}

