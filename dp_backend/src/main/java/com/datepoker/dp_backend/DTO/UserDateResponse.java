package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.UserDate;

import java.time.LocalDateTime;

public record UserDateResponse(
        String title,
        String location,
        String priceLevel,
        String category,
        LocalDateTime dateHappened
) {
    public static UserDateResponse from(UserDate date) {
        DateCard card = date.getDateCard();
        return new UserDateResponse(
                card.getTitle(),
                card.getLocation(),
                card.getPriceLevel(),
                card.getCategory().name(),
                date.getDateHappened()
        );
    }
}
