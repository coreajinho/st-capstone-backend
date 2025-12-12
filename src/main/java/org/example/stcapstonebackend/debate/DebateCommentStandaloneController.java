package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebateCommentWithPostInfoResponse;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 토론 댓글(투표) 관련 독립적인 REST API를 제공하는 컨트롤러입니다.
 * 특정 게시글에 종속되지 않은 댓글 관련 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/debate/comments")
@RequiredArgsConstructor
public class DebateCommentStandaloneController {

    private final DebateCommentService debateCommentService;
    private final UserRepository userRepository;

    /**
     * 로그인한 사용자가 작성한 댓글(투표) 목록을 조회합니다.
     * 게시글 정보가 포함되어 있어 투표한 플레이어 이름을 확인할 수 있습니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 게시글 정보가 포함된 사용자가 작성한 댓글 목록
     */
    @GetMapping("/my-votes")
    public ResponseEntity<List<DebateCommentWithPostInfoResponse>> getMyVotes(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<DebateCommentWithPostInfoResponse> comments = debateCommentService.getMyVotes(user.getId());
        return ResponseEntity.ok(comments);
    }
}

