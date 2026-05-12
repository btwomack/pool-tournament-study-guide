package com.pooltournament.dto.response;

import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.Venue;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TournamentResponse {
    private UUID id;
    private String name;
    private String joinCode;
    private String status;
    private Integer maxPlayers;
    private String bracketFormat;
    private BigDecimal entryFee;
    private Integer raceToDefault;
    private Boolean enableCalcutta;
    private Venue venue;
    private LocalDate date;
    private String location;

    public static TournamentResponse fromEntity(Tournament t) {
        return TournamentResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .joinCode(t.getJoinCode())
                .status(t.getStatus().name())
                .maxPlayers(t.getMaxPlayers())
                .bracketFormat(t.getBracketFormat() != null ? t.getBracketFormat().name() : null)
                .entryFee(t.getEntryFee())
                .raceToDefault(t.getRaceToDefault())
                .enableCalcutta(t.getEnableCalcutta())
                .venue(t.getVenue())
                .date(t.getDate())
                .location(t.getLocation())
                .build();
    }
}
