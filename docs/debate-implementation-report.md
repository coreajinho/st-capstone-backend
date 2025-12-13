# 토론 게시판 상태 관리 시스템 구현 완료 보고서

## ✅ 구현 완료 사항

### 1. 엔티티 레벨 변경

#### DebatePost 엔티티
- ✅ `DebateStatus` enum 추가 (ACTIVE/PENDING/EXPIRED)
- ✅ `debateStatus` 필드 추가 (기본값: ACTIVE)
- ✅ `debateDurationHours` 필드 추가 (토론 기간)
- ✅ `expiresAt` 필드 추가 (만료 예정 시간)
- ✅ `totalExtensionTimeHours` 필드 추가 (총 연장 시간)
- ✅ 상태 전환 메서드 구현
  - `markAsPending(long extensionHours)` - PENDING으로 전환 및 시간 연장
  - `markAsExpired()` - EXPIRED로 전환
  - `canExtend(long extensionHours)` - 연장 가능 여부 확인

#### User 엔티티
- ✅ `debateWins` 필드 추가 (토론 승리 횟수)
- ✅ `debateLosses` 필드 추가 (토론 패배 횟수)
- ✅ `debateDraws` 필드 추가 (토론 무승부 횟수)
- ✅ `judgementSuccesses` 필드 추가 (판결 성공 횟수)
- ✅ `judgementFailures` 필드 추가 (판결 실패 횟수)
- ✅ 통계 증가 메서드 구현

### 2. Repository 레벨 변경

#### DebatePostRepository
- ✅ `findByDebateStatusOrderByCreatedAtDesc()` - 특정 상태의 게시글 조회
- ✅ `findByDebateStatusAndExpiresAtBefore()` - 만료된 게시글 조회
- ✅ `findAllOrderedByStatusAndCreatedAt()` - 상태 우선순위 정렬 (PENDING > ACTIVE > EXPIRED)

### 3. Service 레벨 변경

#### DebatePostService
- ✅ 결산 관련 메서드 구현
  - `settleDebate()` - 토론 결산 수행
  - `updateWriterStats()` - 작성자 통계 업데이트
  - `updateJudgementStats()` - 판결 작성자 통계 업데이트 (무승부 시 제외)

---

## 📊 결산 프로세스

### 토론 작성자 통계 업데이트
- writer (Player1 진영)
  - 승리: debateWins +1
  - 패배: debateLosses +1
  - 무승부: debateDraws +1

- coWriter (Player2 진영) - 존재하는 경우만
  - 승리: debateWins +1
  - 패배: debateLosses +1
  - 무승부: debateDraws +1

### 판결 작성자(댓글) 통계 업데이트
- **승부가 결정된 경우**
  - 자신이 선택한 진영이 승리: judgementSuccesses +1
  - 자신이 선택한 진영이 패배: judgementFailures +1
- **무승부인 경우**
  - 판결 통계 업데이트 없음 (성공/실패 모두 증가하지 않음)

---

## 🔍 API 변경 사항 요약

### 게시글 목록 정렬 순서
- **기본 정렬**: PENDING → ACTIVE → EXPIRED (같은 상태 내에서는 최신순)
- **필터링 지원**: `GET /api/debate/posts?status=ACTIVE`

---

## 🧪 테스트 시나리오

### 시나리오 3: 최대 연장 후 무승부
1. 게시글 생성 (2시간 기간) → ACTIVE
2. 2시간 후 동점 → PENDING + 1시간 연장
3. 1시간 후 동점 → PENDING + 1시간 연장 (총 2시간 연장)
4. 1시간 후 동점 → 더 이상 연장 불가
5. EXPIRED + 무승부 결산
6. writer와 coWriter 모두 debateDraws +1
7. **판결 작성자의 통계는 업데이트되지 않음**

---

## ✅ 빌드 확인

```bash
$ ./gradlew clean build -x test
BUILD SUCCESSFUL
```

**구현 완료일**: 2025-12-13  
**버전**: v2.0.0

