package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRoomCleanupService {

    private final GameRoomRepository gameRoomRepository;

    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void cleanOldRooms() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2); // e.g., 2 hours expiration
        gameRoomRepository.expireOldRooms(cutoff);
    }

    @Scheduled(fixedRate = 60000) // every 60s
    public void closeInactiveRooms() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(10);
        List<GameRoom> inactiveRooms = gameRoomRepository
                .findAllByStatusAndLastActivityBefore(GameRoom.Status.ACTIVE, timeoutThreshold);

        for (GameRoom room : inactiveRooms) {
            room.setStatus(GameRoom.Status.CANCELLED);
            room.setActive(false);
            room.setDisconnected(true);
            room.setDisconnectionReason(GameRoom.DisconnectionReason.TIMEOUT);
            gameRoomRepository.save(room);
        }
    }

}

