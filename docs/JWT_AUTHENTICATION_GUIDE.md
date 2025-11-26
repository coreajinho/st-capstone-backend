# JWT 인증 시스템 구현 가이드

## 📋 개요
Spring Boot 3.5.6 + Servlet 기반 Spring Security JWT 인증 시스템이 성공적으로 구현되었습니다.
- **인증 시스템**: Servlet 기반 (동기/Blocking 방식)
- **외부 API 호출**: WebFlux 기반 (비동기/Non-blocking 방식)

## 🔧 추가된 의존성

### build.gradle
```gradle
// Spring Security
implementation 'org.springframework.boot:spring-boot-starter-security'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

### application.properties
```properties
# JWT Configuration
jwt.secret=yourSecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm
jwt.expiration=86400000
```
⚠️ **중요**: 프로덕션 환경에서는 `jwt.secret`을 환경 변수로 관리하세요!

## 🏗️ 아키텍처 개요

### Servlet + WebFlux 하이브리드
이 프로젝트는 두 가지 처리 방식을 혼용합니다:

| 영역 | 방식 | 이유 |
|------|------|------|
| 인증/인가 (User 도메인) | Servlet (동기) | JPA와의 호환성, 안정성 |
| 외부 API 호출 (Riot API) | WebFlux (비동기) | 높은 처리량, 효율적 리소스 사용 |

### 의존성 구성
```gradle
// Servlet 기반 MVC (인증 시스템용)
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-security'

// WebFlux (외부 API 호출용, 전체 Reactive Stack은 아님)
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

## 📁 프로젝트 구조

```
org.example.stcapstonebackend.user/
├── model/
│   ├── User.java              # 사용자 엔티티
│   └── Role.java              # 권한 enum (USER, ADMIN)
├── dto/
│   ├── UserSignUpRequest.java # 회원가입 요청 DTO
│   ├── UserLoginRequest.java  # 로그인 요청 DTO
│   ├── TokenResponse.java     # 토큰 응답 DTO
│   └── UserResponse.java      # 사용자 정보 응답 DTO
├── exception/
│   ├── DuplicateEmailException.java
│   ├── InvalidCredentialsException.java
│   └── UserNotFoundException.java
├── UserRepository.java        # JPA Repository
├── UserService.java           # 비즈니스 로직
└── AuthController.java        # REST API 컨트롤러

org.example.stcapstonebackend.common.security/
├── SecurityConfig.java           # Spring Security 설정 (Servlet 기반)
├── JwtTokenProvider.java         # JWT 토큰 생성/검증
└── JwtAuthenticationFilter.java  # JWT 인증 필터 (OncePerRequestFilter)
```

## 🔐 API 엔드포인트

### 1. 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자닉네임"
}
```

**응답 (201 Created)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "사용자닉네임",
  "role": "USER"
}
```

### 2. 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답 (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 3. 현재 사용자 정보 조회 (인증 필요)
```http
GET /api/auth/me
Authorization: Bearer {accessToken}
```

**응답 (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "사용자닉네임",
  "role": "USER"
}
```

## 🔒 보안 설정

### 인증이 필요 없는 경로
- `POST /api/auth/**` - 회원가입, 로그인
- `/summoner/**` - 소환사 조회
- `/api/debate/**` - 토론 게시판 (현재 공개)

### 인증이 필요한 경로
- 그 외 모든 경로는 JWT 토큰이 필요합니다.

### JWT 토큰 사용 방법
모든 인증이 필요한 요청에는 다음 헤더를 포함해야 합니다:
```
Authorization: Bearer {accessToken}
```

## 💾 데이터베이스 스키마

### users 테이블
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);
```

- Flyway 마이그레이션: `V2__create_users_table.sql`
- JPA `@Entity`로도 자동 생성 가능 (현재 `spring.jpa.hibernate.ddl-auto=update`)

## 🧪 테스트 방법

### Postman / Thunder Client 사용

1. **회원가입**
```bash
POST http://localhost:8080/api/auth/signup
{
  "email": "test@example.com",
  "password": "test1234",
  "nickname": "테스터"
}
```

2. **로그인**
```bash
POST http://localhost:8080/api/auth/login
{
  "email": "test@example.com",
  "password": "test1234"
}
```
→ 응답에서 `accessToken` 복사

3. **인증이 필요한 API 호출**
```bash
GET http://localhost:8080/api/auth/me
Authorization: Bearer {복사한_accessToken}
```

### cURL 사용
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test1234","nickname":"테스터"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test1234"}'

# 현재 사용자 정보
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {accessToken}"
```

## 🛠️ 주요 기능

### 1. 비밀번호 암호화
- `BCryptPasswordEncoder` 사용
- 단방향 해시로 안전하게 저장

### 2. JWT 토큰
- **알고리즘**: HS256
- **만료 시간**: 24시간 (86400초)
- **포함 정보**: email, role

### 3. Servlet 기반 동기 처리
- 일반 객체 반환 (Mono/Flux 제거)
- `@Transactional`로 트랜잭션 관리
- Servlet 컨테이너가 스레드 관리

### 4. Validation
- `@Valid`로 입력 검증
- 이메일 형식, 비밀번호 길이 등 자동 체크

### 5. 예외 처리
- `GlobalExceptionHandler`로 통합 관리
- 중복 이메일, 잘못된 인증 정보 등 처리

## 📌 주의사항

1. **JWT Secret Key**
   - 현재는 application.properties에 하드코딩
   - 프로덕션에서는 환경 변수 사용 권장
   ```bash
   export JWT_SECRET=your-very-long-secret-key
   ```

2. **Token 만료**
   - 현재 24시간 설정
   - Refresh Token은 구현되지 않음
   - 필요시 추가 구현 가능

3. **CORS 설정**
   - 프론트엔드와 연동 시 CORS 설정 필요
   - `SecurityConfig`에서 추가 가능

4. **Role 기반 권한**
   - 현재 USER, ADMIN 구분만 구현
   - 세밀한 권한 제어는 `@PreAuthorize` 사용

5. **WebFlux 의존성**
   - `spring-boot-starter-webflux`는 WebClient(외부 API 호출)용
   - 인증 시스템은 Servlet 기반이므로 `Mono`/`Flux` 미사용
   - 두 가지 스택이 공존 가능 (Spring Boot 3.x)

## 🚀 다음 단계 (선택사항)

1. **Refresh Token 구현**
   - Access Token 갱신 기능
   - Redis로 Refresh Token 저장

2. **소셜 로그인**
   - OAuth2 (Google, Kakao 등)
   - Spring Security OAuth2 Client

3. **이메일 인증**
   - 회원가입 시 이메일 확인
   - JavaMailSender 사용

4. **비밀번호 찾기/변경**
   - 이메일로 임시 비밀번호 발송
   - 비밀번호 재설정 API

5. **사용자 프로필 관리**
   - 닉네임 변경
   - 프로필 이미지 업로드

## 🐛 트러블슈팅

### 1. 401 Unauthorized
- JWT 토큰이 없거나 만료됨
- Authorization 헤더 확인

### 2. 403 Forbidden
- 권한이 부족함
- Role 확인

### 3. 409 Conflict (회원가입)
- 이미 존재하는 이메일
- 다른 이메일 사용

## 📚 참고 자료

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)

