package com.pooltournament.service;

import com.pooltournament.dto.request.RecordResultRequest;
import com.pooltournament.dto.response.MatchResultResponse;
import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.BracketType;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.enums.TournamentStatus;
import com.pooltournament.exception.InvalidMatchResultException;
import com.pooltournament.exception.TournamentNotFoundException;
import com.pooltournament.exception.UnauthorizedAccessException;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepo;
    private final PlayerRegistrationRepository playerRepo;
    private final TournamentRepository tournamentRepo;
    private final WebSocketService wsService;

    @Transactional
    public MatchResultResponse recordResult(
            UUID matchId, RecordResultRequest request,
            UUID adminUserId) {
        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new TournamentNotFoundException("Match not found"));

        // Ownership check
        Tournament t = match.getTournament();
        if (!t.isOwnedBy(adminUserId)) {
            throw new UnauthorizedAccessException("Not your tournament");
        }

        // Validate scores against race-to
        int winnerScore = Math.max(request.getPlayer1Score(), request.getPlayer2Score());
        if (winnerScore < match.getRaceTo()) {
            throw new InvalidMatchResultException("Winner must reach " + match.getRaceTo() + " games");
        }

        // Determine winner/loser
        UUID winnerId = request.getWinnerId();
        PlayerRegistration winner, loser;
        if (match.getPlayer1().getId().equals(winnerId)) {
            winner = match.getPlayer1();
            loser = match.getPlayer2();
        } else {
            winner = match.getPlayer2();
            loser = match.getPlayer1();
        }

        // Update match
        match.setWinner(winner);
        match.setLoser(loser);
        match.setPlayer1Score(request.getPlayer1Score());
        match.setPlayer2Score(request.getPlayer2Score());
        match.setCompletedAt(Instant.now());
        matchRepo.save(match);

        // Update loser
        loser.incrementLosses();
        int maxLosses = t.getBracketFormat() == BracketFormat.SINGLE_ELIMINATION ? 1 : 2;
        if (loser.getLossesCount() >= maxLosses) {
            loser.setStatus(PlayerStatus.ELIMINATED);
        }
        playerRepo.save(loser);

        boolean bracketReset = false;
        // Handle Grand Final specially
        if (match.getBracketType() == BracketType.FINALS) {
            bracketReset = handleGrandFinal(match, winner, loser, t);
        } else if (match.getBracketType() == BracketType.FINALS_RESET) {
            // GF Reset: winner is tournament champion
            winner.setStatus(PlayerStatus.WINNER);
            loser.setStatus(PlayerStatus.ELIMINATED);
            t.setStatus(TournamentStatus.COMPLETED);
            playerRepo.save(winner);
            playerRepo.save(loser);
            tournamentRepo.save(t);
        } else {
            // Normal advancement
            advanceWinner(match, winner);
            advanceLoser(match, loser, maxLosses);
        }

        // Broadcast
        wsService.broadcastMatchResult(t.getId(), matchId);
        return MatchResultResponse.builder()
                .match(match)
                .bracketReset(bracketReset)
                .build();
    }

    private boolean handleGrandFinal(
            Match gf, PlayerRegistration winner,
            PlayerRegistration loser, Tournament t) {
        // Who came from the winners bracket?
        // The winners bracket champ is always placed as player1 of the GF during advancement.
        boolean winnersChampWon = winner.getId().equals(gf.getPlayer1().getId());
        if (winnersChampWon) {
            // Tournament over. Winners champ never lost.
            winner.setStatus(PlayerStatus.WINNER);
            loser.setStatus(PlayerStatus.ELIMINATED);
            t.setStatus(TournamentStatus.COMPLETED);
            playerRepo.save(winner);
            playerRepo.save(loser);
            tournamentRepo.save(t);

            // Deactivate reset match
            if (gf.getNextMatchWinner() != null) {
                Match reset = gf.getNextMatchWinner();
                reset.setIsBye(true);
                reset.setCompletedAt(Instant.now());
                matchRepo.save(reset);
            }
            return false;
        } else {
            // Bracket reset! Both go to GF2.
            Match reset = gf.getNextMatchWinner();
            reset.setPlayer1(winner); // LB champ = player1
            reset.setPlayer2(loser);  // WB champ = player2
            matchRepo.save(reset);
            return true; // Signal bracket reset to frontend
        }
    }

    public void advanceWinner(Match match, PlayerRegistration winner) {
        if (match.getNextMatchWinner() != null) {
            placePlayerInMatch(winner, match.getNextMatchWinner());
        } else {
            // No next match = tournament winner
            winner.setStatus(PlayerStatus.WINNER);
            match.getTournament().setStatus(TournamentStatus.COMPLETED);
            playerRepo.save(winner);
            tournamentRepo.save(match.getTournament());
        }
    }

    public void advanceLoser(Match match, PlayerRegistration loser, int maxLosses) {
        if (match.getNextMatchLoser() != null && loser.getLossesCount() < maxLosses) {
            placePlayerInMatch(loser, match.getNextMatchLoser());
        }
    }

    public void placePlayerInMatch(PlayerRegistration player, Match target) {
        if (target.getPlayer1() == null) {
            target.setPlayer1(player);
        } else if (target.getPlayer2() == null) {
            target.setPlayer2(player);
        }
        matchRepo.save(target);
    }

    public void autoAdvanceBye(Match byeMatch) {
        byeMatch.setWinner(byeMatch.getPlayer1());
        byeMatch.setCompletedAt(Instant.now());
        matchRepo.save(byeMatch);
        advanceWinner(byeMatch, byeMatch.getPlayer1());
    }
}
