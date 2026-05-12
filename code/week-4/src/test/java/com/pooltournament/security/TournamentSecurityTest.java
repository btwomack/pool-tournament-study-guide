package com.pooltournament.security;

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

    @BeforeEach
    void setUp() {
        admin = User.builder().email("admin@test.com").passwordHash("hash").role(UserRole.ADMIN).build();
        userRepo.save(admin);

        otherUser = User.builder().email("other@test.com").passwordHash("hash").role(UserRole.ADMIN).build();
        userRepo.save(otherUser);

        tournament = Tournament.builder()
                .name("Security Test Tournament")
                .createdBy(admin)
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
    @WithMockUser(username = "admin@test.com")
    void admin_CanGenerateBracket_ForOwnTournament() throws Exception {
        mockMvc.perform(post("/api/tournaments/" + tournament.getId() + "/bracket")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seedingMode\": \"random\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthenticated_CannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isForbidden());
    }
}
