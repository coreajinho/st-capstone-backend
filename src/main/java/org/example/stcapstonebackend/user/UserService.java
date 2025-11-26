package org.example.stcapstonebackend.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.security.JwtTokenProvider;
import org.example.stcapstonebackend.user.dto.TokenResponse;
import org.example.stcapstonebackend.user.dto.UserLoginRequest;
import org.example.stcapstonebackend.user.dto.UserResponse;
import org.example.stcapstonebackend.user.dto.UserSignUpRequest;
import org.example.stcapstonebackend.user.exception.DuplicateEmailException;
import org.example.stcapstonebackend.user.exception.InvalidCredentialsException;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.Role;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signUp(UserSignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + request.email());
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getRole()
        );
    }

    @Transactional(readOnly = true)
    public TokenResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + request.email()));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        log.info("User logged in: {}", user.getEmail());

        return new TokenResponse(token, jwtTokenProvider.getValidityInMilliseconds() / 1000);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}

