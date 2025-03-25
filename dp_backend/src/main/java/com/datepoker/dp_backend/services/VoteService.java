package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.DTO.VoteRequest;
import com.datepoker.dp_backend.DTO.VoteResultResponse;
import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.logic.RoundAnalyzer;
import com.datepoker.dp_backend.repositories.GameDateCardRepository;
import com.datepoker.dp_backend.repositories.GameRoomRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.repositories.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserProfileRepository userProfileRepository;
    private final GameDateCardRepository gameDateCardRepository;
    private final GameRoomRepository gameRoomRepository;
    private final DateHistoryService dateHistoryService;


    @Transactional
    public VoteResultResponse submitVote(User user, VoteRequest request) {
        UserProfile profile = getUserProfile(user);
        GameDateCard card = getGameDateCard(request.cardId());
        Vote.VoteValue value = parseVoteValue(request.value());

        checkForDuplicateVote(profile, card);
        saveVote(profile, card, value);

        updateCardStatusIfBothVoted(card);
        checkAndFinalizeRound(card.getGameRoom());

        List<GameDateCard> cards = gameDateCardRepository.findByGameRoom(card.getGameRoom());
        RoundAnalyzer analyzer = new RoundAnalyzer(cards);

        boolean allVoted = analyzer.isRoundFinished();
        boolean noAccepted = analyzer.noCardsAccepted();

        return VoteResultResponse.of(card, card.getGameRoom(), allVoted, noAccepted);
    }

    private UserProfile getUserProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
    }

    private GameDateCard getGameDateCard(Long cardId) {
        return gameDateCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    private Vote.VoteValue parseVoteValue(String value) {
        return Vote.VoteValue.valueOf(value.toUpperCase());
    }

    private void checkForDuplicateVote(UserProfile profile, GameDateCard card) {
        voteRepository.findByUserProfileAndGameDateCard(profile, card).ifPresent(existing -> {
            throw new IllegalStateException("You already voted on this card.");
        });
    }

    private void saveVote(UserProfile profile, GameDateCard card, Vote.VoteValue value) {
        Vote vote = Vote.builder()
                .userProfile(profile)
                .gameDateCard(card)
                .value(value)
                .build();
        voteRepository.save(vote);
    }

    private void updateCardStatusIfBothVoted(GameDateCard card) {
        List<Vote> votes = voteRepository.findByGameDateCard(card);
        if (votes.size() == 2) {
            boolean bothLiked = votes.stream().allMatch(v -> v.getValue() == Vote.VoteValue.LIKE);
            card.setStatus(bothLiked ? GameDateCard.Status.ACCEPTED : GameDateCard.Status.REJECTED);
            gameDateCardRepository.save(card);
        }
    }

    private void checkAndFinalizeRound(GameRoom room) {
        List<GameDateCard> cards = gameDateCardRepository.findByGameRoom(room);
        RoundAnalyzer analyzer = new RoundAnalyzer(cards);

        if (!analyzer.isRoundFinished()) return;

        if (analyzer.oneCardAccepted()) {
            GameDateCard winner = analyzer.getSingleAcceptedCard();
            room.setAcceptedCardId(winner.getDateCard().getId());
            room.setStatus(GameRoom.Status.FINISHED);
            room.setActive(false);
            gameRoomRepository.save(room);

            dateHistoryService.saveForRoom(room, winner.getDateCard());
        }
        // Else → multiple or none → frontend decides: draw again or choose random
    }



}

