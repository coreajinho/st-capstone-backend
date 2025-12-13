package org.example.stcapstonebackend.debate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;

import java.util.Set;

/**
 * 토론 게시글 생성 및 수정 요청 DTO입니다.
 * writer/coWriter 필드는 서버에서 writerId/coWriterId로부터 자동 생성됩니다.
 *
 * @param title 게시글 제목
 * @param content 게시글 내용
 * @param writerId 작성자 ID (필수)
 * @param coWriterId 공동 작성자 ID (선택)
 * @param videoUrl 비디오 URL
 * @param tags 포지션 태그 목록
 * @param debateDurationHours 토론 기간 (시간 단위, 필수)
 */
@Builder
public record DebatePostRequest(
        @Size(min=1, max=50) @NotBlank String title,
        @Size(min=1) @NotBlank String content,
        @NotNull Long writerId,
        Long coWriterId,
        String videoUrl,
        Set<PositionTag> tags,
        @NotNull Long debateDurationHours
) {
}
