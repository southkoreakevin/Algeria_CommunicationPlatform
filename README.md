# Algeria Communication Platform

## 기술 스택

| 항목 | 내용 |
|------|------|
| Framework | Spring Boot 4.0.5 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| 인증 | JWT (jjwt 0.12.6) |
| 실시간 통신 | WebSocket + STOMP |
| Build Tool | Gradle |

---

## 실행 방법

```bash
export $(cat .env | xargs) && ./gradlew bootRun
```

서버 기동 후 `http://localhost:8080` 에서 접근 가능

---

## 환경변수 (.env)

```
JWT_SECRET=your-jwt-secret-key
```

---

## API 명세

### 공통

```
Base URL: http://localhost:8080
Content-Type: application/json

# 인증이 필요한 API
Authorization: Bearer {token}
```

---

### 인증

#### 회원가입
```
POST /api/join

Request:  { "email": "user@example.com", "password": "1234" }
Response: 200 "Sign Up success"
          400 "이미 존재하는 이메일 입니다."
```

#### 이메일 중복 체크
```
POST /api/idCheck

Request:  { "email": "user@example.com" }
Response: 200 true   (이미 존재)
          200 false  (사용 가능)
```

#### 로그인
```
POST /api/login

Request:  { "email": "user@example.com", "password": "1234" }
Response: 200 { "token": "eyJhbGci..." }
          400 (이메일/비밀번호 불일치)
```

---

### 친구 (JWT 필요)

#### 친구 요청 보내기
```
POST /api/friends/request

Request:  { "receiverEmail": "friend@example.com" }
Response: 200 "친구 요청을 보냈습니다."
          400 "자기 자신에게 친구 요청을 보낼 수 없습니다."
          400 "이미 친구이거나 요청이 존재합니다."
```

#### 받은 친구 요청 목록
```
GET /api/friends/requests

Response: 200 [{ "friendshipId": 1, "requesterEmail": "user@example.com" }]
```

#### 친구 요청 수락
```
POST /api/friends/accept/{friendshipId}

Response: 200 "친구 요청을 수락했습니다."
          400 "권한이 없습니다."
```

#### 친구 요청 거절
```
POST /api/friends/reject/{friendshipId}

Response: 200 "친구 요청을 거절했습니다."
          400 "권한이 없습니다."
```

#### 친구 목록
```
GET /api/friends

Response: 200 [{ "id": 1, "email": "friend@example.com" }]
```

---

### 채팅 (JWT 필요)

#### 채팅방 생성 (1:1)
```
POST /api/chat/rooms

Request:  { "targetEmail": "friend@example.com" }
Response: 200 { "id": 1, "type": "DIRECT", "memberEmails": ["a@test.com", "b@test.com"] }
```
> 이미 채팅방이 존재하면 기존 방을 반환합니다.

#### 내 채팅방 목록
```
GET /api/chat/rooms

Response: 200 [{ "id": 1, "type": "DIRECT", "memberEmails": [...] }]
```

#### 이전 메시지 조회 (페이징)
```
GET /api/chat/rooms/{roomId}/messages?page=0

Response: 200 [
  {
    "id": 1,
    "senderEmail": "alice@test.com",
    "content": "안녕하세요",
    "sentAt": "2026-04-22T23:30:00"
  }
]
```
> page 기본값 0, 한 페이지 30개, 최신순 정렬

---

### WebSocket (실시간 채팅)

#### 연결
```
URL: ws://localhost:8080/ws (SockJS 사용 시 http://localhost:8080/ws)

CONNECT 헤더:
  Authorization: Bearer {token}
```

#### 메시지 구독
```
SUBSCRIBE /topic/chat/{roomId}

수신 메시지:
{
  "id": 1,
  "senderEmail": "alice@test.com",
  "content": "안녕하세요",
  "sentAt": "2026-04-22T23:30:00"
}
```

#### 메시지 전송
```
SEND /app/chat/{roomId}

{ "content": "안녕하세요" }
```

---

## 프로젝트 구조

```
src/main/java/com/example/demo/
│
├── domain/
│   ├── User.java
│   ├── Friendship.java
│   ├── FriendshipStatus.java       (PENDING / ACCEPTED)
│   ├── ChatRoom.java
│   ├── ChatRoomType.java           (DIRECT / GROUP)
│   ├── ChatRoomMember.java
│   └── Message.java
│
├── repository/
│   ├── UserRepository.java
│   ├── FriendshipRepository.java
│   ├── ChatRoomRepository.java
│   ├── ChatRoomMemberRepository.java
│   ├── MessageRepository.java
│   └── jpa/
│       ├── SpringDataUserRepository.java
│       ├── JpaUserRepository.java
│       ├── SpringDataFriendshipRepository.java
│       ├── JpaFriendshipRepository.java
│       ├── SpringDataChatRoomRepository.java
│       ├── JpaChatRoomRepository.java
│       ├── SpringDataChatRoomMemberRepository.java
│       ├── JpaChatRoomMemberRepository.java
│       ├── SpringDataMessageRepository.java
│       └── JpaMessageRepository.java
│
├── service/
│   ├── UserService.java / UserService1.java
│   ├── FriendService.java / FriendService1.java
│   └── ChatService.java / ChatService1.java
│
├── web/
│   ├── UserController.java         (회원가입, 로그인)
│   ├── FriendController.java       (친구 요청/수락/거절/목록)
│   ├── ChatController.java         (STOMP 메시지 처리)
│   ├── ChatRestController.java     (채팅방 생성/목록/이전 메시지)
│   └── dto/
│       ├── UserJoinRequest.java
│       ├── UserLoginRequest.java
│       ├── LoginResponse.java
│       ├── UserResponse.java
│       ├── FriendRequestDto.java
│       ├── FriendResponse.java
│       ├── FriendRequestResponse.java
│       ├── MessageDto.java
│       ├── MessageResponse.java
│       ├── ChatRoomResponse.java
│       └── CreateChatRoomRequest.java
│
└── config/
    ├── PasswordEncoderConfig.java
    ├── JwtConfig.java              (토큰 발급 / 검증)
    ├── JwtFilter.java              (HTTP 요청 JWT 인증)
    ├── WebSocketConfig.java        (STOMP 설정)
    ├── StompAuthInterceptor.java   (WebSocket JWT 인증)
    └── WebConfig.java              (CORS 설정)
```

---

## 인증 흐름

```
HTTP 요청  → JwtFilter         → Authorization 헤더 검증
WebSocket  → StompAuthInterceptor → CONNECT 헤더 검증

공개 경로 (토큰 불필요):
  POST /api/join
  POST /api/login
  POST /api/idCheck
```