package org.example.stcapstonebackend.debate.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 토론 게시글 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class DebatePostMapper {

    private final DebateCommentMapper debateCommentMapper;
    private final UserRepository userRepository;

    /**
     * 요청 DTO를 엔티티로 변환합니다.
     * writerId와 coWriterId로 User를 조회하여 writer/coWriter 필드를 riotName#riotTag 형식으로 생성합니다.
     *
     * @param request 게시글 요청 DTO
     * @return 변환된 게시글 엔티티
     */
    public DebatePost toEntity(DebatePostRequest request) {
        User writer = userRepository.findById(request.writerId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + request.writerId()));

        String writerDisplayName = writer.getRiotName() + "#" + writer.getRiotTag();
        String coWriterDisplayName = null;

        if (request.coWriterId() != null) {
            User coWriter = userRepository.findById(request.coWriterId())
                    .orElseThrow(() -> new UserNotFoundException("공동 작성자를 찾을 수 없습니다. ID: " + request.coWriterId()));
            coWriterDisplayName = coWriter.getRiotName() + "#" + coWriter.getRiotTag();
        }

        return DebatePost.builder()
                .title(request.title())
                .content(request.content())
                .writer(writerDisplayName)
                .writerId(request.writerId())
                .coWriter(coWriterDisplayName)
                .coWriterId(request.coWriterId())
                .videoUrl(request.videoUrl())
                .tags(request.tags())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환합니다.
     * writerId와 coWriterId로 User를 조회하여 displayName을 생성합니다.
     *
     * @param post 게시글 엔티티
     * @return 변환된 게시글 응답 DTO
     */
    public DebatePostResponse toDto(DebatePost post) {
        // writer displayName 생성
        User writer = userRepository.findById(post.getWriterId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + post.getWriterId()));
        String writerDisplayName = writer.getRiotName() + "#" + writer.getRiotTag();

        // coWriter displayName 생성 (선택사항)
        String coWriterDisplayName = null;
        if (post.getCoWriterId() != null) {
            User coWriter = userRepository.findById(post.getCoWriterId())
                    .orElseThrow(() -> new UserNotFoundException("공동 작성자를 찾을 수 없습니다. ID: " + post.getCoWriterId()));
            coWriterDisplayName = coWriter.getRiotName() + "#" + coWriter.getRiotTag();
        }

        return new DebatePostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                writerDisplayName,
                post.getWriterId(),
                coWriterDisplayName,
                post.getCoWriterId(),
                post.getVideoUrl(),
                post.getViews(),
                post.getComments().size(),
                post.getTags(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.getComments().stream()
                        .map(debateCommentMapper::toDto)
                        .collect(Collectors.toList())
        );
    }
}