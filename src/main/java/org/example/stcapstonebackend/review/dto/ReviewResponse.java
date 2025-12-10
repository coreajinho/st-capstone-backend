package org.example.stcapstonebackend.review.dto;

import java.time.LocalDateTime;

/**
 * 리뷰 응답 DTO
 *
 * @param id 리뷰 ID
 * @param reviewerId 리뷰 작성자 ID
 * @param reviewerName 리뷰 작성자 이름
 * @param revieweeId 피평가자 ID
 * @param revieweeName 피평가자 이름
 * @param comment 리뷰 코멘트
 * @param skill 실력 평가
 * @param cooperation 협동 평가
 * @param mental 멘탈 평가
 * @param manner 매너 평가
 * @param overallRating 총평 (4개 항목 평균)
 * @param createdAt 작성일시
 * @param modifiedAt 수정일시
 */
public record ReviewResponse(
    Long id,
    Long reviewerId,
    String reviewerName,
    Long revieweeId,
    String revieweeName,
    String comment,
    Integer skill,
    Integer cooperation,
    Integer mental,
    Integer manner,
    Double overallRating,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {}

