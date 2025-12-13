package org.example.stcapstonebackend.summoner.dto;

import lombok.Builder;

/**
 * 소환사 검색 결과 응답 DTO
 * 랭크 정보와 토론 관련 통계를 포함합니다.
 */
@Builder
public record SummonerSearchResponseDto(
        String nickname,
        String tagline,
        String puuid,
        String soloTier,
        String soloDivision,
        int soloPoints,
        int soloWins,
        int soloLoses,
        String flexTier,
        String flexDivision,
        int flexPoints,
        int flexWins,
        int flexLoses,
        // 회원가입 여부
        boolean isRegisteredUser,
        // 토론 관련 통계
        Integer debateWins,
        Integer debateLosses,
        Integer debateDraws,
        Integer judgementSuccesses,
        Integer judgementFailures
) {
}

