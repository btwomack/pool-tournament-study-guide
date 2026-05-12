package com.pooltournament.repository;

import com.pooltournament.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByTournamentId(UUID tournamentId);

    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND (m.player1.id = :playerId OR m.player2.id = :playerId) AND m.completedAt IS NULL")
    List<Match> findPendingByPlayer(UUID tournamentId, UUID playerId);
}
