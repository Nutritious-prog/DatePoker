package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.DTO.PlaceRecommendation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExternalPlacesClient {

    public List<PlaceRecommendation> searchNearby(double latitude, double longitude, String placeType, int limit) {
        String baseLocation = String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);

        return List.of(
                new PlaceRecommendation("Mock Place 1", baseLocation + " - Street 1", 4.6, "photo-url-1", "maps-url-1"),
                new PlaceRecommendation("Mock Place 2", baseLocation + " - Street 2", 4.3, "photo-url-2", "maps-url-2")
        );
    }

}

