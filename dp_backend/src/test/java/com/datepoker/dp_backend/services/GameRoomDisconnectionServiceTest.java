package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameRoomDisconnectionServiceTest {

    private VoteService voteService;
    private GameRoomCleanupService cleanupService;
    private UserProfileRepository userProfileRepo;
    private GameRoomRepository gameRoomRepo;
    private GameRoomService gameRoomService;

    private User user;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        userProfileRepo = mock(UserProfileRepository.class);
        gameRoomRepo = mock(GameRoomRepository.class);
        voteService = new VoteService(null, userProfileRepo, null, gameRoomRepo, null);
        cleanupService = new GameRoomCleanupService(gameRoomRepo);

        user = new User("test@example.com", "pass", "Test User");
        user.setId(1L);
        profile = new UserProfile("Test", null, user, "test@example.com");

        gameRoomService = new GameRoomService(
                gameRoomRepo,
                null, // GameSettingsRepository
                userProfileRepo,
                null, // DateCardRepository
                null, // FuzzyDateMatcher
                null, // FeatureVectorService
                null, // GameDateCardRepository
                null, // UserDateRepository
                null  // DateHistoryService
        );

    }

    @Test
    void userLeavingMarksRoomDisconnected() {
        GameRoom room = GameRoom.builder()
                .code("LEAVE01")
                .creator(profile)
                .status(GameRoom.Status.ACTIVE)
                .isActive(true)
                .build();

        when(userProfileRepo.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(gameRoomRepo.findActiveRoomByUser(profile)).thenReturn(Optional.of(room));

        gameRoomService.leaveGame(user);

        assertFalse(room.isActive());
        assertEquals(GameRoom.DisconnectionReason.USER_LEFT, room.getDisconnectionReason());
        assertTrue(room.isDisconnected());
        assertEquals(GameRoom.DisconnectionReason.USER_LEFT, room.getDisconnectionReason());

        verify(gameRoomRepo).save(room);
    }

    @Test
    void cleanupServiceCancelsInactiveRooms() {
        GameRoom oldRoom = GameRoom.builder()
                .code("OLD123")
                .status(GameRoom.Status.ACTIVE)
                .isActive(true)
                .lastActivity(LocalDateTime.now().minusMinutes(15))
                .build();

        when(gameRoomRepo.findAllByStatusAndLastActivityBefore(eq(GameRoom.Status.ACTIVE), any()))
                .thenReturn(List.of(oldRoom));

        cleanupService.closeInactiveRooms();

        assertEquals(GameRoom.Status.CANCELLED, oldRoom.getStatus());
        assertFalse(oldRoom.isActive());
        assertTrue(oldRoom.isDisconnected());
        assertEquals(GameRoom.DisconnectionReason.TIMEOUT, oldRoom.getDisconnectionReason());

        verify(gameRoomRepo).save(oldRoom);
    }

    @Test
    void cleanupSkipsRecentRooms() {
        when(gameRoomRepo.findAllByStatusAndLastActivityBefore(eq(GameRoom.Status.ACTIVE), any()))
                .thenReturn(List.of());

        cleanupService.closeInactiveRooms();

        verify(gameRoomRepo, never()).save(any());
    }
}
