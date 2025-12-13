package org.example.stcapstonebackend.debate.model;

/**
 * 토론 게시글의 상태를 나타내는 enum입니다.
 */
public enum DebateStatus {
    /**
     * 활성 상태 - 토론이 진행 중
     */
    ACTIVE,

    /**
     * 연장 상태 - 동점으로 인해 추가 시간 진행 중
     */
    PENDING,

    /**
     * 만료 상태 - 토론 기간 종료 및 결산 완료
     */
    EXPIRED
}

