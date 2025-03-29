package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.GameRoom.DisconnectionReason;
import com.datepoker.dp_backend.entities.GameRoom.Status;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GameRoomCleanupServiceTest {

    private GameRoomRepository gameRoomRepository;
    private GameRoomCleanupService cleanupService;

    @BeforeEach
    void setup() {
        gameRoomRepository = mock(GameRoomRepository.class);
        cleanupService = new GameRoomCleanupService(gameRoomRepository);
    }

    @Test
    void closeInactiveRooms_marksExpiredRoomAsCancelled() {
        GameRoom room = GameRoom.builder()
                .code("TEST01")
                .status(Status.ACTIVE)
                .isActive(true)
                .lastActivity(LocalDateTime.now().minusMinutes(15))
                .build();

        when(gameRoomRepository.findAllByStatusAndLastActivityBefore(eq(Status.ACTIVE), any()))
                .thenReturn(List.of(room));

        cleanupService.closeInactiveRooms();

        assertEquals(Status.CANCELLED, room.getStatus());
        assertFalse(room.isActive());
        assertTrue(room.isDisconnected());
        assertEquals(DisconnectionReason.TIMEOUT, room.getDisconnectionReason());

        verify(gameRoomRepository).save(room);
    }

    @Test
    void closeInactiveRooms_skipsIfNoRoomsToClose() {
        when(gameRoomRepository.findAllByStatusAndLastActivityBefore(eq(Status.ACTIVE), any()))
                .thenReturn(List.of());

        cleanupService.closeInactiveRooms();

        verify(gameRoomRepository, never()).save(any());
    }
}
