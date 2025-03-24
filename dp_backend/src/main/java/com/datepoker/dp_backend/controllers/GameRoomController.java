package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.services.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

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

}
