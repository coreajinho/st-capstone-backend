package org.example.stcapstonebackend.debate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.stcapstonebackend.debate.model.DebateSide;

/**
 * 토론 댓글(투표) 생성 및 수정 요청 DTO입니다.
 * writer 필드는 서버에서 writerId로부터 자동 생성됩니다.
 *
 * @param content 댓글 내용
 * @param writerId 작성자 ID (필수)
 * @param debateSide 투표한 진영 (PLAYER1 또는 PLAYER2)
 */
public record DebateCommentRequest(
        @NotBlank String content,
        @NotNull Long writerId,
        @NotNull DebateSide debateSide
) {
}
