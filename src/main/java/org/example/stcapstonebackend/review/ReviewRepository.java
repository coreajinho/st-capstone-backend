package org.example.stcapstonebackend.review;

import org.example.stcapstonebackend.review.model.Review;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Review 엔티티의 데이터베이스 접근을 담당하는 Repository
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 사용자가 받은 리뷰를 최신순으로 페이징 조회
     *
     * @param reviewee 피평가자
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByRevieweeOrderByCreatedAtDesc(User reviewee, Pageable pageable);

    /**
     * 특정 사용자가 받은 모든 리뷰 조회 (통계 계산용)
     *
     * @param reviewee 피평가자
     * @return 리뷰 목록
     */
    List<Review> findByReviewee(User reviewee);

    /**
     * 특정 월에 리뷰 작성자와 피평가자 조합으로 리뷰 존재 여부 확인
     *
     * @param reviewer 리뷰 작성자
     * @param reviewee 피평가자
     * @param reviewYearMonth 리뷰 작성 년월 (YYYY-MM)
     * @return 존재 여부
     */
    boolean existsByReviewerAndRevieweeAndReviewYearMonth(
        User reviewer,
        User reviewee,
        String reviewYearMonth
    );
}

