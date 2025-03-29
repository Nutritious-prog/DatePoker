package com.datepoker.dp_backend.DTO;

import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.GameRoom.DisconnectionReason;

public record GameRoomStatusResponse(
        String code,
        GameRoom.Status status,
        boolean isActive,
        boolean disconnected,
        DisconnectionReason disconnectionReason,
        String message
) {
    public static GameRoomStatusResponse from(GameRoom room) {
        String msg = null;
        if (room.getDisconnectionReason() == DisconnectionReason.USER_LEFT) {
            msg = "Your partner has left the game 😢";
        } else if (room.getDisconnectionReason() == DisconnectionReason.TIMEOUT) {
            msg = "The session expired due to inactivity.";
        }

        return new GameRoomStatusResponse(
                room.getCode(),
                room.getStatus(),
                room.isActive(),
                room.isDisconnected(),
                room.getDisconnectionReason(),
                msg
        );
    }
}
