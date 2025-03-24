package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.FeatureVector;
import com.datepoker.dp_backend.repositories.FeatureVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureVectorService {

    private final FeatureVectorRepository featureVectorRepository;

    public List<Double> getVector(Long cardId) {
        return featureVectorRepository.findByDateCardIdOrderByIndexAsc(cardId)
                .stream()
                .map(FeatureVector::getValue)
                .toList();
    }
}

