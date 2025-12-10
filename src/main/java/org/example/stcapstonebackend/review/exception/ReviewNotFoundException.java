package org.example.stcapstonebackend.review.exception;

/**
 * 리뷰를 찾을 수 없을 때 발생하는 예외
 */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
