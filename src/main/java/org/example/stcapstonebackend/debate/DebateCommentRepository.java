package org.example.stcapstonebackend.debate;

import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DebateCommentRepository extends JpaRepository<DebateComment, Long> {

    // 특정 게시글 ID에 해당하는 모든 댓글을 찾는 메소드
    List<DebateComment> findByDebatePostId(Long postId);

    /**
     * 특정 게시글의 토론 진영별 댓글 수를 집계합니다. (성능 최적화)
     * @param postId 게시글 ID
     * @return 진영별 댓글 수
     */
    @Query("SELECT dc.debateSide as side, COUNT(dc) as count " +
           "FROM debate_comment dc " +
           "WHERE dc.debatePost.id = :postId " +
           "GROUP BY dc.debateSide")
    List<DebateVoteCount> countByDebateSide(@Param("postId") Long postId);

    /**
     * 진영별 투표 수를 담는 Projection 인터페이스
     */
    interface DebateVoteCount {
        DebateSide getSide();
        Long getCount();
    }

    /**
     * 작성자 ID로 댓글을 조회합니다. (내 투표 조회용)
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param writerId 작성자 ID
     * @return 작성자가 작성한 댓글 목록
     */
    List<DebateComment> findByWriterIdOrderByCreatedAtDesc(Long writerId);

    /**
     * 작성자 ID로 댓글을 조회합니다. (내 투표 조회용)
     * 게시글 정보를 함께 fetch join하여 N+1 문제를 방지합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param writerId 작성자 ID
     * @return 작성자가 작성한 댓글 목록 (게시글 정보 포함)
     */
    @Query("SELECT dc FROM debate_comment dc " +
           "JOIN FETCH dc.debatePost " +
           "WHERE dc.writerId = :writerId " +
           "ORDER BY dc.createdAt DESC")
    List<DebateComment> findByWriterIdWithPostOrderByCreatedAtDesc(@Param("writerId") Long writerId);
}