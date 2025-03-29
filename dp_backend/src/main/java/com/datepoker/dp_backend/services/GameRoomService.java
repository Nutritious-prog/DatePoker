package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.logic.RoundAnalyzer;
import com.datepoker.dp_backend.repositories.*;
import com.datepoker.dp_backend.util.GameCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final UserDateRepository userDateRepository;
    private final DateHistoryService dateHistoryService;



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

        room.setLastActivity(LocalDateTime.now());

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

        if (room.getCreator().getId().equals(joinerUser.getProfile().getId())) {
            throw new IllegalStateException("You cannot join your own room");
        }

        if (isUserInActiveRoom(joinerUser)) {
            throw new IllegalStateException("You're already in an active room");
        }

        if (isUserInActiveRoom(room.getCreator().getUser())) {
            throw new IllegalStateException("You already have an active room");
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
        room.setLastActivity(LocalDateTime.now());
        return gameRoomRepository.save(room);
    }

    private boolean isUserInActiveRoom(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        return gameRoomRepository.existsByCreatorAndIsActiveTrue(profile) ||
                gameRoomRepository.existsByJoinerAndIsActiveTrue(profile);
    }


    @Transactional
    public GameRoom selectNewCardsForRoom(String code, User user) {
        GameRoom room = gameRoomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // ✅ Authorization: only players in this room can trigger it
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow();
        if (!profile.equals(room.getCreator()) && !profile.equals(room.getJoiner())) {
            throw new IllegalStateException("You are not part of this room");
        }

        // ✅ Get already used cards
        List<GameDateCard> allPlayed = gameDateCardRepository.findByGameRoom(room);
        Set<Long> usedCardIds = allPlayed.stream()
                .map(c -> c.getDateCard().getId())
                .collect(Collectors.toSet());

        // ✅ Fuzzy match against unused cards
        double[] vector = room.getSettings().toFeatureVector();
        List<DateCard> nextCards = dateCardRepository.findAll().stream()
                .filter(card -> !usedCardIds.contains(card.getId()))
                .map(card -> Map.entry(card, featureVectorService.getVector(card.getId())))
                .filter(entry -> entry.getValue().size() == vector.length)
                .sorted(Comparator.comparingDouble(entry ->
                        fuzzyDateMatcher.euclideanDistance(vector, entry.getValue())))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (nextCards.isEmpty()) {
            throw new IllegalStateException("No more new cards available");
        }

        // ✅ Create new GameDateCard entries
        List<GameDateCard> gameCards = nextCards.stream()
                .map(card -> GameDateCard.builder()
                        .gameRoom(room)
                        .dateCard(card)
                        .status(GameDateCard.Status.UNDECIDED)
                        .build())
                .toList();

        gameDateCardRepository.saveAll(gameCards);
        room.getGameCards().addAll(gameCards);
        room.setLastActivity(LocalDateTime.now());
        return gameRoomRepository.save(room);
    }

    @Transactional
    public GameRoom selectRandomWinner(String code, User user) {
        GameRoom room = gameRoomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // ✅ Authorization check
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        if (!profile.equals(room.getCreator()) && !profile.equals(room.getJoiner())) {
            throw new IllegalStateException("You are not part of this room");
        }

        // ✅ Analyze round
        List<GameDateCard> cards = gameDateCardRepository.findByGameRoom(room);
        RoundAnalyzer analyzer = new RoundAnalyzer(cards);

        GameDateCard winner = analyzer.getRandomAcceptedCard();
        if (winner == null) {
            throw new IllegalStateException("No accepted cards to choose from.");
        }

        room.setAcceptedCardId(winner.getDateCard().getId());
        room.setStatus(GameRoom.Status.FINISHED);
        room.setActive(false);

        dateHistoryService.saveForRoom(room, winner.getDateCard());
        room.setLastActivity(LocalDateTime.now());
        return gameRoomRepository.save(room);
    }

    public void leaveGame(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        GameRoom room = gameRoomRepository.findActiveRoomByUser(profile)
                .orElseThrow(() -> new IllegalStateException("No active game"));

        room.setActive(false);
        room.setStatus(GameRoom.Status.CANCELLED);
        room.setDisconnected(true);
        room.setDisconnectionReason(GameRoom.DisconnectionReason.USER_LEFT);

        gameRoomRepository.save(room);
    }



}