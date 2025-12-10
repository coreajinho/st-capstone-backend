package org.example.stcapstonebackend.findTeam.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 팀 찾기 게시글 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class FindTeamPostMapper {

    private final FindTeamRequestMapper findTeamRequestMapper;

    /**
     * 요청 DTO를 엔티티로 변환합니다.
     *
     * @param request 게시글 요청 DTO
     * @return 변환된 게시글 엔티티
     */
    public FindTeamPost toEntity(FindTeamPostRequest request) {
        return FindTeamPost.builder()
                .title(request.title())
                .content(request.content())
                .writer(request.writer())
                .writerId(request.writerId())
                .tags(request.tags())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환합니다.
     *
     * @param post 게시글 엔티티
     * @return 변환된 게시글 응답 DTO
     */
    public FindTeamPostResponse toDto(FindTeamPost post) {
        return new FindTeamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getWriterId(),
                post.getTags(),
                post.getAcceptedTags(),
                post.getAvailableTags(),
                post.getStatus(),
                post.getPendingExpirationAt(),
                post.getRequests().size(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.getRequests().stream()
                        .map(findTeamRequestMapper::toDto)
                        .collect(Collectors.toList())
        );
    }
}
