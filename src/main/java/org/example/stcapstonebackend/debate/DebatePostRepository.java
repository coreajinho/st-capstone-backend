package org.example.stcapstonebackend.debate;

import org.example.stcapstonebackend.debate.model.DebatePost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebatePostRepository extends JpaRepository<DebatePost,Long> {
    
    // 제목으로 검색 (대소문자 구분 없이)
    List<DebatePost> findByTitleContainingIgnoreCase(String keyword);
    
    // 내용으로 검색 (대소문자 구분 없이)
    List<DebatePost> findByContentContainingIgnoreCase(String keyword);
    
    // 제목 또는 내용으로 검색 (대소문자 구분 없이)
    List<DebatePost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);
    
    // 작성자(writer 또는 coWriter)로 검색 (대소문자 구분 없이)
    @Query("SELECT d FROM debate_post d WHERE LOWER(d.writer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.coWriter) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DebatePost> findByWriterOrCoWriterContainingIgnoreCase(@Param("keyword") String keyword);

    // 조회수 기준 상위 N개 게시글 조회
    @Query("SELECT d FROM debate_post d ORDER BY d.views DESC")
    List<DebatePost> findTopByOrderByViewsDesc(Pageable pageable);

    // 댓글 수 기준 상위 N개 게시글 조회
    @Query("SELECT d FROM debate_post d LEFT JOIN d.comments c GROUP BY d ORDER BY COUNT(c) DESC")
    List<DebatePost> findTopByOrderByCommentsDesc(Pageable pageable);
}
