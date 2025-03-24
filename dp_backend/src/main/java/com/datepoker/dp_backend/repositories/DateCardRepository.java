package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.DateCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateCardRepository extends JpaRepository<DateCard, Long> {
}
