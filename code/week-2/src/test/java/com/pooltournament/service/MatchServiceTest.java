package com.pooltournament.service;

import com.pooltournament.dto.request.RecordResultRequest;
import com.pooltournament.dto.response.MatchResultResponse;
import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.BracketType;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock private MatchRepository matchRepo;
    @Mock private PlayerRegistrationRepository playerRepo;
    @Mock private TournamentRepository tournamentRepo;
    @Mock private WebSocketService wsService;

    @InjectMocks
    private MatchService matchService;

    private Tournament tournament;
    private User admin;
    private UUID adminId;
    private PlayerRegistration player1;
    private PlayerRegistration player2;
    private Match match;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        admin = User.builder().id(adminId).email("admin@test.com").build();
        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Test Tournament")
                .createdBy(admin)
                .bracketFormat(BracketFormat.SINGLE_ELIMINATION)
                .build();
        
        player1 = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Player 1")
                .lossesCount(0)
                .build();
        player2 = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Player 2")
                .lossesCount(0)
                .build();
        
        matchId = UUID.randomUUID();
        match = Match.builder()
                .id(matchId)
                .tournament(tournament)
                .player1(player1)
                .player2(player2)
                .raceTo(3)
                .bracketType(BracketType.MAIN)
                .build();
    }

    @Test
    void recordResult_Success() {
        RecordResultRequest request = new RecordResultRequest();
        request.setWinnerId(player1.getId());
        request.setPlayer1Score(3);
        request.setPlayer2Score(1);

        when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

        MatchResultResponse response = matchService.recordResult(matchId, request, adminId);

        assertNotNull(response);
        assertEquals(player1, response.getMatch().getWinner());
        assertEquals(player2, response.getMatch().getLoser());
        assertEquals(3, response.getMatch().getPlayer1Score());
        assertEquals(1, response.getMatch().getPlayer2Score());
        assertEquals(1, player2.getLossesCount());
        assertEquals(PlayerStatus.ELIMINATED, player2.getStatus());

        verify(matchRepo).save(match);
        verify(playerRepo).save(player2);
        verify(wsService).broadcastMatchResult(any(), any());
    }

    @Test
    void recordResult_InvalidScore_ThrowsException() {
        RecordResultRequest request = new RecordResultRequest();
        request.setWinnerId(player1.getId());
        request.setPlayer1Score(2); // Should be at least 3
        request.setPlayer2Score(1);

        when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

        assertThrows(RuntimeException.class, () -> 
            matchService.recordResult(matchId, request, adminId)
        );
    }
}
