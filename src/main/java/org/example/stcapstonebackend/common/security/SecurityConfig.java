package org.example.stcapstonebackend.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Security Filter Chain 설정
     * WebMvcConfig의 CORS 설정을 활용하고, preflight 요청(OPTIONS)을 허용합니다.
     * ADMIN 역할을 가진 사용자는 모든 경로에 접근할 수 있습니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // WebMvcConfig의 CORS 설정을 사용
                .cors(cors -> {})
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // preflight 요청(OPTIONS)을 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // === User/Auth 경로 ===
                        // 회원가입, 로그인, CoWriter 검증은 누구나 접근 가능
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/validate-cowriter").permitAll()
                        // 내 정보 조회는 인증 필수
                        .requestMatchers("/api/auth/me").authenticated()

                        // === Summoner 경로 ===
                        // 소환사 관련 모든 API는 누구나 접근 가능
                        .requestMatchers("/api/summoner/**").permitAll()

                        // === Debate 경로 ===
                        // 내 게시글/투표 조회는 인증 필수
                        .requestMatchers("/api/debate/posts/my-posts", "/api/debate/comments/my-votes").authenticated()
                        // 게시글 및 댓글 생성, 수정, 삭제는 인증 필수
                        .requestMatchers(HttpMethod.POST, "/api/debate/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/debate/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/debate/**").authenticated()
                        // 게시글 및 댓글 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/debate/**").permitAll()

                        // === FindTeam 경로 ===
                        // 내 게시글 및 요청 조회는 인증 필수
                        .requestMatchers("/api/find-team/posts/my-posts", "/api/find-team/requests/my-requests/**").authenticated()
                        // 게시글 및 요청 생성, 수정, 삭제는 인증 필수
                        .requestMatchers(HttpMethod.POST, "/api/find-team/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/find-team/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/find-team/**").authenticated()
                        // 게시글 및 요청 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/find-team/**").permitAll()

                        // === Review 경로 ===
                        // 리뷰 생성은 인증 필수
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                        // 리뷰 조회는 누구나 가능
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()

                        // === ADMIN 권한 ===
                        // ADMIN 역할을 가진 사용자는 모든 경로에 접근 가능
                        .requestMatchers("/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필수
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

