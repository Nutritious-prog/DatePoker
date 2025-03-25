package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GameDateCardRepository extends JpaRepository<GameDateCard, Long> {
    List<GameDateCard> findByGameRoom(GameRoom gameRoom);
}
