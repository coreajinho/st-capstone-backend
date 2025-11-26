# 토론 득표 결과 API 구현 가이드

## 📊 개요

토론 게시글의 댓글에서 `debateSide` (PLAYER_1, PLAYER_2) 결과를 집계하여 득표 현황을 제공하는 API가 구현되었습니다.

## 🏗️ 구현 내용

### 1. DebateVoteResultDto (득표 결과 DTO)

득표 결과를 담는 DTO로, 다음 정보를 포함합니다:

```java
{
  "debatePostId": 1,
  "player1Count": 15,      // PLAYER_1 투표 수
  "player2Count": 10,      // PLAYER_2 투표 수
  "totalCount": 25,        // 전체 투표 수
  "player1Percent": 60.0,  // PLAYER_1 득표율
  "player2Percent": 40.0   // PLAYER_2 득표율
}
```

**특징:**
- 엔티티에 필드 추가 없이 계산으로 제공 (데이터 중복 방지)
- 실시간으로 정확한 득표율 제공
- 두 가지 생성 방식 지원:
  - `fromEntity()`: 엔티티에서 직접 계산
  - `fromCounts()`: 집계된 값으로 생성 (성능 최적화)

### 2. DebateCommentRepository (쿼리 최적화)

득표 수를 효율적으로 집계하는 쿼리 메소드 추가:

```java
@Query("SELECT dc.debateSide as side, COUNT(dc) as count " +
       "FROM debate_comment dc " +
       "WHERE dc.debatePost.id = :postId " +
       "GROUP BY dc.debateSide")
List<DebateVoteCount> countByDebateSide(@Param("postId") Long postId);
```

**장점:**
- DB에서 집계된 데이터만 가져옴 (네트워크 트래픽 최소화)
- 댓글 수가 많아도 성능 저하 없음

### 3. DebatePostService (비즈니스 로직)

두 가지 버전의 득표 결과 조회 메소드 제공:

#### 일반 버전 (getVoteResult)
```java
public DebateVoteResultDto getVoteResult(Long postId)
```
- 엔티티에서 직접 계산
- 간단한 로직
- 소규모 댓글에 적합

#### 최적화 버전 (getVoteResultOptimized) ⭐ 권장
```java
public DebateVoteResultDto getVoteResultOptimized(Long postId)
```
- DB 쿼리로 집계
- 대량 댓글에도 빠른 성능
- 현재 API에서 사용 중

### 4. DebatePostController (API 엔드포인트)

득표 결과 조회 API 엔드포인트 추가:

```
GET /api/debate/posts/{id}/vote-result
```

## 📡 API 사용법

### 요청 예시

```bash
GET /api/debate/posts/1/vote-result
```

### 응답 예시

```json
{
  "debatePostId": 1,
  "player1Count": 15,
  "player2Count": 10,
  "totalCount": 25,
  "player1Percent": 60.0,
  "player2Percent": 40.0
}
```

### curl 예시

```bash
curl -X GET http://localhost:8080/api/debate/posts/1/vote-result
```

## 🎨 프론트엔드 연동 예시

### React 예시

```jsx
import React, { useEffect, useState } from 'react';
import axios from 'axios';

const DebateVoteBar = ({ postId }) => {
  const [voteResult, setVoteResult] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchVoteResult = async () => {
      try {
        const response = await axios.get(
          `/api/debate/posts/${postId}/vote-result`
        );
        setVoteResult(response.data);
      } catch (error) {
        console.error('득표 결과 조회 실패:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchVoteResult();
  }, [postId]);

  if (loading) return <div>로딩 중...</div>;
  if (!voteResult) return <div>득표 결과를 불러올 수 없습니다.</div>;

  return (
    <div className="debate-vote-bar">
      <h3>득표 현황</h3>
      
      {/* 득표율 바 */}
      <div className="vote-bar-container">
        <div 
          className="vote-bar player1"
          style={{ width: `${voteResult.player1Percent}%` }}
        >
          <span>PLAYER 1: {voteResult.player1Percent.toFixed(1)}%</span>
        </div>
        <div 
          className="vote-bar player2"
          style={{ width: `${voteResult.player2Percent}%` }}
        >
          <span>PLAYER 2: {voteResult.player2Percent.toFixed(1)}%</span>
        </div>
      </div>

      {/* 투표 수 표시 */}
      <div className="vote-counts">
        <div>PLAYER 1: {voteResult.player1Count}표</div>
        <div>PLAYER 2: {voteResult.player2Count}표</div>
        <div>전체: {voteResult.totalCount}표</div>
      </div>
    </div>
  );
};

export default DebateVoteBar;
```

### CSS 예시

```css
.debate-vote-bar {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.vote-bar-container {
  display: flex;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  margin: 15px 0;
}

.vote-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  transition: width 0.3s ease;
}

.vote-bar.player1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.vote-bar.player2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.vote-counts {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}
```

### Vue.js 예시

```vue
<template>
  <div class="debate-vote-bar">
    <h3>득표 현황</h3>
    
    <div v-if="loading">로딩 중...</div>
    
    <div v-else-if="voteResult">
      <!-- 득표율 바 -->
      <div class="vote-bar-container">
        <div 
          class="vote-bar player1"
          :style="{ width: voteResult.player1Percent + '%' }"
        >
          <span>PLAYER 1: {{ voteResult.player1Percent.toFixed(1) }}%</span>
        </div>
        <div 
          class="vote-bar player2"
          :style="{ width: voteResult.player2Percent + '%' }"
        >
          <span>PLAYER 2: {{ voteResult.player2Percent.toFixed(1) }}%</span>
        </div>
      </div>

      <!-- 투표 수 표시 -->
      <div class="vote-counts">
        <div>PLAYER 1: {{ voteResult.player1Count }}표</div>
        <div>PLAYER 2: {{ voteResult.player2Count }}표</div>
        <div>전체: {{ voteResult.totalCount }}표</div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'DebateVoteBar',
  props: {
    postId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      voteResult: null,
      loading: true
    };
  },
  mounted() {
    this.fetchVoteResult();
  },
  methods: {
    async fetchVoteResult() {
      try {
        const response = await axios.get(
          `/api/debate/posts/${this.postId}/vote-result`
        );
        this.voteResult = response.data;
      } catch (error) {
        console.error('득표 결과 조회 실패:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
```

## 🔄 실시간 업데이트 구현

댓글이 추가될 때마다 득표율을 업데이트하려면:

```javascript
// 댓글 작성 후 득표 결과 갱신
const handleCommentSubmit = async (commentData) => {
  try {
    // 댓글 작성
    await axios.post('/api/debate/comments', commentData);
    
    // 득표 결과 다시 조회
    const voteResponse = await axios.get(
      `/api/debate/posts/${postId}/vote-result`
    );
    setVoteResult(voteResponse.data);
  } catch (error) {
    console.error('오류 발생:', error);
  }
};
```

## ✅ 장점

1. **데이터 일관성**: 엔티티에 필드를 추가하지 않아 데이터 불일치 없음
2. **실시간 정확도**: 항상 최신 댓글 수를 기반으로 계산
3. **성능 최적화**: 쿼리 집계로 대량 데이터에도 빠른 응답
4. **유지보수 용이**: 득표율 계산 로직이 한 곳에 집중
5. **확장 가능**: 추가 통계 정보도 쉽게 추가 가능

## 🔧 추가 개선 사항 (선택사항)

### 1. 캐싱 추가

득표 결과를 캐싱하여 성능 향상:

```java
@Cacheable(value = "voteResults", key = "#postId")
public DebateVoteResultDto getVoteResultOptimized(Long postId) {
    // 기존 코드...
}

@CacheEvict(value = "voteResults", key = "#comment.debatePost.id")
public void createComment(DebateComment comment) {
    // 댓글 생성 시 캐시 무효화
}
```

### 2. WebSocket으로 실시간 업데이트

```java
@MessageMapping("/debate/{postId}/vote")
@SendTo("/topic/debate/{postId}/vote-result")
public DebateVoteResultDto broadcastVoteUpdate(@DestinationVariable Long postId) {
    return debatePostService.getVoteResultOptimized(postId);
}
```

## 📝 테스트 예시

```java
@Test
void testGetVoteResult() {
    // Given
    Long postId = 1L;
    
    // When
    DebateVoteResultDto result = debatePostService.getVoteResultOptimized(postId);
    
    // Then
    assertNotNull(result);
    assertEquals(postId, result.getDebatePostId());
    assertEquals(100.0, result.getPlayer1Percent() + result.getPlayer2Percent());
}
```

## 🚀 배포 및 사용

1. 프로젝트 빌드:
   ```bash
   ./gradlew build
   ```

2. 애플리케이션 실행:
   ```bash
   ./gradlew bootRun
   ```

3. API 테스트:
   ```bash
   curl http://localhost:8080/api/debate/posts/1/vote-result
   ```

---

**구현 완료!** 이제 프론트엔드에서 이 API를 호출하여 득표 현황 바를 구현할 수 있습니다. 🎉

