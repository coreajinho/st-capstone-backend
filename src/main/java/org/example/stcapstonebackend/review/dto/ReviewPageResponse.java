package org.example.stcapstonebackend.review.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 리뷰 목록 + 통계 응답 DTO
 *
 * @param statistics 리뷰 통계
 * @param reviews 리뷰 목록
 * @param currentPage 현재 페이지 번호 (0부터 시작)
 * @param totalPages 총 페이지 수
 * @param totalElements 총 리뷰 개수
 * @param pageSize 페이지당 항목 수
 * @param isLast 마지막 페이지 여부
 */
public record ReviewPageResponse(
    ReviewStatisticsDto statistics,
    List<ReviewResponse> reviews,
    Integer currentPage,
    Integer totalPages,
    Long totalElements,
    Integer pageSize,
    Boolean isLast
) {
    /**
     * Page 객체로부터 ReviewPageResponse 생성
     *
     * @param statistics 리뷰 통계 정보
     * @param reviewPage 페이징된 리뷰 목록
     * @return ReviewPageResponse 인스턴스
     */
    public static ReviewPageResponse of(
        ReviewStatisticsDto statistics,
        Page<ReviewResponse> reviewPage
    ) {
        return new ReviewPageResponse(
            statistics,
            reviewPage.getContent(),
            reviewPage.getNumber(),
            reviewPage.getTotalPages(),
            reviewPage.getTotalElements(),
            reviewPage.getSize(),
            reviewPage.isLast()
        );
    }
}

