package com.pooltournament.service;

import com.pooltournament.dto.request.GenerateBracketRequest;
import com.pooltournament.dto.response.BracketResponse;
import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.BracketType;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.enums.TournamentStatus;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.security.TournamentOwnershipChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BracketGeneratorService {
    private final MatchRepository matchRepository;
    private final PlayerRegistrationRepository playerRepo;
    private final TournamentRepository tournamentRepo;
    private final WebSocketService wsService;
    private final TournamentOwnershipChecker ownershipChecker;
    private final MatchService matchService;

    private BracketFormat determineFormat(Tournament tournament, int playerCount) {
        // Manual override takes precedence
        if (Boolean.TRUE.equals(tournament.getFormatOverride())
                && tournament.getBracketFormat() != null) {
            return tournament.getBracketFormat();
        }
        return playerCount >= 10
                ? BracketFormat.DOUBLE_ELIMINATION
                : BracketFormat.SINGLE_ELIMINATION;
    }

    private List<Match> generateSingleElim(
            List<PlayerRegistration> players,
            Tournament tournament,
            Map<Integer, Integer> raceToConfig) {
        int n = players.size();
        int numRounds = (int) Math.ceil(Math.log(n) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);
        int numByes = bracketSize - n;
        List<Match> allMatches = new ArrayList<>();

        // ---- Round 1 ----
        int round1Count = bracketSize / 2;
        int pi = 0; // player index
        for (int i = 0; i < round1Count; i++) {
            Match m = Match.builder()
                    .tournament(tournament)
                    .roundNumber(1)
                    .matchNumber(i + 1)
                    .bracketType(BracketType.MAIN)
                    .positionLabel("R1." + (i + 1))
                    .raceTo(raceToConfig.getOrDefault(1,
                            tournament.getRaceToDefault()))
                    .isBye(false).isForfeit(false)
                    .player1Score(0).player2Score(0)
                    .build();
            if (i < numByes) {
                // Top seed gets bye
                m.setPlayer1(players.get(pi++));
                m.setIsBye(true);
            } else {
                m.setPlayer1(players.get(pi++));
                m.setPlayer2(players.get(pi++));
            }
            allMatches.add(m);
        }

        // ---- Rounds 2..N ----
        for (int round = 2; round <= numRounds; round++) {
            int matchesInRound = (int) Math.pow(2, numRounds - round);
            for (int i = 0; i < matchesInRound; i++) {
                allMatches.add(Match.builder()
                        .tournament(tournament)
                        .roundNumber(round)
                        .matchNumber(i + 1)
                        .bracketType(BracketType.MAIN)
                        .positionLabel("R" + round + "." + (i + 1))
                        .raceTo(raceToConfig.getOrDefault(round,
                                tournament.getRaceToDefault()))
                        .isBye(false).isForfeit(false)
                        .player1Score(0).player2Score(0)
                        .build());
            }
        }

        // ---- Link matches ----
        for (Match m : allMatches) {
            if (m.getRoundNumber() < numRounds) {
                int nextMatchNum = (int) Math.ceil(m.getMatchNumber() / 2.0);
                int nextRound = m.getRoundNumber() + 1;
                allMatches.stream()
                        .filter(x -> x.getRoundNumber() == nextRound
                                && x.getMatchNumber() == nextMatchNum)
                        .findFirst()
                        .ifPresent(m::setNextMatchWinner);
            }
        }
        return allMatches;
    }

    private List<Match> generateDoubleElim(
            List<PlayerRegistration> players,
            Tournament tournament,
            Map<Integer, Integer> raceToConfig) {
        int n = players.size();
        int wRounds = (int) Math.ceil(Math.log(n) / Math.log(2));
        int bracketSize = (int) Math.pow(2, wRounds);
        int numByes = bracketSize - n;
        List<Match> winners = new ArrayList<>();
        List<Match> losers = new ArrayList<>();

        // --- Winners Round 1 ---
        int r1Count = bracketSize / 2;
        int pi = 0;
        for (int i = 0; i < r1Count; i++) {
            Match m = Match.builder()
                    .tournament(tournament)
                    .roundNumber(1).matchNumber(i + 1)
                    .bracketType(BracketType.MAIN)
                    .positionLabel("W1." + (i + 1))
                    .raceTo(raceToConfig.getOrDefault(1,
                            tournament.getRaceToDefault()))
                    .isBye(false).isForfeit(false)
                    .player1Score(0).player2Score(0)
                    .build();
            if (i < numByes) {
                m.setPlayer1(players.get(pi++));
                m.setIsBye(true);
            } else {
                m.setPlayer1(players.get(pi++));
                m.setPlayer2(players.get(pi++));
            }
            winners.add(m);
        }

        // --- Winners Rounds 2..N ---
        for (int round = 2; round <= wRounds; round++) {
            int matchesInRound = (int) Math.pow(2, wRounds - round);
            for (int i = 0; i < matchesInRound; i++) {
                winners.add(Match.builder()
                        .tournament(tournament)
                        .roundNumber(round).matchNumber(i + 1)
                        .bracketType(BracketType.MAIN)
                        .positionLabel("W" + round + "." + (i + 1))
                        .raceTo(raceToConfig.getOrDefault(round,
                                tournament.getRaceToDefault()))
                        .isBye(false).isForfeit(false)
                        .player1Score(0).player2Score(0)
                        .build());
            }
        }

        // Link winners internally
        for (Match m : winners) {
            if (m.getRoundNumber() < wRounds) {
                int nextNum = (int) Math.ceil(m.getMatchNumber() / 2.0);
                int nextRound = m.getRoundNumber() + 1;
                winners.stream()
                        .filter(x -> x.getRoundNumber() == nextRound
                                && x.getMatchNumber() == nextNum)
                        .findFirst()
                        .ifPresent(m::setNextMatchWinner);
            }
        }

        // --- Losers Bracket ---
        int lRounds = (wRounds - 1) * 2;
        for (int round = 1; round <= lRounds; round++) {
            int matchesInRound;
            if (round == 1) {
                matchesInRound = r1Count / 2;
            } else {
                final int prevRound = round - 1;
                if (round % 2 == 0) {
                    matchesInRound = (int) losers.stream()
                            .filter(x -> x.getRoundNumber() == prevRound)
                            .count() / 2;
                    if (matchesInRound == 0) matchesInRound = 1;
                } else {
                    matchesInRound = (int) losers.stream()
                            .filter(x -> x.getRoundNumber() == prevRound)
                            .count();
                }
            }
            for (int i = 0; i < matchesInRound; i++) {
                losers.add(Match.builder()
                        .tournament(tournament)
                        .roundNumber(round).matchNumber(i + 1)
                        .bracketType(BracketType.LOSERS)
                        .positionLabel("L" + round + "." + (i + 1))
                        .raceTo(raceToConfig.getOrDefault(-round,
                                tournament.getRaceToDefault()))
                        .isBye(false).isForfeit(false)
                        .player1Score(0).player2Score(0)
                        .build());
            }
        }

        // --- Link losers internally ---
        for (Match m : losers) {
            if (m.getRoundNumber() < lRounds) {
                int nextRound = m.getRoundNumber() + 1;
                int nextNum;
                if (m.getRoundNumber() % 2 == 0) {
                    nextNum = m.getMatchNumber();
                } else {
                    nextNum = (int) Math.ceil(m.getMatchNumber() / 2.0);
                }
                losers.stream()
                        .filter(x -> x.getRoundNumber() == nextRound
                                && x.getMatchNumber() == nextNum)
                        .findFirst()
                        .ifPresent(m::setNextMatchWinner);
            }
        }

        // --- Link winners -> losers (drop-downs) ---
        for (Match wm : winners) {
            if (wm.getRoundNumber() >= wRounds) continue;
            int targetLosersRound;
            if (wm.getRoundNumber() == 1) {
                targetLosersRound = 1;
            } else {
                targetLosersRound = (wm.getRoundNumber() - 1) * 2 + 1;
            }
            int targetMatchNum = (int) Math.ceil(wm.getMatchNumber() / 2.0);
            losers.stream()
                    .filter(lm -> lm.getRoundNumber() == targetLosersRound
                            && lm.getMatchNumber() == targetMatchNum)
                    .findFirst()
                    .ifPresent(wm::setNextMatchLoser);
        }

        // --- Grand Final ---
        Match gf = Match.builder()
                .tournament(tournament)
                .roundNumber(1).matchNumber(1)
                .bracketType(BracketType.FINALS)
                .positionLabel("GF")
                .raceTo(raceToConfig.getOrDefault(99,
                        tournament.getRaceToDefault()))
                .isBye(false).isForfeit(false)
                .player1Score(0).player2Score(0)
                .build();

        // --- Grand Final Reset ---
        Match gfReset = Match.builder()
                .tournament(tournament)
                .roundNumber(2).matchNumber(1)
                .bracketType(BracketType.FINALS_RESET)
                .positionLabel("GF2")
                .raceTo(gf.getRaceTo())
                .isBye(false).isForfeit(false)
                .player1Score(0).player2Score(0)
                .build();

        // Connect brackets to Grand Final
        Match winnersChamp = winners.get(winners.size() - 1);
        Match losersChamp = losers.get(losers.size() - 1);
        winnersChamp.setNextMatchWinner(gf);
        losersChamp.setNextMatchWinner(gf);
        gf.setNextMatchWinner(gfReset);

        List<Match> all = new ArrayList<>();
        all.addAll(winners);
        all.addAll(losers);
        all.add(gf);
        all.add(gfReset);
        return all;
    }

    @Transactional
    public BracketResponse generateBracket(
            UUID tournamentId, UUID userId,
            GenerateBracketRequest request) {
        Tournament t = ownershipChecker.verifyAndLoad(tournamentId, userId);
        List<PlayerRegistration> players = playerRepo
                .findByTournamentAndStatus(t, PlayerStatus.CONFIRMED);
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 confirmed players");
        }

        // Apply seeding
        if ("random".equals(request.getSeedingMode())) {
            Collections.shuffle(players);
            for (int i = 0; i < players.size(); i++) {
                players.get(i).setSeedNumber(i + 1);
            }
            playerRepo.saveAll(players);
        }
        players.sort(Comparator.comparing(PlayerRegistration::getSeedNumber));

        // Determine format
        BracketFormat format = determineFormat(t, players.size());
        t.setBracketFormat(format);
        t.setStatus(TournamentStatus.ACTIVE);
        tournamentRepo.save(t);

        // Generate matches
        Map<Integer, Integer> raceToConfig = request.getRaceToConfig() != null
                ? request.getRaceToConfig()
                : Map.of();
        List<Match> matches = format == BracketFormat.SINGLE_ELIMINATION
                ? generateSingleElim(players, t, raceToConfig)
                : generateDoubleElim(players, t, raceToConfig);

        // Save all matches in one batch
        matchRepository.saveAll(matches);

        // Auto-resolve byes
        for (Match m : matches) {
            if (Boolean.TRUE.equals(m.getIsBye()) && m.getPlayer1() != null) {
                matchService.autoAdvanceBye(m);
            }
        }

        // Broadcast to all viewers
        wsService.broadcastBracketUpdate(tournamentId);

        return BracketResponse.builder()
                .format(format.name())
                .matches(matches)
                .totalMatches(matches.size())
                .hasGrandFinalReset(format == BracketFormat.DOUBLE_ELIMINATION)
                .build();
    }
}
