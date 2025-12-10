package org.example.stcapstonebackend.findTeam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;

/**
 * 팀 찾기 신청 요청 생성 및 수정을 위한 DTO입니다.
 *
 * @param content 신청 내용
 * @param writer 신청자명
 * @param writerId 신청자 ID
 * @param desiredTag 희망하는 포지션 태그
 */
@Builder
public record FindTeamRequestRequest(
        @NotBlank String content,
        @NotBlank String writer,
        @NotNull Long writerId,
        @NotNull PositionTag desiredTag
) {
}
