package org.example.stcapstonebackend.findTeam.exception;

/**
 * 동일한 태그가 이미 다른 신청 요청에서 수락되었을 때 발생하는 예외입니다.
 */
public class DuplicateAcceptanceException extends RuntimeException {
    /**
     * 예외 메시지를 포함한 DuplicateAcceptanceException을 생성합니다.
     *
     * @param message 예외 메시지
     */
    public DuplicateAcceptanceException(String message) {
        super(message);
    }
}
