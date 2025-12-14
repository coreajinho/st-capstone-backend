package org.example.stcapstonebackend.findTeam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;
import org.example.stcapstonebackend.findTeam.model.MatchType;

import java.util.Set;

/**
 * 팀 찾기 게시글 생성 및 수정 요청을 위한 DTO입니다.
 *
 * @param title 게시글 제목 (1-100자)
 * @param content 게시글 내용 (1자 이상)
 * @param writer 작성자명 (2-15자)
 * @param writerId 작성자 ID
 * @param tags 모집하는 포지션 태그 목록 (최소 1개 이상)
 * @param matchType 매치 종류 (SOLO_RANK, FLEX_RANK, OTHER_MODES)
 * @param minTier 최소 티어 범위
 * @param maxTier 최대 티어 범위
 * @param requireMasterPlus 마스터 이상 모집 여부
 * @param masterPlusLpCap 마스터 이상 LP 상한 (null이면 무제한)
 */
@Builder
public record FindTeamPostRequest(
        @Size(min = 1, max = 100) @NotBlank String title,
        @Size(min = 1) @NotBlank String content,
        @Size(min = 2, max = 15) @NotBlank String writer,
        @NotNull Long writerId,
        @NotEmpty Set<PositionTag> tags,
        @NotNull MatchType matchType,
        @NotNull @Valid TierRange minTier,
        @NotNull @Valid TierRange maxTier,
        @NotNull Boolean requireMasterPlus,
        @Min(0) @Max(9999) Integer masterPlusLpCap
) {
}
