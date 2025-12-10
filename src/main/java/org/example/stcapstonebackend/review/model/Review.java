package org.example.stcapstonebackend.review.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.debate.model.BaseEntity;
import org.example.stcapstonebackend.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 사용자 리뷰 엔티티
 * 한 사용자가 다른 사용자를 평가한 리뷰 정보를 저장합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "review")
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_monthly",
            columnNames = {"reviewer_id", "reviewee_id", "review_year_month"}
        )
    }
)
@Builder(toBuilder = true)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * 리뷰 작성자
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /**
     * 리뷰 대상자 (피평가자)
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    /**
     * 리뷰 코멘트
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    /**
     * 실력 평가 (0-5점)
     */
    @Column(nullable = false)
    private Integer skill;

    /**
     * 협동 평가 (0-5점)
     */
    @Column(nullable = false)
    private Integer cooperation;

    /**
     * 멘탈 평가 (0-5점)
     */
    @Column(nullable = false)
    private Integer mental;

    /**
     * 매너 평가 (0-5점)
     */
    @Column(nullable = false)
    private Integer manner;

    /**
     * 리뷰 작성 년월 (YYYY-MM 형식)
     * 월 1회 리뷰 제약을 위해 사용
     */
    @Column(name = "review_year_month", nullable = false, length = 7)
    private String reviewYearMonth;

    /**
     * 엔티티 저장 전 자동으로 reviewYearMonth 설정
     */
    @PrePersist
    protected void onCreate() {
        if (reviewYearMonth == null) {
            reviewYearMonth = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    /**
     * 총평 (4개 항목의 평균)
     * @return 실력, 협동, 멘탈, 매너의 평균값
     */
    public Double calculateOverallRating() {
        return (skill + cooperation + mental + manner) / 4.0;
    }

    /**
     * 별점 유효성 검증
     * @param rating 검증할 별점 값
     * @throws IllegalArgumentException 별점이 0-5 범위를 벗어난 경우
     */
    private void validateRating(Integer rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("별점은 0-5 사이의 값이어야 합니다.");
        }
    }
}

