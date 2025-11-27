package org.example.stcapstonebackend.debate.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DebatePostMapper {

    private final DebateCommentMapper debateCommentMapper;

    public DebatePost toEntity(DebatePostRequest request) {
        return DebatePost.builder()
                .title(request.title())
                .content(request.content())
                .writer(request.writer())
                .coWriter(request.coWriter())
                .videoUrl(request.videoUrl())
                .tags(request.tags())
                .build();
    }

    public DebatePostResponse toDto(DebatePost post) {
        return new DebatePostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getCoWriter(),
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