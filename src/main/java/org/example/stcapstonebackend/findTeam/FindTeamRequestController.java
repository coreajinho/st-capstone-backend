package org.example.stcapstonebackend.findTeam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.findTeam.dto.FindTeamRequestRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamRequestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 팀 찾기 게시글에 대한 신청 요청 관련 REST API를 제공하는 컨트롤러입니다.
 * 신청 요청의 생성, 조회, 수정, 삭제 및 수락/취소 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
public class FindTeamRequestController {

    private final FindTeamRequestService findTeamRequestService;

    /**
     * 특정 게시글에 새로운 신청 요청을 생성합니다.
     *
     * @param postId 게시글 ID
     * @param request 신청 요청 데이터
     * @return 생성된 신청 요청 정보
     */
    @PostMapping("/api/find-team/posts/{postId}/requests")
    public ResponseEntity<FindTeamRequestResponse> createRequest(
            @PathVariable Long postId,
            @Valid @RequestBody FindTeamRequestRequest request
    ) {
        FindTeamRequestResponse response = findTeamRequestService.createRequest(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 게시글의 모든 신청 요청을 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 신청 요청 목록
     */
    @GetMapping("/api/find-team/posts/{postId}/requests")
    public ResponseEntity<List<FindTeamRequestResponse>> getRequestsByPostId(@PathVariable Long postId) {
        List<FindTeamRequestResponse> responses = findTeamRequestService.getRequestsByPostId(postId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 기존 신청 요청을 수정합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 수정할 신청 요청 ID
     * @param request 수정할 신청 요청 데이터
     * @return 수정된 신청 요청 정보
     */
    @PutMapping("/api/find-team/posts/{postId}/requests/{requestId}")
    public ResponseEntity<FindTeamRequestResponse> updateRequest(
            @PathVariable Long postId,
            @PathVariable Long requestId,
            @Valid @RequestBody FindTeamRequestRequest request
    ) {
        FindTeamRequestResponse response = findTeamRequestService.updateRequest(postId, requestId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 신청 요청을 삭제합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 삭제할 신청 요청 ID
     * @return 204 No Content 응답
     */
    @DeleteMapping("/api/find-team/posts/{postId}/requests/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long postId,
            @PathVariable Long requestId
    ) {
        findTeamRequestService.deleteRequest(postId, requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 신청 요청의 수락 상태를 토글합니다.
     * 수락된 상태라면 취소하고, 취소된 상태라면 수락합니다.
     *
     * @param postId 게시글 ID
     * @param requestId 수락/취소할 신청 요청 ID
     * @return 변경된 신청 요청 정보
     */
    @PostMapping("/api/find-team/posts/{postId}/requests/{requestId}/toggle-accept")
    public ResponseEntity<FindTeamRequestResponse> toggleAcceptance(
            @PathVariable Long postId,
            @PathVariable Long requestId
    ) {
        FindTeamRequestResponse response = findTeamRequestService.toggleAcceptance(postId, requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인한 사용자가 작성한 모든 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자가 작성한 신청 요청 목록
     */
    @GetMapping("/api/find-team/requests/my-requests")
    public ResponseEntity<List<FindTeamRequestResponse>> getMyRequests(Authentication authentication) {
        String username = authentication.getName();
        List<FindTeamRequestResponse> responses = findTeamRequestService.getMyRequests(username);
        return ResponseEntity.ok(responses);
    }

    /**
     * 로그인한 사용자가 작성한 수락된 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자가 작성한 수락된 신청 요청 목록
     */
    @GetMapping("/api/find-team/requests/my-requests/accepted")
    public ResponseEntity<List<FindTeamRequestResponse>> getMyAcceptedRequests(Authentication authentication) {
        String username = authentication.getName();
        List<FindTeamRequestResponse> responses = findTeamRequestService.getMyAcceptedRequests(username);
        return ResponseEntity.ok(responses);
    }

    /**
     * 로그인한 사용자가 작성한 수락되지 않은 신청 요청을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자가 작성한 수락되지 않은 신청 요청 목록
     */
    @GetMapping("/api/find-team/requests/my-requests/pending")
    public ResponseEntity<List<FindTeamRequestResponse>> getMyPendingRequests(Authentication authentication) {
        String username = authentication.getName();
        List<FindTeamRequestResponse> responses = findTeamRequestService.getMyPendingRequests(username);
        return ResponseEntity.ok(responses);
    }
}
