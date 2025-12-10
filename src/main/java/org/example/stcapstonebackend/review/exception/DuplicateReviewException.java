package org.example.stcapstonebackend.review.exception;

/**
 * 월 1회 리뷰 제약 위반 시 발생하는 예외
 */
public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String message) {
        super(message);
    }
}
