package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.DTO.PlaceRecommendation;
import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationService {

    private final ExternalPlacesClient placesClient; // we will define this next

    public List<PlaceRecommendation> recommendPlaces(DateCard card, UserProfile profile) {
        if (profile.getLatitude() == null || profile.getLongitude() == null) {
            throw new IllegalStateException("User location not set.");
        }
        return placesClient.searchNearby(profile.getLatitude(), profile.getLongitude(), card.getAttractionPlaceType(), 2);
    }

}

