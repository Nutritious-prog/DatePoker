package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.UserDate;
import com.datepoker.dp_backend.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDateRepository extends JpaRepository<UserDate, Long> {
    List<UserDate> findByUserProfile(UserProfile profile);
}

