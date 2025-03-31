package com.datepoker.dp_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRecommendation {
    private String name;
    private String address;
    private double rating;
    private String photoUrl;
    private String mapsUrl;
}

