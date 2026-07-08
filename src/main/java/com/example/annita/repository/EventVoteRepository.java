package com.example.annita.repository;

import com.example.annita.model.EventVote;
import com.example.annita.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventVoteRepository extends JpaRepository<EventVote, UUID> {

    Optional<EventVote> findByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventIdAndType(UUID eventId, VoteType type);

    List<EventVote> findByUserIdAndEventIdIn(UUID userId, List<UUID> eventIds);

    @Query("SELECT v.event.id, v.type, COUNT(v) FROM EventVote v WHERE v.event.id IN :eventIds GROUP BY v.event.id, v.type")
    List<Object[]> countByEventIdInGroupByType(@Param("eventIds") List<UUID> eventIds);

    void deleteByEventIdAndUserId(UUID eventId, UUID userId);

    void deleteByEventId(UUID eventId);
}
