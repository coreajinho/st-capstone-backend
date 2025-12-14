package org.example.stcapstonebackend.findTeam.dto;

import lombok.Builder;

/**
 * 게시글 작성자의 티어 정보 및 허용 가능한 티어 범위 응답 DTO입니다.
 * 프론트엔드에서 게시글 작성 시 작성자의 티어 정보를 표시하고,
 * 팀원 모집 가능한 티어 범위를 제안하기 위해 사용됩니다.
 *
 * @param summonerName 소환사명
 * @param summonerTag 소환사 태그
 * @param soloTier 솔로랭크 티어 (예: "GOLD", "PLATINUM")
 * @param soloDivision 솔로랭크 Division (예: "I", "II", "III", "IV")
 * @param soloLp 솔로랭크 LP
 * @param flexTier 자유랭크 티어 (예: "GOLD", "PLATINUM")
 * @param flexDivision 자유랭크 Division (예: "I", "II", "III", "IV")
 * @param flexLp 자유랭크 LP
 * @param soloRankMinTier 솔로랭크 듀오 가능 최소 티어 (null이면 듀오 불가)
 * @param soloRankMaxTier 솔로랭크 듀오 가능 최대 티어 (null이면 듀오 불가)
 * @param flexRankMinTier 자유랭크/기타모드 허용 최소 티어
 * @param flexRankMaxTier 자유랭크/기타모드 허용 최대 티어
 * @param flexRankMasterPlusAllowed 자유랭크/기타모드 마스터 이상 허용 여부
 */
@Builder
public record WriterTierInfoResponse(
        String summonerName,
        String summonerTag,
        String soloTier,
        String soloDivision,
        Integer soloLp,
        String flexTier,
        String flexDivision,
        Integer flexLp,
        TierRange soloRankMinTier,
        TierRange soloRankMaxTier,
        TierRange flexRankMinTier,
        TierRange flexRankMaxTier,
        Boolean flexRankMasterPlusAllowed
) {
}

