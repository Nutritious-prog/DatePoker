package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.services.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;
    private final GameRoomRepository gameRoomRepository;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/start-planning")
    public ResponseEntity<ApiResponse<GameRoomResponse>> startPlanning(
            @CurrentUser User currentUser,
            @RequestBody GameSettingsRequest request
    ) {
        GameRoom gameRoom = gameRoomService.createRoom(currentUser, request.selectedOptions());

        return ResponseEntity.ok(
                ApiResponse.success("Room created", GameRoomResponse.from(gameRoom))
        );
    }

    @PostMapping(path = "/join-room", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> joinRoom(@RequestBody JoinRoomRequest request, @CurrentUser User user) {
        try {
            GameRoom joinedRoom = gameRoomService.joinRoom(user, request.code());
            return ResponseEntity.ok(GameRoomResponse.from(joinedRoom));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiErrorResponse.of(e));
        }
    }

    @PostMapping("/select-new-cards")
    public ResponseEntity<?> selectNewCards(
            @RequestBody DifferentCardsSelectionRoundRequest request,
            @CurrentUser User user
    ) {
        GameRoom updatedRoom = gameRoomService.selectNewCardsForRoom(request.roomCode(), user);
        return ResponseEntity.ok(GameRoomResponse.from(updatedRoom));
    }

    @PostMapping("/choose-random-winner")
    public ResponseEntity<?> chooseRandomWinner(
            @RequestBody JoinRoomRequest request,
            @CurrentUser User user
    ) {
        GameRoom room = gameRoomService.selectRandomWinner(request.code(), user);
        return ResponseEntity.ok(GameRoomResponse.from(room));
    }

    @PostMapping("/leave-room")
    public ResponseEntity<Void> leaveRoom(@CurrentUser User user) {
        gameRoomService.leaveGame(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<GameRoomStatusResponse> getRoomStatus(@CurrentUser User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        GameRoom room = gameRoomRepository.findActiveRoomByUser(profile)
                .orElseThrow(() -> new IllegalStateException("No active game found"));

        return ResponseEntity.ok(GameRoomStatusResponse.from(room));
    }

    @GetMapping("/{code}/status")
    public ResponseEntity<GameRoomStatusResponse> getRoomStatusByCode(@PathVariable String code) {
        GameRoom room = gameRoomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return ResponseEntity.ok(GameRoomStatusResponse.from(room));
    }



}
