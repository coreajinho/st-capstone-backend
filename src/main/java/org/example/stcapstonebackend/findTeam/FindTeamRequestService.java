package org.example.stcapstonebackend.findTeam;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.findTeam.dto.FindTeamRequestRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamRequestResponse;
import org.example.stcapstonebackend.findTeam.exception.DuplicateAcceptanceException;
import org.example.stcapstonebackend.findTeam.exception.FindTeamPostNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.FindTeamRequestNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.InvalidTagSelectionException;
import org.example.stcapstonebackend.findTeam.mapper.FindTeamRequestMapper;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.example.stcapstonebackend.findTeam.model.FindTeamRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 팀 찾기 신청 요청 관리를 위한 서비스 클래스입니다.
 * 신청 요청의 생성, 조회, 수정, 삭제 및 수락/취소 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FindTeamRequestService {

    private final FindTeamPostRepository findTeamPostRepository;
    private final FindTeamRequestRepository findTeamRequestRepository;
    private final FindTeamRequestMapper findTeamRequestMapper;

    /**
     * 특정 게시글에 새로운 신청 요청을 생성합니다.
     * 선택한 태그가 게시글에 존재하고 아직 수락되지 않은 태그여야 합니다.
     *
     * @param postId 게시글 ID
     * @param request 신청 요청 정보
     * @return 생성된 신청 요청 정보
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws InvalidTagSelectionException 선택한 태그가 유효하지 않거나 이미 수락된 경우
     */
    public FindTeamRequestResponse createRequest(Long postId, FindTeamRequestRequest request) {
        FindTeamPost post = findTeamPostRepository.findById(postId)
                .orElseThrow(() -> new FindTeamPostNotFoundException("Cannot find post with id: " + postId));

        if (!post.getTags().contains(request.desiredTag())) {
            throw new InvalidTagSelectionException("Selected tag is not available for this post");
        }

        if (post.isTagAccepted(request.desiredTag())) {
            throw new InvalidTagSelectionException("This tag is already accepted");
        }

        FindTeamRequest findTeamRequest = FindTeamRequest.builder()
                .content(request.content())
                .writer(request.writer())
                .writerId(request.writerId())
                .desiredTag(request.desiredTag())
                .build();

        post.addRequest(findTeamRequest);
        FindTeamRequest savedRequest = findTeamRequestRepository.save(findTeamRequest);

        return findTeamRequestMapper.toDto(savedRequest);
    }

    /**
     * 특정 게시글의 모든 신청 요청을 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 신청 요청 목록
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public List<FindTeamRequestResponse> getRequestsByPostId(Long postId) {
        if (!findTeamPostRepository.existsById(postId)) {
            throw new FindTeamPostNotFoundException("Cannot find post with id: " + postId);
        }

        return findTeamRequestRepository.findByFindTeamPostId(postId).stream()
                .map(findTeamRequestMapper::toDto)
                .toList();
    }

    /**
     * 기존 신청 요청을 수정합니다.
     * 신청 요청이 해당 게시글에 속해있어야 하며, 수정하려는 태그가 유효해야 합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 수정할 신청 요청 ID
     * @param request 수정할 신청 요청 정보
     * @return 수정된 신청 요청 정보
     * @throws FindTeamRequestNotFoundException 신청 요청을 찾을 수 없는 경우
     * @throws IllegalArgumentException 게시글 ID가 일치하지 않는 경우
     * @throws InvalidTagSelectionException 선택한 태그가 유효하지 않거나 이미 수락된 경우
     */
    public FindTeamRequestResponse updateRequest(Long postId, Long requestId, FindTeamRequestRequest request) {
        FindTeamRequest findTeamRequest = findTeamRequestRepository.findById(requestId)
                .orElseThrow(() -> new FindTeamRequestNotFoundException("Cannot find request with id: " + requestId));

        if (!findTeamRequest.getFindTeamPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Post ID mismatch");
        }

        FindTeamPost post = findTeamRequest.getFindTeamPost();

        if (!post.getTags().contains(request.desiredTag())) {
            throw new InvalidTagSelectionException("Selected tag is not available for this post");
        }

        if (!findTeamRequest.getDesiredTag().equals(request.desiredTag())
                && post.isTagAccepted(request.desiredTag())) {
            throw new InvalidTagSelectionException("This tag is already accepted");
        }

        findTeamRequest.update(request.content(), request.writer(), request.writerId(), request.desiredTag());

        return findTeamRequestMapper.toDto(findTeamRequest);
    }

    /**
     * 신청 요청을 삭제합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 삭제할 신청 요청 ID
     * @throws FindTeamRequestNotFoundException 신청 요청을 찾을 수 없는 경우
     * @throws IllegalArgumentException 게시글 ID가 일치하지 않는 경우
     */
    public void deleteRequest(Long postId, Long requestId) {
        FindTeamRequest request = findTeamRequestRepository.findById(requestId)
                .orElseThrow(() -> new FindTeamRequestNotFoundException("Cannot find request with id: " + requestId));

        if (!request.getFindTeamPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Post ID mismatch");
        }

        findTeamRequestRepository.delete(request);
    }

    /**
     * 신청 요청의 수락 상태를 토글합니다.
     * 수락된 상태라면 취소하고, 취소된 상태라면 수락합니다.
     * 수락 시 동일한 태그가 이미 수락되어 있지 않은지 확인합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 수락/취소할 신청 요청 ID
     * @return 변경된 신청 요청 정보
     * @throws FindTeamRequestNotFoundException 신청 요청을 찾을 수 없는 경우
     * @throws IllegalArgumentException 게시글 ID가 일치하지 않는 경우
     * @throws DuplicateAcceptanceException 동일한 태그가 이미 다른 요청에서 수락된 경우
     */
    public FindTeamRequestResponse toggleAcceptance(Long postId, Long requestId) {
        FindTeamRequest request = findTeamRequestRepository.findById(requestId)
                .orElseThrow(() -> new FindTeamRequestNotFoundException("Cannot find request with id: " + requestId));

        if (!request.getFindTeamPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Post ID mismatch");
        }

        FindTeamPost post = request.getFindTeamPost();

        if (request.getIsAccepted()) {
            request.cancelAccept();
            post.cancelAcceptance(requestId);
        } else {
            if (post.isTagAccepted(request.getDesiredTag())) {
                throw new DuplicateAcceptanceException("This tag is already accepted by another request");
            }

            request.accept();
            post.acceptRequest(requestId, request.getDesiredTag());
        }

        return findTeamRequestMapper.toDto(request);
    }

    /**
     * 로그인한 사용자가 작성한 모든 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param username 사용자명 (로그인한 사용자)
     * @return 사용자가 작성한 신청 요청 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamRequestResponse> getMyRequests(String username) {
        return findTeamRequestRepository.findByWriterOrderByCreatedAtDesc(username).stream()
                .map(findTeamRequestMapper::toDto)
                .toList();
    }

    /**
     * 로그인한 사용자가 작성한 수락된 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param username 사용자명 (로그인한 사용자)
     * @return 사용자가 작성한 수락된 신청 요청 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamRequestResponse> getMyAcceptedRequests(String username) {
        return findTeamRequestRepository.findByWriterAndIsAcceptedOrderByCreatedAtDesc(username, true).stream()
                .map(findTeamRequestMapper::toDto)
                .toList();
    }

    /**
     * 로그인한 사용자가 작성한 수락되지 않은 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param username 사용자명 (로그인한 사용자)
     * @return 사용자가 작성한 수락되지 않은 신청 요청 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamRequestResponse> getMyPendingRequests(String username) {
        return findTeamRequestRepository.findByWriterAndIsAcceptedOrderByCreatedAtDesc(username, false).stream()
                .map(findTeamRequestMapper::toDto)
                .toList();
    }
}
