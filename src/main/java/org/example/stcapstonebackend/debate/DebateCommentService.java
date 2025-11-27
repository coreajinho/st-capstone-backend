package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebateCommentRequest;
import org.example.stcapstonebackend.debate.dto.DebateCommentResponse;
import org.example.stcapstonebackend.common.exception.CommentNotFoundException;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.debate.mapper.DebateCommentMapper;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // ✨ 클래스 레벨에 @Transactional을 선언하여 모든 public 메소드에 트랜잭션을 적용
public class DebateCommentService {

    private final DebatePostRepository debatePostRepository;
    private final DebateCommentRepository debateCommentRepository;
    private final DebateCommentMapper debateCommentMapper;

    // 댓글 생성
    public DebateCommentResponse createComment(Long postId, DebateCommentRequest request) {
        // 1. 게시글 조회 및 예외 처리
        DebatePost post = debatePostRepository.findById(postId)
                .orElseThrow(() -> new DebatePostNotFoundException("해당 ID의 게시글을 찾을 수 없습니다."));

        // 2. 댓글 객체(아직 Entity 아님) 생성
        DebateComment comment = DebateComment.builder()
                .content(request.content())
                .writer(request.writer())
                .debateSide(request.debateSide())
                .build();

        // 3. 연관관계 설정
        post.addComment(comment);

        // 4. 댓글 저장 (cascade 설정으로 post만 저장해도 되지만, 명시적으로 comment를 저장)
        DebateComment savedComment = debateCommentRepository.save(comment);

        return debateCommentMapper.toDto(savedComment);
    }

    // 특정 게시글의 모든 댓글 조회
    @Transactional(readOnly = true) // 조회 기능은 readOnly=true로 설정하여 성능 최적화
    public List<DebateCommentResponse> getCommentsByPostId(Long postId) {
        if (!debatePostRepository.existsById(postId)) {
            throw new DebatePostNotFoundException("해당 ID의 게시글을 찾을 수 없습니다.");
        }

        return debateCommentRepository.findByDebatePostId(postId)
                .stream()
                .map(debateCommentMapper::toDto)
                .toList();
    }

    // 댓글 수정
    public DebateCommentResponse updateComment(Long postId, Long commentId, DebateCommentRequest request) {
        // 1. commentId로 댓글을 먼저 조회합니다.
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 2. 조회한 댓글의 부모 게시글 ID와 URL로 넘어온 postId가 일치하는지 확인합니다.
        if (!comment.getDebatePost().getId().equals(postId)) {
            // 일치하지 않으면, 권한이 없거나 잘못된 요청으로 간주하고 예외를 발생시킵니다.
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        // 3. 검증이 통과된 경우에만 안전하게 수정합니다.
        comment.update(request.content(), request.writer());

        return debateCommentMapper.toDto(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long postId, Long commentId) {
        // 1. commentId로 댓글을 먼저 조회합니다.
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 조회한 댓글의 부모 게시글 ID와 URL로 넘어온 postId가 일치하는지 확인합니다.
        if (!comment.getDebatePost().getId().equals(postId)) {
            // 일치하지 않으면, 권한이 없거나 잘못된 요청으로 간주하고 예외를 발생시킵니다.
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        // 3. 검증이 통과된 경우에만 안전하게 삭제합니다.
        debateCommentRepository.delete(comment);
    }

    // TODO: 좋아요/싫어요 기능 - 나중에 구현 예정
    /*
    // 좋아요 토글 (증가/감소)
    public DebateCommentResponse toggleLike(Long postId, Long commentId) {
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getDebatePost().getId().equals(postId)) {
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        comment.incrementLikes();
        DebateComment savedComment = debateCommentRepository.save(comment);
        return debateCommentMapper.toDto(savedComment);
    }

    // 좋아요 취소 (감소)
    public DebateCommentResponse cancelLike(Long postId, Long commentId) {
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getDebatePost().getId().equals(postId)) {
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        comment.decrementLikes();
        DebateComment savedComment = debateCommentRepository.save(comment);
        return debateCommentMapper.toDto(savedComment);
    }

    // 싫어요 토글 (증가/감소)
    public DebateCommentResponse toggleDislike(Long postId, Long commentId) {
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getDebatePost().getId().equals(postId)) {
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        comment.incrementDislikes();
        DebateComment savedComment = debateCommentRepository.save(comment);
        return debateCommentMapper.toDto(savedComment);
    }

    // 싫어요 취소 (감소)
    public DebateCommentResponse cancelDislike(Long postId, Long commentId) {
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getDebatePost().getId().equals(postId)) {
            throw new IllegalArgumentException("게시글과 댓글의 정보가 일치하지 않습니다.");
        }

        comment.decrementDislikes();
        DebateComment savedComment = debateCommentRepository.save(comment);
        return debateCommentMapper.toDto(savedComment);
    }
    */
}
