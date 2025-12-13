# 토론 만료 및 결산 기능 테스트 가이드

## 개요
토론 게시글의 만료 및 자동 결산 기능을 테스트하기 위한 가이드입니다. 개발 프로필을 사용하여 짧은 만료 시간과 빠른 스케줄러 주기로 테스트할 수 있습니다.

## 1. 통합 테스트 실행

### 1.1 테스트 실행 방법

```bash
# Windows
.\gradlew test --tests "org.example.stcapstonebackend.debate.DebateExpirationIntegrationTest"

# Linux/Mac
./gradlew test --tests "org.example.stcapstonebackend.debate.DebateExpirationIntegrationTest"
```

**중요**: 통합 테스트는 `test` 프로필을 사용하며, **H2 인메모리 데이터베이스**를 사용합니다.
- MySQL 데이터베이스가 실행 중이지 않아도 테스트 가능
- 각 테스트는 격리된 환경에서 실행 (`@Transactional` 사용)
- 테스트 후 데이터는 자동으로 롤백됨 (트랜잭션)
- 스케줄러와 테스트 간의 트랜잭션 격리 문제는 `EntityManager.flush()`와 `clear()`로 해결

### 1.2 테스트 내용

통합 테스트는 다음 시나리오를 검증합니다:

1. **Player1 승리 시나리오**: Player1(writer)이 승리하고, 모든 사용자의 통계가 올바르게 업데이트되는지 확인
2. **Player2 승리 시나리오**: Player2(coWriter)가 승리하고, 통계가 올바르게 업데이트되는지 확인
3. **무승부 시나리오**: 동점이고 연장 불가능한 경우 무승부로 처리되는지 확인
4. **동점 연장 시나리오**: 동점이고 연장 가능한 경우 PENDING 상태로 전환되는지 확인
5. **CoWriter 없는 경우**: Writer만 있는 게시글의 통계가 올바르게 업데이트되는지 확인

## 2. 수동 테스트 (개발 프로필 사용)

### 2.1 IntelliJ IDEA Run Configuration 설정

1. **Run/Debug Configurations** 열기
   - 상단 메뉴: `Run` → `Edit Configurations...`

2. **새로운 Spring Boot Configuration 생성**
   - `+` 버튼 클릭 → `Spring Boot` 선택
   - **Name**: `StCapstoneBackend (dev)`
   - **Main class**: `org.example.stcapstonebackend.StCapstoneBackendApplication`

3. **Active profiles 설정**
   - `Active profiles` 필드에 `dev` 입력

4. **저장 및 실행**
   - `Apply` → `OK`
   - 해당 Configuration으로 애플리케이션 실행

### 2.2 환경변수 설정 확인

개발 프로필에서는 다음 설정이 적용됩니다 (`.env` 파일):

```properties
# 토론 만료 시간: 5분
DEBATE_EXPIRATION_OVERRIDE=PT5M

# 스케줄러 실행 주기: 10초
MATCH_CHECK_INTERVAL_OVERRIDE=10000

# 토론 동점 시 연장 시간: 1시간
DEBATE_PENDING_EXTENSION_DURATION=PT1H
```

### 2.3 수동 테스트 절차

1. **서버 시작**
   ```bash
   # 또는 IntelliJ Run Configuration으로 실행
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

2. **토론 게시글 생성**
   - API: `POST /api/debates`
   - Body 예시:
     ```json
     {
       "title": "테스트 토론",
       "content": "테스트 내용",
       "writerId": 1,
       "coWriterId": 2,
       "debateDurationHours": 1,
       "tags": ["TOP"]
     }
     ```
   - **주의**: 개발 프로필에서는 `debateDurationHours` 값과 무관하게 5분 후 만료됩니다.

3. **댓글 작성 (투표)**
   - API: `POST /api/debates/{debateId}/comments`
   - Player1 또는 Player2를 선택하여 댓글 작성

4. **만료 대기**
   - 5분 후, 스케줄러가 자동으로 게시글을 만료 처리합니다.
   - 로그 확인: `토론 만료 스케줄러 실행`, `토론 결산 시작` 등의 로그가 출력됩니다.

5. **결과 확인**
   - API: `GET /api/debates/{debateId}`
   - `debateStatus`가 `EXPIRED` 또는 `PENDING`으로 변경되었는지 확인
   - 사용자 통계 확인: `GET /api/users/{userId}`
   - `debateWins`, `debateLosses`, `judgementSuccesses` 등이 업데이트되었는지 확인

## 3. 프로필별 설정 비교

| 항목 | 프로덕션 (`application.properties`) | 개발 (`application-dev.properties`) | 테스트 (`application-test.properties`) |
|------|-------------------------------------|-------------------------------------|-----------------------------------------|
| 데이터베이스 | MySQL (외부) | MySQL (외부) | **H2 인메모리** |
| 토론 만료 시간 | 요청된 `debateDurationHours` 사용 | **5분 고정** | **5분 고정** |
| 스케줄러 주기 | 60초 (1분) | **10초** | **10초** |
| 로그 레벨 | INFO | **DEBUG** (debate 패키지) | **DEBUG** (debate 패키지) |
| DDL 자동 생성 | update | update | **create-drop** |

## 4. 주의사항

1. **개발 프로필은 수동 테스트 목적으로만 사용**
   - 프로덕션 환경에서는 절대 `dev` 프로필을 사용하지 마세요.
   - 5분 만료 시간은 실제 서비스에 적합하지 않습니다.

2. **데이터베이스**
   - **테스트 프로필 (`test`)**: H2 인메모리 DB 사용 → MySQL 불필요, 자동화 테스트에 적합
   - **개발 프로필 (`dev`)**: 실제 MySQL DB 사용 → 수동 테스트용, 데이터가 DB에 남음

3. **환경변수 관리**
   - `.env` 파일은 Git에 커밋하지 마세요.
   - 환경변수 값은 `.env.example`에 예시로만 남기세요.

## 5. 트러블슈팅

### 5.1 테스트가 실패하는 경우

- **환경변수 미설정**: `.env` 파일에 모든 필수 환경변수가 설정되어 있는지 확인
- **데이터베이스 연결 실패**: MySQL이 실행 중인지 확인 (개발 프로필 사용 시)
- **포트 충돌**: 8080 포트가 사용 중인지 확인

### 5.2 스케줄러가 실행되지 않는 경우

- 로그 레벨을 DEBUG로 설정하여 스케줄러 로그 확인
- `@EnableScheduling`이 활성화되어 있는지 확인
- 만료 시간이 정확히 과거인지 확인

### 5.3 테스트에서 스케줄러 변경사항이 반영되지 않는 경우

**원인**: 테스트의 `@Transactional`과 스케줄러의 별도 트랜잭션이 격리됨

**해결방법**: 
```java
// 스케줄러 호출 전: 테스트 트랜잭션의 변경사항을 DB에 반영
entityManager.flush();
entityManager.clear();

// 스케줄러 실행
debateExpirationScheduler.expireDebatePosts();

// 검증 전: 스케줄러 트랜잭션의 변경사항을 가져오기
entityManager.flush();
entityManager.clear();
```

이렇게 하면 `@Transactional`을 유지하면서도 트랜잭션 간 데이터를 동기화할 수 있습니다.

## 6. 관련 파일

- **테스트 프로필 설정**: `src/test/resources/application-test.properties` (H2 인메모리 DB)
- **개발 프로필 설정**: `src/main/resources/application-dev.properties` (MySQL)
- **환경변수**: `.env`
- **통합 테스트**: `src/test/java/org/example/stcapstonebackend/debate/DebateExpirationIntegrationTest.java`
- **스케줄러**: `src/main/java/org/example/stcapstonebackend/debate/scheduler/DebateExpirationScheduler.java`
- **결산 로직**: `src/main/java/org/example/stcapstonebackend/debate/DebatePostService.java` (settleDebate 메서드)
- **Gradle 의존성**: `build.gradle` (H2 데이터베이스 포함)

## 7. 기대 결과

### 7.1 게시글 만료 처리

- **승부가 결정된 경우**: `debateStatus`가 `EXPIRED`로 변경
- **동점이고 연장 가능한 경우**: `debateStatus`가 `PENDING`으로 변경, `expiresAt`이 1시간 연장

### 7.2 사용자 통계 업데이트

- **Writer (Player1)**
  - 승리 시: `debateWins` +1
  - 패배 시: `debateLosses` +1
  - 무승부 시: `debateDraws` +1

- **CoWriter (Player2)**
  - 승리 시: `debateWins` +1
  - 패배 시: `debateLosses` +1
  - 무승부 시: `debateDraws` +1

- **댓글 작성자 (판결 참여자)**
  - 승리 진영에 투표 시: `judgementSuccesses` +1
  - 패배 진영에 투표 시: `judgementFailures` +1
  - 무승부 시: 변동 없음

