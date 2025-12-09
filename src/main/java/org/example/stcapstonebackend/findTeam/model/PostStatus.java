package org.example.stcapstonebackend.findTeam.model;

/**
 * 팀 찾기 게시글의 상태를 나타내는 열거형입니다.
 */
public enum PostStatus {
    /** 활성 상태 - 아직 모든 포지션이 채워지지 않음 */
    ACTIVE,
    /** 대기 상태 - 모든 포지션이 채워졌으며 10분 후 매칭 예정 */
    PENDING,
    /** 매칭 완료 상태 - 모든 포지션이 매칭되어 게시글이 종료됨 */
    MATCHED,
    /** 만료 상태 - 생성 후 1일이 지난 게시글 */
    EXPIRED
}
