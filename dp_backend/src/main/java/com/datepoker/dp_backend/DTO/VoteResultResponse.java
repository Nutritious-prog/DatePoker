package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.GameRoom;

public record VoteResultResponse(
        String cardStatus,
        String roomStatus,
        Long acceptedCardId,
        boolean roundEnded,
        boolean noCardsAccepted
) {
    public static VoteResultResponse of(GameDateCard card, GameRoom room, boolean allVoted, boolean noneAccepted) {
        return new VoteResultResponse(
                card.getStatus().name(),
                room.getStatus().name(),
                room.getAcceptedCardId(),
                allVoted,
                noneAccepted
        );
    }
}

