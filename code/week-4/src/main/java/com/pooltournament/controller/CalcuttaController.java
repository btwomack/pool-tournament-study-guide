package com.pooltournament.controller;

import com.pooltournament.entity.CalcuttaBid;
import com.pooltournament.entity.User;
import com.pooltournament.service.CalcuttaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/calcutta")
@RequiredArgsConstructor
public class CalcuttaController {
    private final CalcuttaService calcuttaService;

    @PostMapping("/bids")
    public ResponseEntity<CalcuttaBid> placeBid(
            @PathVariable UUID tournamentId,
            @RequestParam UUID playerId,
            @RequestParam Double amount,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(calcuttaService.placeBid(tournamentId, playerId, user.getId(), amount));
    }

    @GetMapping("/bids")
    public ResponseEntity<List<CalcuttaBid>> getBidsByTournament(@PathVariable UUID tournamentId) {
        return ResponseEntity.ok(calcuttaService.getBidsByTournament(tournamentId));
    }

    @GetMapping("/player/{playerId}/bids")
    public ResponseEntity<List<CalcuttaBid>> getBidsByPlayer(@PathVariable UUID playerId) {
        return ResponseEntity.ok(calcuttaService.getBidsByPlayer(playerId));
    }
}
