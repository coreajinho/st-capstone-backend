package org.example.stcapstonebackend.summoner.dto;

import lombok.Builder;
import org.example.stcapstonebackend.common.client.dto.MatchDto;

import java.util.List;

@Builder
public record MatchListResponseDto(
        List<MatchDto> matches,
        boolean hasMore
) {
}
