package org.example.stcapstonebackend.debate.dto;

import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 토론 게시글 응답 DTO입니다.
 *
 * @param id 게시글 ID
 * @param title 제목
 * @param content 내용
 * @param writer 작성자 표시명 (riotName#riotTag)
 * @param writerId 작성자 ID (프론트엔드에서 사용자 프로필 조회, 권한 확인 등에 사용)
 * @param coWriter 공동 작성자 표시명 (riotName#riotTag)
 * @param coWriterId 공동 작성자 ID (프론트엔드에서 사용자 프로필 조회 등에 사용)
 * @param videoUrl 비디오 URL
 * @param views 조회수
 * @param commentCount 댓글 수
 * @param tags 포지션 태그 목록
 * @param createdAt 생성일시
 * @param modifiedAt 수정일시
 * @param comments 댓글 목록
 */
@Builder
public record DebatePostResponse(
        Long id,
        String title,
        String content,
        String writer,
        Long writerId,
        String coWriter,
        Long coWriterId,
        String videoUrl,
        int views,
        int commentCount,
        Set<PositionTag> tags,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<DebateCommentResponse> comments
) {
}
