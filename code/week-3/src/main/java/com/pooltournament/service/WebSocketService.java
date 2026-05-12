package com.pooltournament.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastBracketUpdate(UUID tournamentId) {
        messagingTemplate.convertAndSend("/topic/tournament/" + tournamentId, 
                Map.of("type", "BRACKET_UPDATE", "tournamentId", tournamentId));
    }

    public void broadcastMatchResult(UUID tournamentId, UUID matchId) {
        messagingTemplate.convertAndSend("/topic/tournament/" + tournamentId, 
                Map.of("type", "MATCH_RESULT", "matchId", matchId));
    }

    public void broadcastBracketReset(UUID tournamentId) {
        messagingTemplate.convertAndSend("/topic/tournament/" + tournamentId, 
                Map.of("type", "BRACKET_RESET", "tournamentId", tournamentId));
    }

    public void broadcastPlayerDrop(UUID tournamentId, UUID playerId) {
        messagingTemplate.convertAndSend("/topic/tournament/" + tournamentId, 
                Map.of("type", "PLAYER_DROP", "playerId", playerId));
    }
}
