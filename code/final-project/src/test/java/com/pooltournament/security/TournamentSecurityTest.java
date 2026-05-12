package com.pooltournament.security;
import org.springframework.test.context.ActiveProfiles;

import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.UserRole;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TournamentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TournamentRepository tournamentRepo;

    @Autowired
    private UserRepository userRepo;

    private User admin;
    private User otherUser;
    private Tournament tournament;

    @Autowired
    private com.pooltournament.repository.MatchRepository matchRepo;
    @Autowired
    private com.pooltournament.repository.PlayerRegistrationRepository playerRepo;
    
    @BeforeEach
    void setUp() {
        matchRepo.deleteAll();
        playerRepo.deleteAll();
        tournamentRepo.deleteAll();
        userRepo.deleteAll();
        
        admin = User.builder().email("admin@test.com").passwordHash("hash").role(UserRole.ADMIN).build();
        userRepo.save(admin);

        otherUser = User.builder().email("other@test.com").passwordHash("hash").role(UserRole.ADMIN).build();
        userRepo.save(otherUser);

        tournament = Tournament.builder()
                .name("Security Test Tournament")
                .createdBy(admin)
                .joinCode("SEC123")
                .maxPlayers(16)
                .build();
        tournamentRepo.save(tournament);
    }

    @Test
    @WithMockUser(username = "other@test.com")
    void otherUser_CannotGenerateBracket_ForOtherTournament() throws Exception {
        mockMvc.perform(post("/api/tournaments/" + tournament.getId() + "/bracket")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seedingMode\": \"random\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void admin_CanGenerateBracket_ForOwnTournament() throws Exception {
        // Set the admin ID in the ThreadLocal for the controller to pick up
        com.pooltournament.controller.TestUserIdHolder.setUserId(admin.getId());
        try {
            // Need at least 2 confirmed players to generate a bracket
            com.pooltournament.entity.PlayerRegistration p1 = com.pooltournament.entity.PlayerRegistration.builder()
                    .tournament(tournament)
                    .playerName("P1")
                    .status(com.pooltournament.enums.PlayerStatus.CONFIRMED)
                    .build();
            com.pooltournament.entity.PlayerRegistration p2 = com.pooltournament.entity.PlayerRegistration.builder()
                    .tournament(tournament)
                    .playerName("P2")
                    .status(com.pooltournament.enums.PlayerStatus.CONFIRMED)
                    .build();
            playerRepo.save(p1);
            playerRepo.save(p2);

            mockMvc.perform(post("/api/tournaments/" + tournament.getId() + "/bracket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"seedingMode\": \"random\"}"))
                    .andExpect(status().isCreated());
        } finally {
            com.pooltournament.controller.TestUserIdHolder.clear();
        }
    }

    @Test
    void unauthenticated_CannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isForbidden());
    }
}
