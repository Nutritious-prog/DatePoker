package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.GameDateCard;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GameDateCardRepository extends JpaRepository<GameDateCard, Long> {
}
