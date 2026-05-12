package com.pooltournament.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTournamentRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private LocalDate date;

    @Size(max = 255)
    private String location;

    @Min(2)
    @Max(32)
    private Integer maxPlayers;

    @Min(0)
    @Max(500)
    private BigDecimal entryFee;

    @Min(1)
    @Max(11)
    private Integer raceToDefault = 3;

    private Boolean enableCalcutta = false;

    private UUID venueId;
}
