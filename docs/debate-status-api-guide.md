# 토론 게시판 상태 관리 시스템 API 가이드

## 📋 개요

토론 게시판에 **ACTIVE/PENDING/EXPIRED** 상태 관리 시스템과 자동 결산 기능이 추가되었습니다.

### 주요 기능
- ✅ 토론 게시글 상태 관리 (ACTIVE → PENDING → EXPIRED)
- ✅ 동점 시 1시간 단위 자동 연장 (최대 원래 토론 기간까지)
- ✅ 토론 종료 시 자동 결산 (승패/무승부 집계)
- ✅ 사용자 토론 통계 자동 업데이트
- ✅ 상태별 필터링 및 정렬 기능
- ✅ 소환사 검색 시 토론 통계 포함

---

## 🔄 토론 상태 (DebateStatus)

| 상태 | 설명 | 의미 |
|------|------|------|
| `ACTIVE` | 활성 상태 | 토론이 진행 중 |
| `PENDING` | 연장 상태 | 동점으로 인해 추가 시간 진행 중 |
| `EXPIRED` | 만료 상태 | 토론 기간 종료 및 결산 완료 |

### 상태 전환 로직

```
게시글 생성 → ACTIVE
     ↓
만료 시간 도달
     ↓
투표 집계
     ↓
동점? ─── YES ──→ 연장 가능? ─── YES ──→ PENDING (1시간 연장)
  ↓ NO                        ↓ NO            ↓
  ↓                            ↓            반복 체크
  ↓                            ↓                ↓
  └────────────→ EXPIRED ←─────┘          (최대 원래 기간)
                    ↓
                  결산 수행
```

---

## 📊 토론 결산 로직

### 결산 시점
- 토론 만료 시간(`expiresAt`)이 지났을 때
- 동점이지만 더 이상 연장할 수 없을 때
- 스케줄러가 자동으로 처리 (주기: `scheduler.match-check-interval`)

### 결산 내용

#### 1. 토론 작성자 통계 업데이트
- **writer (PLAYER_1 진영)**
  - 승리: `debateWins +1`
  - 패배: `debateLosses +1`
  - 무승부: `debateDraws +1`

- **coWriter (PLAYER_2 진영)** *(존재하는 경우)*
  - 승리: `debateWins +1`
  - 패배: `debateLosses +1`
  - 무승부: `debateDraws +1`

#### 2. 판결 작성자(댓글 작성자) 통계 업데이트
- **승부가 결정된 경우**
  - 자신이 선택한 진영(`debateSide`)이 승리: `judgementSuccesses +1`
  - 자신이 선택한 진영이 패배: `judgementFailures +1`
- **무승부인 경우**
  - 판결 통계 업데이트 없음 (성공/실패 모두 증가하지 않음)

---

## 🔧 변경된 API 명세

### 1. 토론 게시글 생성 (POST)
**Endpoint:** `POST /api/debate/posts`

#### 요청 Body (변경사항)
```json
{
  "title": "페이커 vs 쇼메이커 누가 더 잘할까?",
  "content": "토론 내용...",
  "writerId": 1,
  "coWriterId": 2,
  "videoUrl": "https://youtube.com/...",
  "tags": ["MID", "TOP"],
  "debateDurationHours": 24  // ✨ 새로 추가: 토론 기간 (시간 단위, 필수)
}
```

#### 응답 Body (변경사항)
```json
{
  "id": 123,
  "title": "페이커 vs 쇼메이커 누가 더 잘할까?",
  "content": "토론 내용...",
  "writer": "Faker#KR1",
  "writerId": 1,
  "coWriter": "ShowMaker#KR1",
  "coWriterId": 2,
  "videoUrl": "https://youtube.com/...",
  "views": 0,
  "commentCount": 0,
  "tags": ["MID", "TOP"],
  "debateStatus": "ACTIVE",                    // ✨ 새로 추가
  "debateDurationHours": 24,                   // ✨ 새로 추가
  "expiresAt": "2025-12-14T15:30:00",          // ✨ 새로 추가
  "totalExtensionTimeHours": 0,                // ✨ 새로 추가
  "createdAt": "2025-12-13T15:30:00",
  "modifiedAt": "2025-12-13T15:30:00",
  "comments": []
}
```

### 2. 전체 토론 게시글 조회 (GET) - 필터링 지원
**Endpoint:** `GET /api/debate/posts`

#### Query Parameters (새로 추가)
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `status` | String | ❌ | 필터링할 상태 | `ACTIVE`, `PENDING`, `EXPIRED` |

#### 사용 예시

**1. 전체 조회 (상태 우선순위 정렬)**
```http
GET /api/debate/posts
```
- 정렬 순서: `PENDING` → `ACTIVE` → `EXPIRED` (같은 상태 내에서는 최신순)

**2. ACTIVE 상태만 조회**
```http
GET /api/debate/posts?status=ACTIVE
```

**3. EXPIRED 상태만 조회 (종료된 토론)**
```http
GET /api/debate/posts?status=EXPIRED
```

#### 응답 Body
```json
[
  {
    "id": 123,
    "title": "페이커 vs 쇼메이커",
    "debateStatus": "ACTIVE",
    "expiresAt": "2025-12-14T15:30:00",
    "totalExtensionTimeHours": 0,
    // ... 기타 필드
  },
  {
    "id": 122,
    "title": "제우스 vs 키겐",
    "debateStatus": "PENDING",              // 연장 중
    "expiresAt": "2025-12-13T17:00:00",
    "totalExtensionTimeHours": 2,           // 2시간 연장됨
    // ... 기타 필드
  }
]
```

### 3. 단일 게시글 조회 (GET)
**Endpoint:** `GET /api/debate/posts/{id}`

#### 응답 Body
- 위의 "토론 게시글 생성 응답"과 동일한 구조
- `debateStatus`, `expiresAt`, `totalExtensionTimeHours` 필드 포함

### 4. 소환사 검색 (GET) - 토론 통계 추가
**Endpoint:** `GET /api/summoner/account?fullName=Faker%23KR1`

#### 응답 Body (변경사항)
```json
{
  "nickname": "Faker",
  "tagline": "KR1",
  "puuid": "...",
  "soloTier": "CHALLENGER",
  "soloDivision": "I",
  "soloPoints": 1234,
  "soloWins": 50,
  "soloLoses": 30,
  "flexTier": "MASTER",
  "flexDivision": "I",
  "flexPoints": 200,
  "flexWins": 20,
  "flexLoses": 10,
  // ✨ 토론 통계 추가
  "debateWins": 15,              // 토론 승리 횟수
  "debateLosses": 5,             // 토론 패배 횟수
  "debateDraws": 2,              // 토론 무승부 횟수
  "judgementSuccesses": 30,      // 판결 성공 횟수
  "judgementFailures": 10        // 판결 실패 횟수
}
```

#### 토론 통계 필드 설명
- **debateWins**: writer 또는 coWriter로 참여한 토론에서 승리한 횟수
- **debateLosses**: writer 또는 coWriter로 참여한 토론에서 패배한 횟수
- **debateDraws**: writer 또는 coWriter로 참여한 토론에서 무승부로 끝난 횟수
- **judgementSuccesses**: 댓글(판결)로 참여한 토론에서 승리 진영을 맞춘 횟수
- **judgementFailures**: 댓글(판결)로 참여한 토론에서 승리 진영을 맞추지 못한 횟수

> ⚠️ **주의**: User 정보가 없는 소환사의 경우 모든 토론 통계는 `0`으로 반환됩니다.

---

## 🎨 프론트엔드 UI 구현 가이드

### 1. 토론 상태 표시

#### 상태별 배지 색상 추천
```javascript
const getStatusBadge = (status) => {
  switch(status) {
    case 'ACTIVE':
      return <Badge color="green">진행중</Badge>;
    case 'PENDING':
      return <Badge color="orange">연장전</Badge>;
    case 'EXPIRED':
      return <Badge color="gray">종료</Badge>;
  }
};
```

#### 만료 시간 표시
```javascript
const formatTimeRemaining = (expiresAt) => {
  const now = new Date();
  const expire = new Date(expiresAt);
  const diff = expire - now;
  
  if (diff <= 0) return '만료됨';
  
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  
  return `${hours}시간 ${minutes}분 남음`;
};
```

#### 연장 상태 표시
```javascript
const ExtensionInfo = ({ status, totalExtensionTimeHours }) => {
  if (status !== 'PENDING') return null;
  
  return (
    <div className="extension-info">
      ⏰ 연장전 진행 중 (총 {totalExtensionTimeHours}시간 연장됨)
    </div>
  );
};
```

### 2. 게시글 목록 필터 탭

```javascript
const DebateListTabs = () => {
  const [activeTab, setActiveTab] = useState('all');
  
  const fetchPosts = async (status) => {
    const url = status 
      ? `/api/debate/posts?status=${status}`
      : '/api/debate/posts';
    
    const response = await fetch(url);
    return response.json();
  };
  
  return (
    <div>
      <Tabs value={activeTab} onChange={setActiveTab}>
        <Tab value="all">전체</Tab>
        <Tab value="ACTIVE">진행중</Tab>
        <Tab value="PENDING">연장전</Tab>
        <Tab value="EXPIRED">종료</Tab>
      </Tabs>
      
      {/* 게시글 목록 렌더링 */}
    </div>
  );
};
```

### 3. 토론 생성 폼

```javascript
const CreateDebateForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    writerId: currentUser.id,
    coWriterId: null,
    debateDurationHours: 24, // 기본값 24시간
    videoUrl: '',
    tags: []
  });
  
  return (
    <form onSubmit={handleSubmit}>
      {/* 기존 필드들... */}
      
      <FormGroup>
        <Label>토론 기간</Label>
        <Select 
          value={formData.debateDurationHours}
          onChange={(e) => setFormData({
            ...formData, 
            debateDurationHours: Number(e.target.value)
          })}
        >
          <option value={12}>12시간</option>
          <option value={24}>24시간 (1일)</option>
          <option value={48}>48시간 (2일)</option>
          <option value={72}>72시간 (3일)</option>
          <option value={168}>168시간 (7일)</option>
        </Select>
      </FormGroup>
    </form>
  );
};
```

### 4. 소환사 프로필에 토론 통계 표시

```javascript
const SummonerDebateStats = ({ stats }) => {
  const totalDebates = stats.debateWins + stats.debateLosses + stats.debateDraws;
  const winRate = totalDebates > 0 
    ? ((stats.debateWins / totalDebates) * 100).toFixed(1) 
    : 0;
  
  const judgementRate = (stats.judgementSuccesses + stats.judgementFailures) > 0
    ? ((stats.judgementSuccesses / (stats.judgementSuccesses + stats.judgementFailures)) * 100).toFixed(1)
    : 0;
  
  return (
    <div className="debate-stats">
      <h3>토론 통계</h3>
      
      <div className="stat-section">
        <h4>토론 참여</h4>
        <div>총 {totalDebates}회</div>
        <div>승: {stats.debateWins}회</div>
        <div>패: {stats.debateLosses}회</div>
        <div>무: {stats.debateDraws}회</div>
        <div>승률: {winRate}%</div>
      </div>
      
      <div className="stat-section">
        <h4>판결 참여</h4>
        <div>성공: {stats.judgementSuccesses}회</div>
        <div>실패: {stats.judgementFailures}회</div>
        <div>정확도: {judgementRate}%</div>
      </div>
    </div>
  );
};
```

---

## ⚙️ 환경 변수 설정

### application.properties 또는 환경 변수에 추가

```properties
# 토론 연장 시간 (기본값: 1시간)
DEBATE_PENDING_EXTENSION_DURATION=PT1H

# 스케줄러 실행 주기 (기본값은 기존 설정 사용)
MATCH_CHECK_INTERVAL=300000
```

### Duration 형식 참고
- `PT1H` = 1시간
- `PT30M` = 30분
- `PT2H` = 2시간
- `PT24H` = 24시간

---

## 🔍 동작 시나리오 예시

### 시나리오 1: 정상 종료
1. 게시글 생성 (24시간 기간) → `ACTIVE`
2. 24시간 후 Player1 투표 10개, Player2 투표 5개
3. 스케줄러 실행 → `EXPIRED`로 전환
4. 결산: Player1 승리, writer의 `debateWins +1`, coWriter의 `debateLosses +1`

### 시나리오 2: 1회 연장 후 종료
1. 게시글 생성 (24시간 기간) → `ACTIVE`
2. 24시간 후 Player1 투표 5개, Player2 투표 5개 (동점)
3. 스케줄러 실행 → `PENDING`으로 전환, 1시간 연장
4. 1시간 후 Player1 투표 7개, Player2 투표 6개
5. 스케줄러 실행 → `EXPIRED`로 전환
6. 결산: Player1 승리

### 시나리오 3: 최대 연장 후 무승부
1. 게시글 생성 (2시간 기간) → `ACTIVE`
2. 2시간 후 동점 → `PENDING`, 1시간 연장
3. 3시간 후 동점 → `PENDING`, 1시간 연장 (총 2시간 연장)
4. 4시간 후 동점 → 더 이상 연장 불가 (원래 기간 2시간 도달)
5. `EXPIRED`로 전환, 결산: 무승부
6. writer와 coWriter 모두 `debateDraws +1`
7. 판결 작성자의 통계는 업데이트되지 않음 (성공/실패 모두 증가하지 않음)

---

## 📝 주의사항

### 1. 토론 기간 선택
- 최소 1시간 이상 권장
- 너무 짧은 기간 설정 시 사용자 참여 저조 가능

### 2. 상태 표시
- `EXPIRED` 상태 게시글은 기본적으로 목록 하단에 표시됨
- 별도 필터로 종료된 토론 검색 가능

### 3. 통계 갱신
- 토론 통계는 결산 시점에 자동 갱신
- 실시간 갱신이 아니므로 결산 완료 후 반영됨

### 4. 권한 관리
- 토론 작성/수정 시 `writerId`, `coWriterId` 검증 필요
- 만료된 토론(`EXPIRED`)은 수정/삭제 불가 권장

---

## 🐛 문제 해결

### Q1. 토론이 자동으로 만료되지 않아요
**A1.** 스케줄러 설정 확인
- `scheduler.match-check-interval` 값이 너무 크지 않은지 확인
- 로그에서 "토론 만료 스케줄러 실행" 메시지 확인

### Q2. 토론 통계가 표시되지 않아요
**A2.** User 정보 확인
- `riotName`과 `riotTag`가 정확히 일치하는지 확인
- 가입하지 않은 소환사는 통계가 0으로 표시됨

### Q3. 연장이 계속 반복돼요
**A3.** 정상 동작입니다
- 최대 원래 토론 기간만큼만 연장됩니다
- 예: 24시간 토론 → 최대 24시간까지만 연장 가능

---

## 📞 백엔드 개발자 연락처

구현 중 문제가 있거나 추가 요청사항이 있으면 백엔드 팀에 문의해주세요.

**구현 완료일:** 2025-12-13  
**버전:** v2.0.0

