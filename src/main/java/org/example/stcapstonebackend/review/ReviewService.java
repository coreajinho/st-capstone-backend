package org.example.stcapstonebackend.review;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.review.dto.ReviewCreateRequest;
import org.example.stcapstonebackend.review.dto.ReviewPageResponse;
import org.example.stcapstonebackend.review.dto.ReviewResponse;
import org.example.stcapstonebackend.review.dto.ReviewStatisticsDto;
import org.example.stcapstonebackend.review.exception.DuplicateReviewException;
import org.example.stcapstonebackend.review.mapper.ReviewMapper;
import org.example.stcapstonebackend.review.model.Review;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 리뷰 관련 비즈니스 로직을 처리하는 Service
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    /**
     * 리뷰 작성
     *
     * @param reviewerId 작성자 ID
     * @param request 리뷰 작성 요청 DTO
     * @return 생성된 리뷰 응답 DTO
     */
    @Transactional
    public ReviewResponse createReview(Long reviewerId, ReviewCreateRequest request) {
        // 작성자 조회
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new UserNotFoundException("리뷰 작성자를 찾을 수 없습니다."));

        // 피평가자 조회
        User reviewee = userRepository.findById(request.revieweeId())
            .orElseThrow(() -> new UserNotFoundException("평가 대상자를 찾을 수 없습니다."));

        // 현재 년월 계산
        String currentYearMonth = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 월 1회 제약 검증
        boolean isDuplicate = reviewRepository.existsByReviewerAndRevieweeAndReviewYearMonth(
            reviewer, reviewee, currentYearMonth
        );

        if (isDuplicate) {
            throw new DuplicateReviewException(
                String.format("이미 이번 달(%s)에 해당 사용자에 대한 리뷰를 작성하셨습니다.", currentYearMonth)
            );
        }

        // 리뷰 엔티티 생성
        Review review = Review.builder()
            .reviewer(reviewer)
            .reviewee(reviewee)
            .comment(request.comment())
            .skill(request.skill())
            .cooperation(request.cooperation())
            .mental(request.mental())
            .manner(request.manner())
            .build();

        // 저장
        Review savedReview = reviewRepository.save(review);

        // DTO로 변환하여 반환
        return reviewMapper.toResponse(savedReview);
    }

    /**
     * 특정 사용자의 리뷰 목록 및 통계 조회
     *
     * @param riotName Riot 게임 이름
     * @param riotTag Riot 태그
     * @param page 페이지 번호 (0부터 시작)
     * @return 리뷰 목록 + 통계 응답 DTO
     */
    @Transactional(readOnly = true)
    public ReviewPageResponse getReviewsByRiotAccount(String riotName, String riotTag, int page) {
        // Riot 계정으로 User 조회
        User reviewee = userRepository.findByRiotNameAndRiotTag(riotName, riotTag)
            .orElseThrow(() -> new UserNotFoundException(
                String.format("Riot 계정 '%s#%s'에 해당하는 사용자를 찾을 수 없습니다.", riotName, riotTag)
            ));

        // 페이징 설정 (10개씩)
        Pageable pageable = PageRequest.of(page, 10);

        // 리뷰 목록 조회
        Page<Review> reviewPage = reviewRepository.findByRevieweeOrderByCreatedAtDesc(reviewee, pageable);

        // Review를 ReviewResponse로 변환
        Page<ReviewResponse> reviewResponsePage = reviewPage.map(reviewMapper::toResponse);

        // 통계 계산
        ReviewStatisticsDto statistics = calculateStatistics(reviewee);

        // 응답 DTO 생성
        return ReviewPageResponse.of(statistics, reviewResponsePage);
    }

    /**
     * 특정 사용자의 리뷰 통계 계산
     *
     * @param reviewee 피평가자
     * @return 리뷰 통계 DTO
     */
    private ReviewStatisticsDto calculateStatistics(User reviewee) {
        List<Review> reviews = reviewRepository.findByReviewee(reviewee);

        // 리뷰가 없는 경우
        if (reviews.isEmpty()) {
            return new ReviewStatisticsDto(
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0L
            );
        }

        // 각 항목별 평균 계산
        double averageSkill = reviews.stream()
            .mapToInt(Review::getSkill)
            .average()
            .orElse(0.0);

        double averageCooperation = reviews.stream()
            .mapToInt(Review::getCooperation)
            .average()
            .orElse(0.0);

        double averageMental = reviews.stream()
            .mapToInt(Review::getMental)
            .average()
            .orElse(0.0);

        double averageManner = reviews.stream()
            .mapToInt(Review::getManner)
            .average()
            .orElse(0.0);

        // 전체 평균 계산
        double overallAverage = (averageSkill + averageCooperation + averageMental + averageManner) / 4.0;

        return new ReviewStatisticsDto(
            Math.round(averageSkill * 100.0) / 100.0,
            Math.round(averageCooperation * 100.0) / 100.0,
            Math.round(averageMental * 100.0) / 100.0,
            Math.round(averageManner * 100.0) / 100.0,
            Math.round(overallAverage * 100.0) / 100.0,
            (long) reviews.size()
        );
    }
}

