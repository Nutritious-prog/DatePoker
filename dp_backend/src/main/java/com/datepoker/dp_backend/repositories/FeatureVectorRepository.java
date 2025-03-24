package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.FeatureVector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureVectorRepository extends JpaRepository<FeatureVector, Long> {

    List<FeatureVector> findByDateCardIdOrderByIndexAsc(Long dateCardId);
}
