# 접근 권한 설정 구현 완료

## 개요
Spring Security를 사용하여 서비스의 접근 권한을 세분화하여 설정했습니다.

## 구현 내용

### 1. SecurityConfig 업데이트
경로별로 세분화된 접근 권한을 설정했습니다.

#### User/Auth 경로
- **누구나 접근 가능**: `/api/auth/signup`, `/api/auth/login`, `/api/auth/validate-cowriter`
- **인증 필수**: `/api/auth/me`

#### Summoner 경로
- **누구나 접근 가능**: `/api/summoner/**` (모든 API)

#### Debate 경로
- **누구나 접근 가능**: GET 요청 (게시글 및 댓글 조회, 검색, 인기글 등)
- **인증 필수**: 
  - POST, PUT, DELETE 요청 (생성, 수정, 삭제)
  - `/api/debate/posts/my-posts` (내 게시글 조회)
  - `/api/debate/comments/my-votes` (내 투표 조회)

#### FindTeam 경로
- **누구나 접근 가능**: GET 요청 (게시글 및 요청 조회)
- **인증 필수**:
  - POST, PUT, DELETE 요청 (생성, 수정, 삭제)
  - `/api/find-team/posts/my-posts` (내 게시글 조회)
  - `/api/find-team/requests/my-requests/**` (내 요청 조회)

#### Review 경로
- **누구나 접근 가능**: GET 요청 (리뷰 조회)
- **인증 필수**: POST 요청 (리뷰 생성)

### 2. 작성자 검증 로직 추가

#### 새로운 예외 클래스
- `UnauthorizedAccessException`: 작성자가 아닌 사용자가 수정/삭제를 시도할 때 발생

#### Service 레이어 수정

##### DebatePostService
- `updatePost(DebatePostRequest, Long id, Long userId)`: 작성자 또는 공동 작성자만 수정 가능
- `deletePost(Long id, Long userId)`: 작성자 또는 공동 작성자만 삭제 가능
- `isAuthorOrCoAuthor(DebatePost, Long userId)`: 작성자 검증 헬퍼 메서드

##### DebateCommentService
- `updateComment(Long postId, Long commentId, DebateCommentRequest, Long userId)`: 작성자만 수정 가능
- `deleteComment(Long postId, Long commentId, Long userId)`: 작성자만 삭제 가능

##### FindTeamPostService
- `updatePost(Long id, FindTeamPostRequest, Long userId)`: 작성자만 수정 가능
- `deletePost(Long id, Long userId)`: 작성자만 삭제 가능

##### FindTeamRequestService
- `updateRequest(Long postId, Long requestId, FindTeamRequestRequest, Long userId)`: 작성자만 수정 가능
- `deleteRequest(Long postId, Long requestId, Long userId)`: 작성자만 삭제 가능

#### Controller 레이어 수정
모든 수정/삭제 엔드포인트에 `Authentication` 파라미터를 추가하고, 인증된 사용자의 ID를 Service 레이어로 전달하도록 수정했습니다.

- `DebatePostController`: updatePost, deletePost
- `DebateCommentController`: updateComment, deleteComment
- `FindTeamPostController`: updatePost, deletePost
- `FindTeamRequestController`: updateRequest, deleteRequest

### 3. 보안 흐름

1. **요청 수신**: 클라이언트가 JWT 토큰과 함께 요청 전송
2. **인증 확인**: `JwtAuthenticationFilter`가 토큰 검증 및 인증 정보 설정
3. **권한 확인**: `SecurityFilterChain`이 경로와 HTTP 메서드에 따라 접근 권한 확인
4. **Controller**: 인증된 사용자 정보(`Authentication`)를 받아서 사용자 ID 추출
5. **Service**: 데이터베이스에서 리소스 조회 후 작성자 검증
6. **예외 처리**: 권한이 없는 경우 `UnauthorizedAccessException` 발생

## 테스트 가이드

### 1. 인증 없이 조회 테스트
```bash
# Debate 게시글 조회 (성공해야 함)
GET /api/debate/posts

# FindTeam 게시글 조회 (성공해야 함)
GET /api/find-team/posts
```

### 2. 인증 없이 생성/수정/삭제 테스트
```bash
# Debate 게시글 생성 (401 Unauthorized)
POST /api/debate/posts

# FindTeam 게시글 수정 (401 Unauthorized)
PUT /api/find-team/posts/1
```

### 3. 인증 후 작성자가 아닌 사용자가 수정/삭제 테스트
```bash
# 다른 사용자의 게시글 수정 시도 (UnauthorizedAccessException)
PUT /api/debate/posts/1
Authorization: Bearer {user2_token}
```

### 4. 인증 후 작성자가 수정/삭제 테스트
```bash
# 본인의 게시글 수정 (성공해야 함)
PUT /api/debate/posts/1
Authorization: Bearer {user1_token}
```

## 파일 변경 사항

### 새로 생성된 파일
- `src/main/java/org/example/stcapstonebackend/common/exception/UnauthorizedAccessException.java`

### 수정된 파일
- `src/main/java/org/example/stcapstonebackend/common/security/SecurityConfig.java`
- `src/main/java/org/example/stcapstonebackend/debate/DebatePostService.java`
- `src/main/java/org/example/stcapstonebackend/debate/DebatePostController.java`
- `src/main/java/org/example/stcapstonebackend/debate/DebateCommentService.java`
- `src/main/java/org/example/stcapstonebackend/debate/DebateCommentController.java`
- `src/main/java/org/example/stcapstonebackend/findTeam/FindTeamPostService.java`
- `src/main/java/org/example/stcapstonebackend/findTeam/FindTeamPostController.java`
- `src/main/java/org/example/stcapstonebackend/findTeam/FindTeamRequestService.java`
- `src/main/java/org/example/stcapstonebackend/findTeam/FindTeamRequestController.java`

## 추가 고려사항

### 1. GlobalExceptionHandler 업데이트 필요
`UnauthorizedAccessException`에 대한 적절한 HTTP 응답(403 Forbidden)을 반환하도록 설정이 필요할 수 있습니다.

### 2. Debate 게시글의 CoWriter
현재 구현은 작성자(writer) 또는 공동 작성자(coWriter) 모두 수정/삭제가 가능합니다. 필요시 역할별 권한을 더 세분화할 수 있습니다.

### 3. FindTeam의 toggle-accept
`/api/find-team/posts/{postId}/requests/{requestId}/toggle-accept` 엔드포인트는 현재 인증만 필요하지만, 게시글 작성자만 수락/취소할 수 있도록 추가 검증이 필요할 수 있습니다.

