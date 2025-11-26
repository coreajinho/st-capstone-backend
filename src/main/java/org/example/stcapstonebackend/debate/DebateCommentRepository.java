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
}