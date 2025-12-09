package org.example.stcapstonebackend.findTeam.exception;

/**
 * 팀 찾기 게시글을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class FindTeamPostNotFoundException extends RuntimeException {
    /**
     * 예외 메시지를 포함한 FindTeamPostNotFoundException을 생성합니다.
     *
     * @param message 예외 메시지
     */
    public FindTeamPostNotFoundException(String message) {
        super(message);
    }
}
