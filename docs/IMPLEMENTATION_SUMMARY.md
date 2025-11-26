# JWT 기반 인증 시스템 구현 완료

## ✅ 구현 완료 항목

### 1. Dependencies (build.gradle)
- ✅ Spring Security (`spring-boot-starter-security`)
- ✅ JWT 라이브러리 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.3)

### 2. Domain Model (`org.example.stcapstonebackend.user.model`)
- ✅ `User` Entity
  - `id` (PK, BIGINT AUTO_INCREMENT)
  - `email` (Unique, VARCHAR(100))
  - `password` (암호화 저장, VARCHAR(255))
  - `nickname` (VARCHAR(50))
  - `role` (Enum: USER, ADMIN)
  - `createdAt`, `modifiedAt` (자동 생성, BaseEntity 상속)
- ✅ `Role` Enum

### 3. Repository
- ✅ `UserRepository` (Spring Data JPA)
  - `findByEmail(String email)`
  - `existsByEmail(String email)`

### 4. DTOs (`org.example.stcapstonebackend.user.dto`)
- ✅ `UserSignUpRequest` (Record)
  - email, password, nickname
  - Bean Validation 적용
- ✅ `UserLoginRequest` (Record)
  - email, password
- ✅ `TokenResponse` (Record)
  - accessToken, tokenType, expiresIn
- ✅ `UserResponse` (Record)
  - id, email, nickname, role

### 5. Security Configuration (`org.example.stcapstonebackend.common.security`)
- ✅ `SecurityConfig`
  - **Servlet 기반 Spring Security** 설정 (`@EnableWebSecurity`)
  - CSRF disable (Stateless JWT)
  - Session Management: Stateless
  - PasswordEncoder (BCrypt)
  - `SecurityFilterChain` 구성
  - Path 권한 설정:
    - `POST /api/auth/**` → permitAll
    - `/summoner/**` → permitAll
    - `/api/debate/**` → permitAll
    - 나머지 → authenticated
- ✅ `JwtTokenProvider`
  - 토큰 생성 (`createToken`)
  - 토큰 검증 (`validateToken`)
  - 클레임 추출 (`getEmailFromToken`, `getRoleFromToken`)
  - Secret Key 및 만료시간 설정
- ✅ `JwtAuthenticationFilter`
  - `OncePerRequestFilter` 상속
  - Authorization 헤더에서 JWT 추출 및 검증
  - Bearer 토큰 파싱
  - `SecurityContext`에 인증 정보 설정

### 6. Business Logic (`org.example.stcapstonebackend.user`)
- ✅ `UserService`
  - `signUp`: 회원가입 (비밀번호 암호화, 중복 체크)
  - `login`: 로그인 (인증 후 JWT 발급)
  - `getUserByEmail`: 사용자 조회
  - **동기(Blocking) 방식** - 일반 객체 반환 (Mono/Flux 제거)
  - `@Transactional` 적용
- ✅ `AuthController`
  - `POST /api/auth/signup` - 회원가입
  - `POST /api/auth/login` - 로그인
  - `GET /api/auth/me` - 현재 사용자 정보 (인증 필요)
  - `ResponseEntity<T>` 반환 (동기 방식)

### 7. Exception Handling
- ✅ Custom Exceptions
  - `DuplicateEmailException` (409 Conflict)
  - `InvalidCredentialsException` (401 Unauthorized)
  - `UserNotFoundException` (404 Not Found)
- ✅ `GlobalExceptionHandler` 업데이트
  - 사용자 관련 예외 처리 추가

### 8. Database
- ✅ Flyway Migration Script
  - `V2__create_users_table.sql`
- ✅ JPA Entity Auditing 활성화 (이미 설정됨)

### 9. Configuration
- ✅ `application.properties`
  - `jwt.secret` 설정
  - `jwt.expiration` 설정 (86400000ms = 24시간)

## 📝 구현된 파일 목록

### Java 파일 (15개)
1. `User.java` - 사용자 엔티티
2. `Role.java` - 권한 Enum
3. `UserRepository.java` - JPA Repository
4. `UserSignUpRequest.java` - 회원가입 DTO
5. `UserLoginRequest.java` - 로그인 DTO
6. `TokenResponse.java` - 토큰 응답 DTO
7. `UserResponse.java` - 사용자 응답 DTO
8. `DuplicateEmailException.java` - 중복 이메일 예외
9. `InvalidCredentialsException.java` - 잘못된 인증정보 예외
10. `UserNotFoundException.java` - 사용자 없음 예외
11. `UserService.java` - 비즈니스 로직 (Servlet/동기 방식)
12. `AuthController.java` - REST API (Servlet/동기 방식)
13. `SecurityConfig.java` - Security 설정 (Servlet 기반)
14. `JwtTokenProvider.java` - JWT 유틸리티
15. `JwtAuthenticationFilter.java` - JWT 인증 필터 (OncePerRequestFilter)

### 설정 파일 (3개)
1. `build.gradle` - 의존성 추가
2. `application.properties` - JWT 설정 추가
3. `V2__create_users_table.sql` - DB 마이그레이션

### 문서 (1개)
1. `JWT_AUTHENTICATION_GUIDE.md` - 완전한 사용 가이드

## 🚀 사용 방법

### 1. 프로젝트 빌드 및 실행
```bash
.\gradlew clean build
.\gradlew bootRun
```

### 2. API 테스트
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test1234","nickname":"테스터"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test1234"}'

# 사용자 정보 조회 (토큰 필요)
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {accessToken}"
```

## 🎯 주요 특징

1. **Spring Boot 3.5.6 + Java 21** 최신 버전 사용
2. **Servlet 기반 Spring Security** (동기/Blocking 방식)
3. **JWT 토큰 기반** Stateless 인증
4. **BCrypt** 비밀번호 암호화
5. **Bean Validation** 입력 검증
6. **Record DTO** Java 14+ Record 활용
7. **Exception Handling** 통합 예외 처리
8. **JPA Auditing** 자동 생성/수정 시간 관리
9. **WebFlux(Reactive) 혼용** - 외부 API 호출은 WebClient 사용 유지

## 🏗️ 아키텍처 설계

### Servlet vs WebFlux 역할 분담
이 프로젝트는 **Servlet(동기)** 와 **WebFlux(비동기)** 를 혼용하는 하이브리드 아키텍처를 채택했습니다.

#### Servlet 기반 (동기/Blocking)
- **인증/인가 시스템** (Spring Security + JWT)
  - 회원가입/로그인 (`AuthController`, `UserService`)
  - JWT 필터 (`JwtAuthenticationFilter`)
  - Security 설정 (`SecurityConfig`)
- **Database 작업** (Spring Data JPA)
  - Repository 레이어는 기본적으로 Blocking

#### WebFlux 기반 (비동기/Non-blocking)
- **외부 API 호출** (WebClient)
  - Riot Games API 호출 (`RiotApiClient`)
  - 높은 처리량이 필요한 I/O 작업

> **Why?** JPA는 Blocking이므로, 인증 시스템을 Servlet 기반으로 구현하는 것이 더 자연스럽고 안정적입니다. 외부 API 호출만 WebClient(비동기)를 사용하여 효율적인 리소스 사용을 유지합니다.

## ⚠️ 주의사항

1. **JWT Secret Key**: 프로덕션 환경에서는 환경 변수로 관리 필요
2. **Token 만료**: 현재 24시간, Refresh Token 미구현
3. **CORS**: 프론트엔드 연동 시 추가 설정 필요
4. **Database**: MySQL 사용 (포트 3308)
5. **WebFlux 의존성**: WebClient 사용을 위해 `spring-boot-starter-webflux`가 포함되어 있으나, 전체 애플리케이션이 Reactive Stack은 아님

## 📚 참고 문서

- 자세한 사용 가이드: `docs/JWT_AUTHENTICATION_GUIDE.md`
- API 스펙, 테스트 방법, 트러블슈팅 포함

## ✨ 다음 단계 (Optional)

- [ ] Refresh Token 구현
- [ ] 소셜 로그인 (OAuth2)
- [ ] 이메일 인증
- [ ] 비밀번호 찾기/변경
- [ ] 사용자 프로필 관리
- [ ] Admin 전용 API
- [ ] Rate Limiting
- [ ] 로그인 이력 추적

---

**구현 완료일**: 2025-11-23
**기술 스택**: Spring Boot 3.5.6, Spring Security (Servlet), JWT, JPA, MySQL
**아키텍처**: Servlet(동기) + WebFlux(비동기) 혼용 - 인증은 Servlet, 외부 API는 WebFlux

