package org.example.stcapstonebackend.review.dto;

/**
 * 리뷰 통계 DTO
 *
 * @param averageSkill 실력 평균
 * @param averageCooperation 협동 평균
 * @param averageMental 멘탈 평균
 * @param averageManner 매너 평균
 * @param overallAverage 전체 평균 (4개 항목의 평균)
 * @param totalReviews 총 리뷰 개수
 */
public record ReviewStatisticsDto(
    Double averageSkill,
    Double averageCooperation,
    Double averageMental,
    Double averageManner,
    Double overallAverage,
    Long totalReviews
) {}

