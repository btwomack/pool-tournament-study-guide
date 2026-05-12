package com.pooltournament.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RecordResultRequest {
    @NotNull
    private UUID winnerId;

    @Min(0)
    private Integer player1Score;

    @Min(0)
    private Integer player2Score;
}
