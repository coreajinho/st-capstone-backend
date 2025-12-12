package org.example.stcapstonebackend.debate.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebateCommentRequest;
import org.example.stcapstonebackend.debate.dto.DebateCommentResponse;
import org.example.stcapstonebackend.debate.dto.DebateCommentWithPostInfoResponse;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.stereotype.Component;

/**
 * 토론 댓글 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class DebateCommentMapper {

    private final UserRepository userRepository;

    /**
     * 요청 DTO를 엔티티로 변환합니다.
     * writerId로 User를 조회하여 writer 필드를 riotName#riotTag 형식으로 생성합니다.
     *
     * @param request 댓글 요청 DTO
     * @return 변환된 댓글 엔티티
     */
    public DebateComment toEntity(DebateCommentRequest request) {
        User writer = userRepository.findById(request.writerId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + request.writerId()));

        String writerDisplayName = writer.getRiotName() + "#" + writer.getRiotTag();

        return DebateComment.builder()
                .content(request.content())
                .writer(writerDisplayName)
                .writerId(request.writerId())
                .debateSide(request.debateSide())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환합니다.
     * writerId로 User를 조회하여 displayName을 생성합니다.
     *
     * @param debateComment 댓글 엔티티
     * @return 변환된 댓글 응답 DTO
     */
    public DebateCommentResponse toDto(DebateComment debateComment) {
        // writer displayName 생성
        User writer = userRepository.findById(debateComment.getWriterId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + debateComment.getWriterId()));
        String writerDisplayName = writer.getRiotName() + "#" + writer.getRiotTag();

        return new DebateCommentResponse(
                debateComment.getId(),
                debateComment.getContent(),
                writerDisplayName,
                debateComment.getWriterId(),
                debateComment.getDebateSide(),
                debateComment.getLikes(),
                debateComment.getDislikes(),
                debateComment.getCreatedAt(),
                debateComment.getModifiedAt()
        );
    }

    /**
     * 엔티티를 게시글 정보가 포함된 상세 응답 DTO로 변환합니다.
     * 댓글과 연관된 게시글의 writer, coWriter 정보를 함께 반환합니다.
     *
     * @param debateComment 댓글 엔티티 (debatePost가 fetch join되어 있어야 함)
     * @return 게시글 정보가 포함된 댓글 응답 DTO
     */
    public DebateCommentWithPostInfoResponse toDetailedDto(DebateComment debateComment) {
        // 댓글 작성자 displayName 생성
        User writer = userRepository.findById(debateComment.getWriterId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + debateComment.getWriterId()));
        String writerDisplayName = writer.getRiotName() + "#" + writer.getRiotTag();

        // 게시글 정보 가져오기
        var post = debateComment.getDebatePost();

        return DebateCommentWithPostInfoResponse.builder()
                .id(debateComment.getId())
                .content(debateComment.getContent())
                .writer(writerDisplayName)
                .writerId(debateComment.getWriterId())
                .debateSide(debateComment.getDebateSide())
                .likes(debateComment.getLikes())
                .dislikes(debateComment.getDislikes())
                .createdAt(debateComment.getCreatedAt())
                .modifiedAt(debateComment.getModifiedAt())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postWriter(post.getWriter())
                .postCoWriter(post.getCoWriter())
                .build();
    }
}
