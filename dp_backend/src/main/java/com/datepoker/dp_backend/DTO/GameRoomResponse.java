package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.GameRoom;
import lombok.*;

import java.util.List;

@Builder
public record GameRoomResponse(
        String code,
        Long creatorId,
        Long joinerId,
        GameRoom.Status status,
        List<DateCardSummary> dateCards
) {
    public static GameRoomResponse from(GameRoom room) {
        return new GameRoomResponse(
                room.getCode(),
                room.getCreator().getId(),
                room.getJoiner() != null ? room.getJoiner().getId() : null,
                room.getStatus(),
                room.getGameCards() != null
                        ? room.getGameCards().stream()
                        .map(DateCardSummary::from)
                        .toList()
                        : List.of()
        );
    }
}



