package com.pooltournament.repository;

import com.pooltournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
    Optional<Tournament> findByJoinCode(String joinCode);
}
