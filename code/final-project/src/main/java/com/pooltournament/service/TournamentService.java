package com.pooltournament.service;

import com.pooltournament.dto.request.CreateTournamentRequest;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.TournamentStatus;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepo;
    private final UserRepository userRepo;

    @Transactional
    public Tournament createTournament(CreateTournamentRequest req, UUID userId) {
        User creator = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tournament t = Tournament.builder()
                .name(req.getName())
                .entryFee(req.getEntryFee())
                .raceToDefault(req.getRaceToDefault())
                .status(TournamentStatus.REGISTRATION)
                .createdBy(creator)
                .joinCode(generateJoinCode())
                .build();

        return tournamentRepo.save(t);
    }

    public Tournament getById(UUID id) {
        return tournamentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
    }

    public Tournament getByJoinCode(String joinCode) {
        return tournamentRepo.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
