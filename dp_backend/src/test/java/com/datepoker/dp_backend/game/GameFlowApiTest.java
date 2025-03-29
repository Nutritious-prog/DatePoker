package com.datepoker.dp_backend.game;

import com.datepoker.dp_backend.DTO.GameSettingsRequest;
import com.datepoker.dp_backend.DTO.JoinRoomRequest;
import com.datepoker.dp_backend.DTO.VoteRequest;
import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// TODO create the security config for tests
public class GameFlowApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private ObjectMapper objectMapper;

    private User userA, userB;
    private UserProfile profileA, profileB;
    private String tokenA = "Bearer token-a";
    private String tokenB = "Bearer token-b";

    @BeforeEach
    void setup() {
        userA = userRepository.save(new User("apiA@example.com", "pass", "API A"));
        userB = userRepository.save(new User("apiB@example.com", "pass", "API B"));
        profileA = userProfileRepository.save(new UserProfile("A", null, userA, "apiA@example.com"));
        profileB = userProfileRepository.save(new UserProfile("B", null, userB, "apiB@example.com"));
    }


    void fullGameApiFlowWithOneWinner() throws Exception {
        // ✅ 1. User A starts planning
        List<String> preferences = List.of("activity", "outdoor", "spring", "fun", "budget_friendly", "healthy");
        String startResponse = mockMvc.perform(post("/api/v1/game/start-planning")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameSettingsRequest(preferences))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String gameCode = objectMapper.readTree(startResponse)
                .path("data").path("gameCode").asText();

        // ✅ 2. User B joins
        mockMvc.perform(post("/api/v1/game/join-room")
                        .header("Authorization", tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinRoomRequest(gameCode))))
                .andExpect(status().isOk());

        // ✅ 3. Get cards
        GameRoom room = gameRoomRepository.findByCode(gameCode).orElseThrow();
        List<Long> cardIds = room.getGameCards().stream()
                .map(gc -> gc.getDateCard().getId())
                .toList();

        // ✅ 4. Both users vote: like only card[2], rest disagree
        for (int i = 0; i < cardIds.size(); i++) {
            Long cardId = cardIds.get(i);
            String voteA = (i == 2) ? "LIKE" : "DISLIKE";
            String voteB = (i == 2) ? "LIKE" : "DISLIKE";

            VoteRequest voteRequestA = new VoteRequest(cardId, voteA);
            VoteRequest voteRequestB = new VoteRequest(cardId, voteB);

            mockMvc.perform(post("/api/v1/game/vote")
                            .header("Authorization", tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequestA)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/game/vote")
                            .header("Authorization", tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequestB)))
                    .andExpect(status().isOk());
        }

        // ✅ 5. Check profile history
        String historyA = mockMvc.perform(get("/api/v1/profile/history")
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode historyNode = objectMapper.readTree(historyA).path("data");
        Assertions.assertEquals(1, historyNode.size());
        Assertions.assertTrue(historyNode.get(0).get("title").asText().startsWith("Option"));
    }
}

