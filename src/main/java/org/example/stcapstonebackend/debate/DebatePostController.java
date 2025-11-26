package org.example.stcapstonebackend.debate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debate/posts")
@RequiredArgsConstructor
public class DebatePostController {
    private final DebatePostService debatePostService;

    @PostMapping
    public ResponseEntity<DebatePostResponse> createPost(@Valid @RequestBody DebatePostRequest postDto){
        DebatePostResponse responseDto = debatePostService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebatePostResponse> getPost(@PathVariable Long id) {
        DebatePostResponse responseDto = debatePostService.getPost(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebatePostResponse> updatePost
            (@Valid @RequestBody DebatePostRequest postDto, @PathVariable Long id) {
        DebatePostResponse responseDto = debatePostService.updatePost(postDto,id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(responseDto);
    }

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

}

