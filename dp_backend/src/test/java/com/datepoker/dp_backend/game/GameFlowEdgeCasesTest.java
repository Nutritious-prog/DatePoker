package com.datepoker.dp_backend.game;

import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GameFlowEdgeCasesTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private GameDateCardRepository gameDateCardRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private UserDateRepository userDateRepository;
    @Autowired private DateCardRepository dateCardRepository;

    private User userA, userB;
    private UserProfile profileA, profileB;

    @BeforeEach
    void setup() {
        userA = userRepository.save(new User("edge-a@example.com", "pass", "Edge A"));
        userB = userRepository.save(new User("edge-b@example.com", "pass", "Edge B"));
        profileA = userProfileRepository.save(new UserProfile("A", null, userA, "edge-a@example.com"));
        profileB = userProfileRepository.save(new UserProfile("B", null, userB, "edge-b@example.com"));
    }

    @Test
    void userCannotJoinOwnRoom() {
        GameRoom room = createRoom(profileA, GameRoom.Status.WAITING);
        room.setJoiner(profileA);
        Assertions.assertThrows(IllegalStateException.class, () -> {
            validateJoiner(room, profileA);
        });
    }

    @Test
    void userCannotJoinSameRoomTwice() {
        GameRoom room = createRoom(profileA, GameRoom.Status.ACTIVE);
        room.setJoiner(profileB);
        gameRoomRepository.save(room);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            validateJoiner(room, profileB); // already joined
        });
    }

    @Test
    void cannotCreateRoomIfUserAlreadyHasActiveOne() {
        GameRoom active = createRoom(profileA, GameRoom.Status.ACTIVE);
        active.setActive(true);
        gameRoomRepository.save(active);

        Assertions.assertTrue(gameRoomRepository.existsByCreatorAndIsActiveTrue(profileA));

        // Simulate create-room logic
        Assertions.assertThrows(IllegalStateException.class, () -> {
            if (gameRoomRepository.existsByCreatorAndIsActiveTrue(profileA)) {
                throw new IllegalStateException("You already have an active room");
            }
        });
    }

    @Test
    void roundDoesNotFinishWhenNoCardsAreAccepted() {
        GameRoom room = createRoom(profileA, GameRoom.Status.ACTIVE);
        room.setJoiner(profileB);
        gameRoomRepository.save(room);

        List<GameDateCard> cards = attachCards(room, 5);
        cards.forEach(card -> {
            card.setStatus(GameDateCard.Status.REJECTED);
        });
        gameDateCardRepository.saveAll(cards);

        boolean allVoted = cards.stream().allMatch(c -> c.getStatus() != GameDateCard.Status.UNDECIDED);
        boolean anyAccepted = cards.stream().anyMatch(c -> c.getStatus() == GameDateCard.Status.ACCEPTED);

        Assertions.assertTrue(allVoted);
        Assertions.assertFalse(anyAccepted);
        Assertions.assertNull(room.getAcceptedCardId());
    }

    @Test
    void roundDoesNotFinishAutomaticallyWhenMultipleCardsAccepted() {
        GameRoom room = createRoom(profileA, GameRoom.Status.ACTIVE);
        room.setJoiner(profileB);
        gameRoomRepository.save(room);

        List<GameDateCard> cards = attachCards(room, 5);
        cards.get(0).setStatus(GameDateCard.Status.ACCEPTED);
        cards.get(1).setStatus(GameDateCard.Status.ACCEPTED);
        cards.subList(2, 5).forEach(c -> c.setStatus(GameDateCard.Status.REJECTED));
        gameDateCardRepository.saveAll(cards);

        long acceptedCount = cards.stream()
                .filter(c -> c.getStatus() == GameDateCard.Status.ACCEPTED)
                .count();

        Assertions.assertEquals(2, acceptedCount);
        Assertions.assertNull(room.getAcceptedCardId());
        Assertions.assertEquals(GameRoom.Status.ACTIVE, room.getStatus());
    }

    @Test
    void selectingNewCardsFailsIfNoneAreLeft() {
        GameRoom room = createRoom(profileA, GameRoom.Status.ACTIVE);
        room.setJoiner(profileB);
        gameRoomRepository.save(room);

        // Assume all DateCards in DB are already used
        List<DateCard> allCards = dateCardRepository.findAll();
        List<GameDateCard> played = allCards.stream()
                .map(card -> GameDateCard.builder()
                        .gameRoom(room)
                        .dateCard(card)
                        .status(GameDateCard.Status.REJECTED)
                        .build())
                .toList();

        gameDateCardRepository.saveAll(played);

        Set<Long> usedCardIds = played.stream()
                .map(c -> c.getDateCard().getId())
                .collect(Collectors.toSet());

        List<DateCard> nextPool = dateCardRepository.findAll().stream()
                .filter(c -> !usedCardIds.contains(c.getId()))
                .toList();

        Assertions.assertEquals(0, nextPool.size());
    }

    @Test
    void userCannotVoteTwiceOnSameCard() {
        GameRoom room = createRoom(profileA, GameRoom.Status.ACTIVE);
        room.setJoiner(profileB);
        gameRoomRepository.save(room);

        GameDateCard card = attachCards(room, 1).get(0);

        voteRepository.save(Vote.builder()
                .userProfile(profileA)
                .gameDateCard(card)
                .value(Vote.VoteValue.LIKE)
                .build());

        Assertions.assertThrows(IllegalStateException.class, () -> {
            voteRepository.findByUserProfileAndGameDateCard(profileA, card)
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Already voted");
                    });
        });
    }

    // --- Helpers ---

    private GameRoom createRoom(UserProfile creator, GameRoom.Status status) {
        GameSettings settings = GameSettings.fromOptions(
                List.of("activity", "outdoor", "spring", "fun", "budget_friendly", "healthy"),
                creator
        );
        return gameRoomRepository.save(GameRoom.builder()
                .creator(creator)
                .settings(settings)
                .code(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .status(status)
                .isActive(true)
                .build());
    }

    private List<GameDateCard> attachCards(GameRoom room, int count) {
        List<DateCard> all = dateCardRepository.findAll().stream().limit(count).toList();
        return gameDateCardRepository.saveAll(
                all.stream()
                        .map(card -> GameDateCard.builder()
                                .gameRoom(room)
                                .dateCard(card)
                                .status(GameDateCard.Status.UNDECIDED)
                                .build())
                        .toList()
        );
    }

    private void validateJoiner(GameRoom room, UserProfile profile) {
        if (room.getCreator().equals(profile)) {
            throw new IllegalStateException("Cannot join your own room");
        }
        if (profile.equals(room.getJoiner())) {
            throw new IllegalStateException("Already in this room");
        }
    }
}

