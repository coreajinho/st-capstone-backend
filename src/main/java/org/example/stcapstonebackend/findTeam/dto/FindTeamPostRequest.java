package org.example.stcapstonebackend.findTeam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;

import java.util.Set;

/**
 * 팀 찾기 게시글 생성 및 수정 요청을 위한 DTO입니다.
 *
 * @param title 게시글 제목 (1-100자)
 * @param content 게시글 내용 (1자 이상)
 * @param writer 작성자명 (2-15자)
 * @param tags 모집하는 포지션 태그 목록 (최소 1개 이상)
 */
@Builder
public record FindTeamPostRequest(
        @Size(min = 1, max = 100) @NotBlank String title,
        @Size(min = 1) @NotBlank String content,
        @Size(min = 2, max = 15) @NotBlank String writer,
        @NotEmpty Set<PositionTag> tags
) {
}
