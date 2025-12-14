package org.example.stcapstonebackend.debate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.model.DebateStatus;
import org.example.stcapstonebackend.debate.model.PopularType;
import org.example.stcapstonebackend.debate.model.SearchType;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debate/posts")
@RequiredArgsConstructor
public class DebatePostController {
    private final DebatePostService debatePostService;
    private final UserRepository userRepository;

    //    토론 게시글 생성
    @PostMapping
    public ResponseEntity<DebatePostResponse> createPost(@Valid @RequestBody DebatePostRequest postDto){
        DebatePostResponse responseDto = debatePostService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseDto);
    }

    //    id로 토론 게시글 1개 조회
    @GetMapping("/{id}")
    public ResponseEntity<DebatePostResponse> getPost(@PathVariable Long id) {
        DebatePostResponse responseDto = debatePostService.getPost(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }

    /**
     * 전체 토론 게시글 조회 (상태별 필터링 지원)
     * status 파라미터가 없으면 상태 우선순위 정렬(PENDING > ACTIVE > EXPIRED)로 반환합니다.
     *
     * @param status 조회할 토론 상태 (선택, ACTIVE/PENDING/EXPIRED)
     * @return 게시글 목록
     */
    @GetMapping
    public ResponseEntity<List<DebatePostResponse>> getAllPosts(
            @RequestParam(required = false) DebateStatus status) {
        List<DebatePostResponse> posts;

        if (status != null) {
            // 특정 상태 필터링
            posts = debatePostService.getPostsByStatus(status);
        } else {
            // 상태 우선순위 정렬
            posts = debatePostService.getAllPostsOrderedByStatus();
        }

        return ResponseEntity.ok(posts);
    }

    //    id로 토론 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<DebatePostResponse> updatePost(
            @Valid @RequestBody DebatePostRequest postDto,
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        DebatePostResponse responseDto = debatePostService.updatePost(postDto, id, user.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(responseDto);
    }

    //    id로 토론 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        debatePostService.deletePost(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 토론 게시글의 득표 결과를 조회합니다.
     * @param id 게시글 ID
     * @return 득표 결과 (진영별 투표 수와 퍼센트)
     */
    @GetMapping("/{id}/vote-result")
    public ResponseEntity<DebateVoteResultDto> getVoteResult(@PathVariable Long id) {
        DebateVoteResultDto voteResult = debatePostService.getVoteResultOptimized(id);
        return ResponseEntity.ok(voteResult);
    }

    /**
     * 토론 게시글을 검색합니다.
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<DebatePostResponse>> searchPosts(
            @RequestParam SearchType searchType,
            @RequestParam String keyword) {
        List<DebatePostResponse> posts = debatePostService.searchPosts(searchType, keyword);
        return ResponseEntity.ok(posts);
    }

    /**
     * 인기글을 조회합니다.
     * @param popularType 인기글 기준 (VIEWS: 조회수, COMMENTS: 댓글 수)
     * @return 인기글 목록 (상위 10개, 많은 순서대로)
     */
    @GetMapping("/popular")
    public ResponseEntity<List<DebatePostResponse>> getPopularPosts(
            @RequestParam PopularType popularType) {
        List<DebatePostResponse> posts = debatePostService.getPopularPosts(popularType);
        return ResponseEntity.ok(posts);
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 작성자 또는 공동 작성자로 등록된 게시글을 모두 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자가 작성한 게시글 목록
     */
    @GetMapping("/my-posts")
    public ResponseEntity<List<DebatePostResponse>> getMyPosts(Authentication authentication) {
        String username = authentication.getName();
        // username으로 userId 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<DebatePostResponse> posts = debatePostService.getMyPosts(user.getId());
        return ResponseEntity.ok(posts);
    }
}
