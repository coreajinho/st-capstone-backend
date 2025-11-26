# 🚀 JWT 인증 시스템 - Quick Start

## 개요
- **인증 방식**: Servlet 기반 Spring Security + JWT
- **처리 방식**: 동기(Blocking)

## 빠른 시작 (5분 안에 테스트하기)

### 1️⃣ 애플리케이션 실행
```bash
cd F:\Projects\st-capstone-backend
.\gradlew bootRun
```

### 2️⃣ 회원가입 테스트
**Postman / Thunder Client / cURL**
```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "admin1234",
  "nickname": "관리자"
}
```

**예상 응답 (201 Created):**
```json
{
  "id": 1,
  "email": "admin@test.com",
  "nickname": "관리자",
  "role": "USER"
}
```

### 3️⃣ 로그인 테스트
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "admin1234"
}
```

**예상 응답 (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzA2MDAwMDAwLCJleHAiOjE3MDYwODY0MDB9.xxxxx",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

👉 **accessToken 값을 복사하세요!**

### 4️⃣ 인증된 API 호출
```http
GET http://localhost:8080/api/auth/me
Authorization: Bearer {위에서_복사한_accessToken}
```

**예상 응답 (200 OK):**
```json
{
  "id": 1,
  "email": "admin@test.com",
  "nickname": "관리자",
  "role": "USER"
}
```

---

## 📱 Postman 설정 방법

### 1. 회원가입/로그인 요청
1. Method: `POST`
2. URL: `http://localhost:8080/api/auth/signup` (또는 `/login`)
3. Headers: `Content-Type: application/json`
4. Body (raw, JSON):
   ```json
   {
     "email": "test@example.com",
     "password": "test1234",
     "nickname": "테스터"
   }
   ```

### 2. 인증이 필요한 API 요청
1. Method: `GET`
2. URL: `http://localhost:8080/api/auth/me`
3. Headers:
   - `Authorization: Bearer {토큰값}`
4. Body: (없음)

---

## ⚡ 주요 엔드포인트

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | ❌ | 회원가입 |
| POST | `/api/auth/login` | ❌ | 로그인 (토큰 발급) |
| GET | `/api/auth/me` | ✅ | 현재 사용자 정보 |

---

## 🔧 문제 해결

### ❌ 401 Unauthorized
→ JWT 토큰이 없거나 만료되었습니다. 다시 로그인하세요.

### ❌ 409 Conflict (회원가입)
→ 이미 존재하는 이메일입니다. 다른 이메일을 사용하세요.

### ❌ 400 Bad Request
→ 입력 검증 실패입니다. 요청 본문을 확인하세요.
- 이메일 형식 확인
- 비밀번호 8자 이상
- 닉네임 2자 이상

---

## 📖 더 알아보기

- **상세 가이드**: `docs/JWT_AUTHENTICATION_GUIDE.md`
- **구현 요약**: `docs/IMPLEMENTATION_SUMMARY.md`

---

**Happy Coding! 🎉**

