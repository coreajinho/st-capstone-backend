package org.example.stcapstonebackend.findTeam.exception;

/**
 * 티어 범위 제약 조건 위반 시 발생하는 예외입니다.
 */
public class InvalidTierRangeException extends RuntimeException {
    public InvalidTierRangeException(String message) {
        super(message);
    }
}

