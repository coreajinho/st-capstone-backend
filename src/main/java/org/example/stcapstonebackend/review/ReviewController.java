package org.example.stcapstonebackend.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.review.dto.ReviewCreateRequest;
import org.example.stcapstonebackend.review.dto.ReviewResponse;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 리뷰 관련 API 엔드포인트를 제공하는 Controller
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    /**
     * 리뷰 작성
     *
     * @param authentication 인증 정보
     * @param request 리뷰 작성 요청 DTO
     * @return 생성된 리뷰 응답 DTO
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
        Authentication authentication,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        // 인증된 사용자의 username 추출
        String username = authentication.getName();

        // username으로 User 조회
        User reviewer = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));

        // 리뷰 생성
        ReviewResponse response = reviewService.createReview(reviewer.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }
}

