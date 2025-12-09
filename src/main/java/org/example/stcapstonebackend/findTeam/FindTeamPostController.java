package org.example.stcapstonebackend.findTeam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 팀 찾기 게시글 관련 REST API를 제공하는 컨트롤러입니다.
 * 게시글의 CRUD 작업 및 검색 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/find-team/posts")
@RequiredArgsConstructor
public class FindTeamPostController {

    private final FindTeamPostService findTeamPostService;

    /**
     * 새로운 팀 찾기 게시글을 생성합니다.
     *
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보
     */
    @PostMapping
    public ResponseEntity<FindTeamPostResponse> createPost(@Valid @RequestBody FindTeamPostRequest request) {
        FindTeamPostResponse response = findTeamPostService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param id 수정할 게시글의 ID
     * @param request 수정할 게시글 데이터
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public ResponseEntity<FindTeamPostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody FindTeamPostRequest request
    ) {
        FindTeamPostResponse response = findTeamPostService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param id 삭제할 게시글의 ID
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        findTeamPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ID로 특정 게시글을 조회합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 조회된 게시글 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<FindTeamPostResponse> getPost(@PathVariable Long id) {
        FindTeamPostResponse response = findTeamPostService.getPost(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 전체 게시글 목록
     */
    @GetMapping
    public ResponseEntity<List<FindTeamPostResponse>> getAllPosts() {
        List<FindTeamPostResponse> responses = findTeamPostService.getAllPosts();
        return ResponseEntity.ok(responses);
    }

    /**
     * 활성 상태의 게시글만 조회합니다.
     *
     * @return 활성 상태의 게시글 목록
     */
    @GetMapping("/active")
    public ResponseEntity<List<FindTeamPostResponse>> getActivePosts() {
        List<FindTeamPostResponse> responses = findTeamPostService.getActivePosts();
        return ResponseEntity.ok(responses);
    }

    /**
     * 팀 찾기 게시글을 검색합니다.
     *
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<FindTeamPostResponse>> searchPosts(
            @RequestParam org.example.stcapstonebackend.findTeam.model.SearchType searchType,
            @RequestParam String keyword) {
        List<FindTeamPostResponse> responses = findTeamPostService.searchPosts(searchType, keyword);
        return ResponseEntity.ok(responses);
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param principal 인증된 사용자 정보
     * @return 사용자가 작성한 게시글 목록
     */
    @GetMapping("/my-posts")
    public ResponseEntity<List<FindTeamPostResponse>> getMyPosts(
            java.security.Principal principal) {
        String username = principal.getName();
        List<FindTeamPostResponse> responses = findTeamPostService.getMyPosts(username);
        return ResponseEntity.ok(responses);
    }
}
