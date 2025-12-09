package org.example.stcapstonebackend.findTeam.exception;

/**
 * 유효하지 않은 태그를 선택했거나 이미 수락된 태그를 선택했을 때 발생하는 예외입니다.
 */
public class InvalidTagSelectionException extends RuntimeException {
    /**
     * 예외 메시지를 포함한 InvalidTagSelectionException을 생성합니다.
     *
     * @param message 예외 메시지
     */
    public InvalidTagSelectionException(String message) {
        super(message);
    }
}
