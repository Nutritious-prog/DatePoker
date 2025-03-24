package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.GameDateCard;

public record DateCardSummary(
        Long id,
        String title,
        String imageUrl,
        String location,
        String priceLevel,
        String category
) {
    public static DateCardSummary from(GameDateCard card) {
        DateCard dc = card.getDateCard();
        return new DateCardSummary(
                dc.getId(),
                dc.getTitle(),
                dc.getImageUrl(),
                dc.getLocation(),
                dc.getPriceLevel(),
                dc.getCategory().name()
        );
    }
}

