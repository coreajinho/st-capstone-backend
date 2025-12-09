package org.example.stcapstonebackend.findTeam.exception;

/**
 * 팀 찾기 신청 요청을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class FindTeamRequestNotFoundException extends RuntimeException {
    /**
     * 예외 메시지를 포함한 FindTeamRequestNotFoundException을 생성합니다.
     *
     * @param message 예외 메시지
     */
    public FindTeamRequestNotFoundException(String message) {
        super(message);
    }
}
