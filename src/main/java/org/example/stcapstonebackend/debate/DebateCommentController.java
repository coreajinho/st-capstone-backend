package org.example.stcapstonebackend.debate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebateCommentRequest;
import org.example.stcapstonebackend.debate.dto.DebateCommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debate/posts/{postId}/comments")
@RequiredArgsConstructor
public class DebateCommentController {

    private final DebateCommentService debateCommentService;

    // 특정 게시글에 댓글 생성
    @PostMapping
    public ResponseEntity<DebateCommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody DebateCommentRequest request
    ) {
        DebateCommentResponse response = debateCommentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 특정 게시글의 모든 댓글 조회
    @GetMapping
    public ResponseEntity<List<DebateCommentResponse>> getComments(@PathVariable Long postId) {
        List<DebateCommentResponse> comments = debateCommentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 id로 특정 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<DebateCommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody DebateCommentRequest request
    ) {
        DebateCommentResponse response = debateCommentService.updateComment(postId, commentId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // 댓글 id로 특정 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId, // postId는 경로 일관성을 위해 받지만, 실제 로직에선 commentId만 사용
            @PathVariable Long commentId
    ) {
        debateCommentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    // TODO: 좋아요/싫어요 기능 - 나중에 구현 예정
    /*
    // 댓글 좋아요 추가
    @PostMapping("/{commentId}/like")
    public ResponseEntity<DebateCommentResponse> likeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        DebateCommentResponse response = debateCommentService.toggleLike(postId, commentId);
        return ResponseEntity.ok(response);
    }

    // 댓글 좋아요 취소
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<DebateCommentResponse> cancelLikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        DebateCommentResponse response = debateCommentService.cancelLike(postId, commentId);
        return ResponseEntity.ok(response);
    }

    // 댓글 싫어요 추가
    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<DebateCommentResponse> dislikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        DebateCommentResponse response = debateCommentService.toggleDislike(postId, commentId);
        return ResponseEntity.ok(response);
    }

    // 댓글 싫어요 취소
    @DeleteMapping("/{commentId}/dislike")
    public ResponseEntity<DebateCommentResponse> cancelDislikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        DebateCommentResponse response = debateCommentService.cancelDislike(postId, commentId);
        return ResponseEntity.ok(response);
    }
    */
}
