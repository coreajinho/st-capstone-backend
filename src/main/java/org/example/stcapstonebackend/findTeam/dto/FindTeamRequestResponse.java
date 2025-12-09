package org.example.stcapstonebackend.findTeam.dto;

import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;

import java.time.LocalDateTime;

/**
 * 팀 찾기 신청 요청 응답을 위한 DTO입니다.
 *
 * @param id 신청 요청 ID
 * @param content 신청 내용
 * @param writer 신청자명
 * @param desiredTag 희망하는 포지션 태그
 * @param isAccepted 수락 여부
 * @param createdAt 생성 일시
 * @param modifiedAt 수정 일시
 */
@Builder
public record FindTeamRequestResponse(
        Long id,
        String content,
        String writer,
        PositionTag desiredTag,
        Boolean isAccepted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
