package com.pooltournament.dto.response;

import com.pooltournament.entity.Match;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BracketResponse {
    private String format;
    private List<Match> matches;
    private Integer totalMatches;
    private Boolean hasGrandFinalReset;
}
