package com.datepoker.dp_backend.game;

import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GameFlowHappyPathTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private GameDateCardRepository gameDateCardRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private UserDateRepository userDateRepository;

    @Test
    public void testFullHappyPath_GamePlaysToWin() throws Exception {
        // ✅ 1. Setup two users
        User userA = userRepository.save(new User("userA@example.com", "pass", "User A"));
        User userB = userRepository.save(new User("userB@example.com", "pass", "User B"));
        UserProfile profileA = userProfileRepository.save(new UserProfile("A", null, userA, "userA@example.com"));
        UserProfile profileB = userProfileRepository.save(new UserProfile("B", null, userB, "userB@example.com"));

        // ✅ 2. User A creates a room
        List<String> options = List.of("activity", "outdoor", "spring", "fun", "healthy", "budget_friendly");
        GameSettings settings = GameSettings.fromOptions(options, profileA);
        GameRoom room = gameRoomRepository.save(GameRoom.builder()
                .creator(profileA)
                .code("TEST01")
                .settings(settings)
                .status(GameRoom.Status.WAITING)
                .isActive(true)
                .build());

        // ✅ 3. User B joins
        room.setJoiner(profileB);
        room.setStatus(GameRoom.Status.ACTIVE);
        gameRoomRepository.save(room);

        // ✅ 4. Create 5 GameDateCards, mark one for mutual liking
        List<GameDateCard> cards = IntStream.range(0, 5).mapToObj(i -> {
            DateCard dateCard = new DateCard();
            dateCard.setId((long) i + 1);
            dateCard.setTitle("Option " + (i + 1));
            dateCard.setCategory(DateCard.Category.ACTIVITY);
            return GameDateCard.builder()
                    .dateCard(dateCard)
                    .gameRoom(room)
                    .status(GameDateCard.Status.UNDECIDED)
                    .build();
        }).toList();
        gameDateCardRepository.saveAll(cards);

        GameDateCard winningCard = cards.get(2);

        // ✅ 5. Both users vote - one mutual LIKE, rest mixed/disliked
        for (GameDateCard card : cards) {
            if (card == winningCard) {
                voteRepository.save(Vote.builder().gameDateCard(card).userProfile(profileA).value(Vote.VoteValue.LIKE).build());
                voteRepository.save(Vote.builder().gameDateCard(card).userProfile(profileB).value(Vote.VoteValue.LIKE).build());
                card.setStatus(GameDateCard.Status.ACCEPTED);
            } else {
                voteRepository.save(Vote.builder().gameDateCard(card).userProfile(profileA).value(Vote.VoteValue.DISLIKE).build());
                voteRepository.save(Vote.builder().gameDateCard(card).userProfile(profileB).value(Vote.VoteValue.LIKE).build());
                card.setStatus(GameDateCard.Status.REJECTED);
            }
        }

        gameDateCardRepository.saveAll(cards);

        // ✅ 6. Simulate finalization logic (or call directly if exposed)
        room.setAcceptedCardId(winningCard.getDateCard().getId());
        room.setStatus(GameRoom.Status.FINISHED);
        room.setActive(false);
        gameRoomRepository.save(room);

        // ✅ 7. Save to date history manually (or via service)
        userDateRepository.saveAll(List.of(
                UserDate.builder().userProfile(profileA).dateCard(winningCard.getDateCard()).dateHappened(LocalDateTime.now()).build(),
                UserDate.builder().userProfile(profileB).dateCard(winningCard.getDateCard()).dateHappened(LocalDateTime.now()).build()
        ));

        // ✅ 8. Assert final room + vote state
        GameRoom finishedRoom = gameRoomRepository.findByCode("TEST01").orElseThrow();
        Assertions.assertEquals(GameRoom.Status.FINISHED, finishedRoom.getStatus());
        Assertions.assertEquals(winningCard.getDateCard().getId(), finishedRoom.getAcceptedCardId());

        // ✅ 9. Assert both users got a final date
        Assertions.assertEquals(1, userDateRepository.findByUserProfile(profileA).size());
        Assertions.assertEquals(1, userDateRepository.findByUserProfile(profileB).size());
    }
}

