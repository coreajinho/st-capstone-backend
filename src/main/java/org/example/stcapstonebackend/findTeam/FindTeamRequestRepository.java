package org.example.stcapstonebackend.findTeam;

import org.example.stcapstonebackend.findTeam.model.FindTeamRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 팀 찾기 신청 요청 엔티티에 대한 데이터 접근 레포지토리입니다.
 */
@Repository
public interface FindTeamRequestRepository extends JpaRepository<FindTeamRequest, Long> {

    /**
     * 특정 게시글의 모든 신청 요청을 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 해당 게시글의 신청 요청 목록
     */
    List<FindTeamRequest> findByFindTeamPostId(Long postId);

    /**
     * 특정 게시글에서 수락 여부에 따른 신청 요청을 조회합니다.
     *
     * @param postId 게시글 ID
     * @param isAccepted 수락 여부
     * @return 조건에 맞는 신청 요청 목록
     */
    List<FindTeamRequest> findByFindTeamPostIdAndIsAccepted(Long postId, Boolean isAccepted);
}

