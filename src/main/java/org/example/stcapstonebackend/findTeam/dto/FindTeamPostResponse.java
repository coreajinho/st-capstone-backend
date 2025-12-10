package org.example.stcapstonebackend.findTeam.dto;

import lombok.Builder;
import org.example.stcapstonebackend.common.model.PositionTag;
import org.example.stcapstonebackend.findTeam.model.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 팀 찾기 게시글 응답을 위한 DTO입니다.
 *
 * @param id 게시글 ID
 * @param title 게시글 제목
 * @param content 게시글 내용
 * @param writer 작성자명
 * @param writerId 작성자 ID
 * @param tags 모집하는 포지션 태그 목록
 * @param acceptedTags 수락된 태그와 요청 ID 매핑 (Key: PositionTag, Value: 요청 ID)
 * @param availableTags 아직 수락되지 않은 태그 목록
 * @param status 게시글 상태 (ACTIVE, PENDING_EXPIRATION, EXPIRED)
 * @param pendingExpirationAt 만료 예정 시간
 * @param requestCount 신청 요청 수
 * @param createdAt 생성 일시
 * @param modifiedAt 수정 일시
 * @param requests 신청 요청 목록
 */
@Builder
public record FindTeamPostResponse(
        Long id,
        String title,
        String content,
        String writer,
        Long writerId,
        Set<PositionTag> tags,
        Map<PositionTag, Long> acceptedTags,
        Set<PositionTag> availableTags,
        PostStatus status,
        LocalDateTime pendingExpirationAt,
        int requestCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<FindTeamRequestResponse> requests
) {
}
