package com.pooltournament.service;

import com.pooltournament.dto.request.GenerateBracketRequest;
import com.pooltournament.dto.response.BracketResponse;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.security.TournamentOwnershipChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BracketGeneratorServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private PlayerRegistrationRepository playerRepo;
    @Mock private TournamentRepository tournamentRepo;
    @Mock private WebSocketService wsService;
    @Mock private TournamentOwnershipChecker ownershipChecker;
    @Mock private MatchService matchService;

    @InjectMocks
    private BracketGeneratorService bracketGeneratorService;

    private Tournament tournament;
    private User admin;
    private UUID tournamentId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        admin = User.builder().id(adminId).email("admin@test.com").build();
        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Test Tournament")
                .createdBy(admin)
                .raceToDefault(3)
                .build();
    }

    private List<PlayerRegistration> createPlayers(int count) {
        List<PlayerRegistration> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(PlayerRegistration.builder()
                    .id(UUID.randomUUID())
                    .playerName("Player " + (i + 1))
                    .status(PlayerStatus.CONFIRMED)
                    .seedNumber(i + 1)
                    .build());
        }
        return players;
    }

    @Test
    void generateBracket_SingleElimination_4Players() {
        List<PlayerRegistration> players = createPlayers(4);
        when(ownershipChecker.verifyAndLoad(tournamentId, adminId)).thenReturn(tournament);
        when(playerRepo.findByTournamentAndStatus(tournament, PlayerStatus.CONFIRMED)).thenReturn(players);

        GenerateBracketRequest request = new GenerateBracketRequest();
        request.setSeedingMode("manual");

        BracketResponse response = bracketGeneratorService.generateBracket(tournamentId, adminId, request);

        assertEquals(BracketFormat.SINGLE_ELIMINATION.name(), response.getFormat());
        assertEquals(3, response.getMatches().size()); // 2 in R1, 1 in R2
        verify(matchRepository).saveAll(any());
    }

    @Test
    void generateBracket_DoubleElimination_10Players() {
        List<PlayerRegistration> players = createPlayers(10);
        when(ownershipChecker.verifyAndLoad(tournamentId, adminId)).thenReturn(tournament);
        when(playerRepo.findByTournamentAndStatus(tournament, PlayerStatus.CONFIRMED)).thenReturn(players);

        GenerateBracketRequest request = new GenerateBracketRequest();
        request.setSeedingMode("manual");

        BracketResponse response = bracketGeneratorService.generateBracket(tournamentId, adminId, request);

        assertEquals(BracketFormat.DOUBLE_ELIMINATION.name(), response.getFormat());
        assertTrue(response.getMatches().size() > 10);
        assertTrue(response.getHasGrandFinalReset());
        verify(matchRepository).saveAll(any());
    }
}
