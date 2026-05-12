package com.pooltournament.repository;

import com.pooltournament.entity.CalcuttaBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalcuttaBidRepository extends JpaRepository<CalcuttaBid, UUID> {
    List<CalcuttaBid> findByTournament_Id(UUID tournamentId);
    List<CalcuttaBid> findByPlayer_Id(UUID playerId);
    
    @Query("SELECT b FROM CalcuttaBid b WHERE b.player.id = :playerId ORDER BY b.bidAmount DESC LIMIT 1")
    Optional<CalcuttaBid> findMaxBidByPlayerId(UUID playerId);
}
