package com.datepoker.dp_backend.logic;

import com.datepoker.dp_backend.entities.GameDateCard;

import java.util.List;
import java.util.Random;

public class RoundAnalyzer {

    private final List<GameDateCard> cards;
    private static final Random random = new Random();

    public RoundAnalyzer(List<GameDateCard> cards) {
        this.cards = cards;
    }

    public boolean isRoundFinished() {
        return cards.stream().allMatch(c -> c.getStatus() != GameDateCard.Status.UNDECIDED);
    }

    public boolean noCardsAccepted() {
        return cards.stream().noneMatch(c -> c.getStatus() == GameDateCard.Status.ACCEPTED);
    }

    public boolean oneCardAccepted() {
        return getAcceptedCards().size() == 1;
    }

    public int acceptedCount() {
        return getAcceptedCards().size();
    }

    public List<GameDateCard> getAcceptedCards() {
        return cards.stream()
                .filter(c -> c.getStatus() == GameDateCard.Status.ACCEPTED)
                .toList();
    }

    public GameDateCard getSingleAcceptedCard() {
        return oneCardAccepted() ? getAcceptedCards().get(0) : null;
    }

    public GameDateCard getRandomAcceptedCard() {
        List<GameDateCard> accepted = getAcceptedCards();
        if (accepted.isEmpty()) return null;
        return accepted.get(random.nextInt(accepted.size()));
    }
}
