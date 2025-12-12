package org.example.stcapstonebackend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.user.dto.CoWriterValidationRequest;
import org.example.stcapstonebackend.user.dto.CoWriterValidationResponse;
import org.example.stcapstonebackend.user.dto.TokenResponse;
import org.example.stcapstonebackend.user.dto.UserLoginRequest;
import org.example.stcapstonebackend.user.dto.UserResponse;
import org.example.stcapstonebackend.user.dto.UserSignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        UserResponse userResponse = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody UserLoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * CoWriter 검증 API
     * 토론 게시글 작성 시 CoWriter로 지정할 사용자가 회원인지 확인합니다.
     *
     * @param request CoWriter 검증 요청 (riotName, riotTag)
     * @return CoWriter 검증 응답 (회원 여부, 사용자 정보)
     */
    @PostMapping("/validate-cowriter")
    public ResponseEntity<CoWriterValidationResponse> validateCoWriter(
            @Valid @RequestBody CoWriterValidationRequest request) {
        CoWriterValidationResponse response =
                userService.validateCoWriter(request.riotName(), request.riotTag());
        return ResponseEntity.ok(response);
    }
}

