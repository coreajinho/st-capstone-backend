package org.example.stcapstonebackend.findTeam;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.example.stcapstonebackend.findTeam.exception.FindTeamPostNotFoundException;
import org.example.stcapstonebackend.findTeam.mapper.FindTeamPostMapper;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.example.stcapstonebackend.findTeam.model.PostStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 팀 찾기 게시글 관리를 위한 서비스 클래스입니다.
 * 게시글의 생성, 조회, 수정, 삭제 및 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class FindTeamPostService {

    private final FindTeamPostRepository findTeamPostRepository;
    private final FindTeamPostMapper findTeamPostMapper;

    /**
     * 게시글 ID로 게시글 엔티티를 조회합니다.
     *
     * @param id 게시글 ID
     * @return 조회된 게시글 엔티티
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    private FindTeamPost getPostEntity(Long id) {
        return findTeamPostRepository.findById(id)
                .orElseThrow(() -> new FindTeamPostNotFoundException("Cannot find post with id: " + id));
    }

    /**
     * 새로운 팀 찾기 게시글을 생성합니다.
     *
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글 정보
     */
    @Transactional
    public FindTeamPostResponse createPost(FindTeamPostRequest request) {
        FindTeamPost post = findTeamPostMapper.toEntity(request);
        FindTeamPost savedPost = findTeamPostRepository.save(post);
        return findTeamPostMapper.toDto(savedPost);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param id 수정할 게시글의 ID
     * @param request 수정할 게시글 정보
     * @return 수정된 게시글 정보
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public FindTeamPostResponse updatePost(Long id, FindTeamPostRequest request) {
        FindTeamPost post = getPostEntity(id);
        post.update(request.title(), request.content(), request.writer(), request.tags());
        return findTeamPostMapper.toDto(post);
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param id 삭제할 게시글의 ID
     */
    @Transactional
    public void deletePost(Long id) {
        findTeamPostRepository.deleteById(id);
    }

    /**
     * ID로 특정 게시글을 조회합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 조회된 게시글 정보
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public FindTeamPostResponse getPost(Long id) {
        FindTeamPost post = getPostEntity(id);
        return findTeamPostMapper.toDto(post);
    }

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 전체 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getAllPosts() {
        return findTeamPostRepository.findAll().stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상태의 게시글만 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @return 활성 상태의 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getActivePosts() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글을 검색합니다.
     * 검색 타입에 따라 제목, 내용, 제목+내용, 작성자 중 선택하여 검색합니다.
     *
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> searchPosts(org.example.stcapstonebackend.findTeam.model.SearchType searchType, String keyword) {
        List<FindTeamPost> posts = switch (searchType) {
            case TITLE -> findTeamPostRepository.findByTitleContainingIgnoreCase(keyword);
            case CONTENT -> findTeamPostRepository.findByContentContainingIgnoreCase(keyword);
            case TITLE_CONTENT -> findTeamPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
            case WRITER -> findTeamPostRepository.findByWriterContainingIgnoreCase(keyword);
        };

        return posts.stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param username 사용자명 (로그인한 사용자)
     * @return 사용자가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getMyPosts(String username) {
        return findTeamPostRepository.findByWriterOrderByCreatedAtDesc(username).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }
}
