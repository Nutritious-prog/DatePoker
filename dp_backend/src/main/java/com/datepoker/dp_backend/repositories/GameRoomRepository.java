package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.GameRoom;
import com.datepoker.dp_backend.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByCode(String code);
    boolean existsByCreatorAndIsActiveTrue(UserProfile creator);
    boolean existsByJoinerAndIsActiveTrue(UserProfile joiner);
    @Modifying
    @Query("UPDATE GameRoom r SET r.status = com.datepoker.dp_backend.entities.GameRoom.Status.CANCELLED, r.isActive = false WHERE r.status = com.datepoker.dp_backend.entities.GameRoom.Status.WAITING AND r.createdAt <= :cutoff")
    void expireOldRooms(@Param("cutoff") LocalDateTime cutoff);
    List<GameRoom> findAllByStatusAndLastActivityBefore(GameRoom.Status status, LocalDateTime threshold);
    @Query("""
    SELECT r FROM GameRoom r
    WHERE r.isActive = true AND (r.creator = :profile OR r.joiner = :profile)""")
    Optional<GameRoom> findActiveRoomByUser(@Param("profile") UserProfile profile);

}
