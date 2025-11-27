package org.example.stcapstonebackend.debate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.model.PopularType;
import org.example.stcapstonebackend.debate.model.SearchType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debate/posts")
@RequiredArgsConstructor
public class DebatePostController {
    private final DebatePostService debatePostService;

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

    //    전체 토론 게시글 조회
    @GetMapping
    public ResponseEntity<List<DebatePostResponse>> getAllPosts() {
        List<DebatePostResponse> posts = debatePostService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    //    id로 토론 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<DebatePostResponse> updatePost
            (@Valid @RequestBody DebatePostRequest postDto, @PathVariable Long id) {
        DebatePostResponse responseDto = debatePostService.updatePost(postDto,id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(responseDto);
    }

    //    id로 토론 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id){
        debatePostService.deletePost(id);
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
}
