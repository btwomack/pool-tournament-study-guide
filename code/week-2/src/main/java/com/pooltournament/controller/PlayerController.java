package com.pooltournament.controller;

import com.pooltournament.dto.request.DropPlayerRequest;
import com.pooltournament.service.PlayerDropService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerDropService playerDropService;

    @PostMapping("/{playerId}/drop")
    public ResponseEntity<?> dropPlayer(
            @PathVariable UUID tournamentId,
            @PathVariable UUID playerId,
            @Valid @RequestBody DropPlayerRequest req) {
        playerDropService.dropPlayer(playerId, tournamentId, req.getReason());
        return ResponseEntity.ok().build();
    }
}
