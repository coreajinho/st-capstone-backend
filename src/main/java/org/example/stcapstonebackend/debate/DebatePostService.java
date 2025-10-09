package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DebatePostService {
    private final DebatePostRepository debatePostRepository;

    //--------------------내부 메소드------------------------------------------
    private DebatePost toPostEntity(DebatePostRequest postDto) {
        return DebatePost.builder()
                .title(postDto.title())
                .content(postDto.content())
                .writer(postDto.writer())
                .coWriter(postDto.coWriter())
                .build();
    }

    private DebatePostResponse toPostDto(DebatePost post) {
        return new DebatePostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getCoWriter(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }

    private DebatePost getPostEntity(Long id) throws Exception{
        return debatePostRepository.findById(id)
                .orElseThrow( () -> new DebatePostNotFoundException("해당id의 게시글이 없습니다. "));
    }

    //---------------------------------------------------------------------------- 서비스 메소드

    public DebatePostResponse createPost(DebatePostRequest postDto) {
        DebatePost post = toPostEntity(postDto);
        DebatePost savedPost = debatePostRepository.save(post);
        return toPostDto(savedPost);
    }

    public DebatePostResponse updatePost(DebatePostRequest postDto, Long id) throws Exception{
        DebatePost post = getPostEntity(id);
        post = post.toBuilder()
                .title(postDto.title())
                .content(postDto.content())
                .writer(postDto.writer())
                .coWriter(postDto.coWriter())
                .build();
        DebatePost savedPost = debatePostRepository.save(post);
        return toPostDto(savedPost);
    }

    public void deletePost(Long id){
        debatePostRepository.deleteById(id);
    }

    public DebatePostResponse getPost(Long id) throws Exception{
        DebatePost post = getPostEntity(id);
        return toPostDto(post);
    }
}
