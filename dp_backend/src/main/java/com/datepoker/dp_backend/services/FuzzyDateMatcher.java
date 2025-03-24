package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.GameSettings;
import com.datepoker.dp_backend.repositories.DateCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FuzzyDateMatcher {

    private final DateCardRepository dateCardRepository;
    private final FeatureVectorService vectorService; // fetches full feature vectors

    public List<DateCard> getTopMatches(List<String> options, int limit) {
        GameSettings settings = GameSettings.fromOptions(options, null); // no profile needed
        double[] userVector = settings.toFeatureVector();

        return dateCardRepository.findAll().stream()
                .map(card -> new AbstractMap.SimpleEntry<>(card, vectorService.getVector(card.getId())))
                .filter(entry -> entry.getValue().size() == userVector.length)
                .sorted(Comparator.comparingDouble(entry ->
                        euclideanDistance(userVector, entry.getValue())))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    double euclideanDistance(double[] a, List<Double> b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}

