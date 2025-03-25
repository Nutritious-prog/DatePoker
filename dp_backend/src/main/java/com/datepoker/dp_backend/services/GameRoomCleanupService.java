package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.repositories.GameRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}

