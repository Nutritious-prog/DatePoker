package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.DateCard;

public record DateCardRecommendation(
        Long id,
        String title,
        String imageUrl,
        String location,
        String priceLevel,
        String category
) {
    public static DateCardRecommendation from(DateCard card) {
        return new DateCardRecommendation(
                card.getId(), card.getTitle(), card.getImageUrl(),
                card.getLocation(), card.getPriceLevel(), card.getCategory().name()
        );
    }
}

