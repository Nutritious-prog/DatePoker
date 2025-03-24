package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.GameSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSettingsRepository extends JpaRepository<GameSettings, Long> {
}
