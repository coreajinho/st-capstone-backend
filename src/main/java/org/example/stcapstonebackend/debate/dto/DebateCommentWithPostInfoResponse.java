package org.example.stcapstonebackend.debate.dto;

import lombok.Builder;
import org.example.stcapstonebackend.debate.model.DebateSide;

import java.time.LocalDateTime;

/**
 * 게시글 정보가 포함된 토론 댓글(투표) 응답 DTO입니다.
 * 주로 사용자의 댓글 목록 조회 시 사용되며, 어떤 게시글에 어떤 플레이어에게 투표했는지 확인할 수 있습니다.
 *
 * @param id 댓글 ID
 * @param content 댓글 내용
 * @param writer 작성자 표시명 (riotName#riotTag)
 * @param writerId 작성자 ID (프론트엔드에서 사용자 프로필 조회, 권한 확인 등에 사용)
 * @param debateSide 투표한 진영 (PLAYER_1 또는 PLAYER_2)
 * @param likes 좋아요 수
 * @param dislikes 싫어요 수
 * @param createdAt 생성일시
 * @param modifiedAt 수정일시
 * @param postId 댓글이 달린 게시글 ID
 * @param postTitle 게시글 제목
 * @param postWriter PLAYER_1 (게시글 작성자) 표시명
 * @param postCoWriter PLAYER_2 (게시글 공동 작성자) 표시명
 */
@Builder
public record DebateCommentWithPostInfoResponse(
        Long id,
        String content,
        String writer,
        Long writerId,
        DebateSide debateSide,
        Integer likes,
        Integer dislikes,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long postId,
        String postTitle,
        String postWriter,
        String postCoWriter
) {
}

