package com.pooltournament.controller;

import com.pooltournament.dto.request.RecordResultRequest;
import com.pooltournament.entity.User;
import com.pooltournament.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments/{tId}/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @PutMapping("/{matchId}")
    public ResponseEntity<?> recordResult(
            @PathVariable UUID tId,
            @PathVariable UUID matchId,
            @Valid @RequestBody RecordResultRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                matchService.recordResult(matchId, req, user.getId()));
    }
}
