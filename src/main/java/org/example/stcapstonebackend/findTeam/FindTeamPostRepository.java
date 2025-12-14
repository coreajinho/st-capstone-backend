package org.example.stcapstonebackend.findTeam;

import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.example.stcapstonebackend.findTeam.model.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팀 찾기 게시글 엔티티에 대한 데이터 접근 레포지토리입니다.
 */
@Repository
public interface FindTeamPostRepository extends JpaRepository<FindTeamPost, Long> {

    /**
     * 특정 상태의 게시글을 조회합니다.
     *
     * @param status 조회할 게시글 상태
     * @return 해당 상태의 게시글 목록
     */
    List<FindTeamPost> findByStatus(PostStatus status);

    /**
     * 만료 예정 시간이 지난 게시글을 조회합니다.
     *
     * @param status 조회할 게시글 상태
     * @param now 현재 시간
     * @return 만료된 게시글 목록
     */
    @Query("SELECT p FROM find_team_post p WHERE p.status = :status AND p.pendingExpirationAt <= :now")
    List<FindTeamPost> findExpiredPosts(PostStatus status, LocalDateTime now);

    /**
     * 제목에 특정 문자열을 포함하는 게시글을 검색합니다.
     * 대소문자를 구분하지 않습니다.
     *
     * @param title 검색할 제목 문자열
     * @return 검색된 게시글 목록
     */
    List<FindTeamPost> findByTitleContainingIgnoreCase(String title);

    /**
     * 작성자명에 특정 문자열을 포함하는 게시글을 검색합니다.
     * 대소문자를 구분하지 않습니다.
     *
     * @param writer 검색할 작성자명 문자열
     * @return 검색된 게시글 목록
     */
    List<FindTeamPost> findByWriterContainingIgnoreCase(String writer);

    /**
     * 내용에 특정 문자열을 포함하는 게시글을 검색합니다.
     * 대소문자를 구분하지 않습니다.
     *
     * @param content 검색할 내용 문자열
     * @return 검색된 게시글 목록
     */
    List<FindTeamPost> findByContentContainingIgnoreCase(String content);

    /**
     * 제목 또는 내용에 특정 문자열을 포함하는 게시글을 검색합니다.
     * 대소문자를 구분하지 않습니다.
     *
     * @param titleKeyword 제목 검색 키워드
     * @param contentKeyword 내용 검색 키워드
     * @return 검색된 게시글 목록
     */
    List<FindTeamPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);

    /**
     * 특정 상태의 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @param status 조회할 게시글 상태
     * @return 정렬된 게시글 목록
     */
    List<FindTeamPost> findByStatusOrderByCreatedAtDesc(PostStatus status);

    /**
     * 특정 작성자의 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @param writer 작성자명
     * @return 정렬된 게시글 목록
     */
    List<FindTeamPost> findByWriterOrderByCreatedAtDesc(String writer);

    /**
     * 모든 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    List<FindTeamPost> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @param status 조회할 게시글 상태
     * @return 정렬된 게시글 목록
     */
    List<FindTeamPost> findByStatusOrderByCreatedAtAsc(PostStatus status);

    /**
     * 모든 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    List<FindTeamPost> findAllByOrderByCreatedAtAsc();

    /**
     * 티어 범위 교차(Range Intersection) 조건으로 게시글을 필터링합니다.
     * 사용자가 입력한 티어 범위와 게시글의 모집 티어 범위가 조금이라도 겹치면 결과에 포함됩니다.
     *
     * 조건: Max(userMin, postMin) <= Min(userMax, postMax)
     *
     * @param userMinScore 사용자의 최소 티어 점수
     * @param userMaxScore 사용자의 최대 티어 점수
     * @param status 게시글 상태 (ACTIVE만 조회)
     * @return 필터링된 게시글 목록
     */
    @Query("SELECT p FROM find_team_post p WHERE " +
           "p.status = :status AND " +
           "GREATEST(p.minTierScore, :userMinScore) <= LEAST(p.maxTierScore, :userMaxScore) " +
           "ORDER BY p.createdAt DESC")
    List<FindTeamPost> findByTierRangeIntersection(Integer userMinScore, Integer userMaxScore, PostStatus status);

    /**
     * 티어 범위와 매치 타입으로 게시글을 필터링합니다.
     *
     * @param userMinScore 사용자의 최소 티어 점수
     * @param userMaxScore 사용자의 최대 티어 점수
     * @param matchType 매치 타입
     * @param status 게시글 상태
     * @return 필터링된 게시글 목록
     */
    @Query("SELECT p FROM find_team_post p WHERE " +
           "p.status = :status AND " +
           "p.matchType = :matchType AND " +
           "GREATEST(p.minTierScore, :userMinScore) <= LEAST(p.maxTierScore, :userMaxScore) " +
           "ORDER BY p.createdAt DESC")
    List<FindTeamPost> findByTierRangeAndMatchType(Integer userMinScore, Integer userMaxScore,
                                                     org.example.stcapstonebackend.findTeam.model.MatchType matchType,
                                                     PostStatus status);
}

