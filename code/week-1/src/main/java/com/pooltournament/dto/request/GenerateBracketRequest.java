package com.pooltournament.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class GenerateBracketRequest {
    @Pattern(regexp = "manual|random")
    private String seedingMode;

    private String formatOverride;

    private Map<Integer, Integer> raceToConfig;
}
