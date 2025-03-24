package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.repositories.*;
import com.datepoker.dp_backend.util.GameCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final GameSettingsRepository settingsRepository;
    private final UserProfileRepository userProfileRepository;
    private final DateCardRepository dateCardRepository;
    private final FuzzyDateMatcher fuzzyDateMatcher;
    private final FeatureVectorService featureVectorService;
    private final GameDateCardRepository gameDateCardRepository;


    public GameRoom createRoom(User hostUser, List<String> selectedOptions) {
        UserProfile profile = userProfileRepository.findByUserId(hostUser.getId())
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        // Always create a fresh GameSettings snapshot
        GameSettings settings = GameSettings.fromOptions(selectedOptions, profile);
        settingsRepository.save(settings);

        GameRoom room = GameRoom.builder()
                .creator(profile)
                .code(GameCodeGenerator.generateCode())
                .settings(settings)
                .status(GameRoom.Status.WAITING)
                .isActive(true)
                .build();

        return gameRoomRepository.save(room);
    }


    public GameRoom joinRoom(User joinerUser, String code) {
        GameRoom room = gameRoomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!GameRoom.Status.WAITING.equals(room.getStatus())) {
            throw new IllegalStateException("Room is not open for joining");
        }

        if (room.getJoiner() != null) {
            throw new IllegalStateException("Room already has a second player");
        }

        UserProfile joinerProfile = userProfileRepository.findByUserId(joinerUser.getId())
                .orElseThrow(() -> new IllegalStateException("User has no profile"));

        // Match top 5 cards using GameSettings
        double[] userVector = room.getSettings().toFeatureVector();
        List<DateCard> topCards = dateCardRepository.findAll().stream()
                .map(card -> Map.entry(card, featureVectorService.getVector(card.getId())))
                .filter(entry -> entry.getValue().size() == userVector.length)
                .sorted(Comparator.comparingDouble(entry ->
                        fuzzyDateMatcher.euclideanDistance(userVector, entry.getValue())))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Convert to GameDateCard
        List<GameDateCard> gameDateCards = topCards.stream()
                .map(card -> GameDateCard.builder()
                        .gameRoom(room)
                        .dateCard(card)
                        .status(GameDateCard.Status.UNDECIDED)
                        .build())
                .collect(Collectors.toList());

        // Update and persist
        room.setJoiner(joinerProfile);
        room.setGameCards(gameDateCards);
        room.setStatus(GameRoom.Status.ACTIVE);
        room.setActive(true);

        gameDateCardRepository.saveAll(gameDateCards);
        return gameRoomRepository.save(room);
    }
}