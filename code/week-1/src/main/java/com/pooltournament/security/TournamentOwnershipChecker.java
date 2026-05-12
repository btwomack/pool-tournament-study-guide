package com.pooltournament.security;

import com.pooltournament.entity.Tournament;
import com.pooltournament.exception.TournamentNotFoundException;
import com.pooltournament.exception.UnauthorizedAccessException;
import com.pooltournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TournamentOwnershipChecker {
    private final TournamentRepository tournamentRepository;

    /**
     * Loads the tournament and verifies the current user owns it.
     * Throws 404 if not found, 403 if not owned.
     */
    public Tournament verifyAndLoad(UUID tournamentId, UUID userId) {
        Tournament tournament = tournamentRepository
                .findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        if (!tournament.isOwnedBy(userId)) {
            throw new UnauthorizedAccessException("You do not own this tournament");
        }
        return tournament;
    }
}
