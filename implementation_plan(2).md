# Nâng cấp: Chống Crawl + WebSocket Realtime + Recommendation Engine

## Phân tích hiện trạng & Lỗ hổng

### 1. Bảo vệ nội dung truyện — KHÔNG CÓ

| Lỗ hổng | Vị trí | Mức độ |
|---|---|---|
| Không Rate Limiting | Toàn bộ API — đặc biệt [getChapterContent()](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java#L139-L167) | 🔴 Nghiêm trọng |
| Content trả về plaintext | Chapter content = raw text, dễ crawl hàng loạt | 🔴 Nghiêm trọng |
| Không detect bot | [JwtAuthenticationFilter](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/security/JwtAuthenticationFilter.java) chỉ check JWT, không detect pattern bất thường | 🟡 Cao |
| Không watermark | Không tracking ai copy content | 🟡 Cao |
| Không CAPTCHA | Không challenge khi nghi ngờ bot | 🟡 Cao |

### 2. Notification — KHÔNG CÓ
- Hệ thống hoàn toàn **pull-based** (client phải tự gọi API)
- Không có cách thông báo realtime khi: sách mới, chapter mới, admin announce

### 3. Đề xuất truyện — KHÔNG CÓ
- [UserReadingProgress](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/entity/UserReadingProgress.java) + [UserFavoriteBook](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/entity/UserFavoriteBook.java) đã tracking behavior nhưng **không sử dụng** cho recommendation
- Dữ liệu có sẵn: lịch sử đọc (user-book-chapter), danh sách yêu thích (user-book), category mỗi book — đủ để xây recommendation

---

## User Review Required

> [!IMPORTANT]
> **Rate Limiting**: Đề xuất dùng **Bucket4j** (in-memory, nhẹ, tích hợp Spring Boot tốt). Nếu scale multi-instance, cần kết hợp Redis backend cho Bucket4j. Bạn chạy single instance hay multi-instance?

> [!IMPORTANT]
> **CAPTCHA Provider**: Đề xuất tích hợp **Google reCAPTCHA v3** (invisible, không làm phiền user bình thường). Bạn OK với Google reCAPTCHA hay muốn dùng provider khác (hCaptcha, Cloudflare Turnstile)?

> [!WARNING]
> **WebSocket + Spring Security**: Cần cấu hình STOMP interceptor để authenticate WebSocket connections bằng JWT hiện có. Điều này sẽ modify [SecurityConfig.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/security/SecurityConfig.java).

## Open Questions

> [!IMPORTANT]
> 1. **Notification Types**: Bạn muốn push những loại notification nào? Đề xuất: Sách mới, Chapter mới, Admin Announcement, Recommendation mới — đúng hết?
> 2. **Recommendation Depth**: Thuật toán đề xuất ở mức nào?
>    - **Option A** (Đề xuất): Content-based + Collaborative filtering đơn giản, chạy trong app
>    - **Option B**: Tích hợp ML service bên ngoài (phức tạp hơn, accuracy cao hơn)
> 3. **Anti-crawl aggressiveness**: Khi phát hiện bot, bạn muốn block IP luôn hay chỉ yêu cầu CAPTCHA + rate limit?

---

## Kiến trúc đề xuất

```
                                    ┌─────────────────────────────────────┐
                                    │         CLIENT (Browser)            │
                                    │  HTTP REST ←→  WebSocket (STOMP)    │
                                    └──────┬────────────────┬─────────────┘
                                           │                │
                              ┌────────────▼──────┐   ┌─────▼──────────────┐
                              │  Anti-Crawl Layer │   │  WebSocket Server  │
                              │  ┌──────────────┐ │   │  (STOMP Broker)    │
                              │  │ Rate Limiter │ │   │                    │
                              │  │ (Bucket4j)   │ │   │  /topic/books-new  │
                              │  ├──────────────┤ │   │  /topic/chapters   │
                              │  │ Fingerprint  │ │   │  /user/queue/notif │
                              │  │ Detector     │ │   │  /topic/announce   │
                              │  ├──────────────┤ │   └─────▲──────────────┘
                              │  │ CAPTCHA Gate │ │         │
                              │  ├──────────────┤ │         │ push
                              │  │ Honeypot Trap│ │         │
                              │  └──────────────┘ │   ┌─────┴──────────────┐
                              └────────┬───────────┘  │NotificationService │
                                       │              └─────▲──────────────┘
                              ┌────────▼───────────┐        │
                              │   Controllers      │────────┘
                              │   (REST API)       │    trigger on events
                              └────────┬───────────┘
                                       │
                              ┌────────▼───────────┐
                              │ RecommendationSvc  │
                              │                    │
                              │ Content-Based:     │
                              │  Same Category     │
                              │  Same Author       │
                              │                    │
                              │ Collaborative:     │
                              │  Users who read X  │
                              │  also read Y       │
                              │                    │
                              │ Scoring + Ranking  │
                              └────────┬───────────┘
                                       │
                              ┌────────▼───────────┐
                              │     MySQL DB       │
                              │ UserReadingProgress│
                              │ UserFavoriteBook   │
                              │ UserActivity (NEW) │
                              └────────────────────┘
```

---

## Proposed Changes — 3 Phases

---

### Phase 1: Chống Crawl Truyện Nâng Cao

**Mục tiêu**: Bảo vệ nội dung chapter khỏi bị bot crawl hàng loạt, đồng thời không ảnh hưởng trải nghiệm user thật.

---

#### [MODIFY] [pom.xml](file:///d:/FS/Hibernate/demo1/pom.xml)
Thêm dependencies:
```xml
<!-- Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>

<!-- Google reCAPTCHA verification (RestTemplate call) -->
<!-- Không cần thêm dependency, dùng RestTemplate có sẵn -->
```

#### [MODIFY] [application.properties](file:///d:/FS/Hibernate/demo1/src/main/resources/application.properties)
```properties
# Anti-Crawl Configuration
anticrawl.rate-limit.chapter-reads-per-minute=30
anticrawl.rate-limit.api-calls-per-minute=100
anticrawl.fingerprint.suspicious-threshold=50
anticrawl.captcha.enabled=true
anticrawl.captcha.site-key=YOUR_RECAPTCHA_SITE_KEY
anticrawl.captcha.secret-key=YOUR_RECAPTCHA_SECRET_KEY
anticrawl.honeypot.enabled=true
```

#### [NEW] `config/AntiCrawlProperties.java`
- `@ConfigurationProperties(prefix = "anticrawl")` — typesafe config binding
- Chứa tất cả threshold, rate limit values

#### [NEW] `anticrawl/RateLimitingFilter.java`
- Extends `OncePerRequestFilter`, đăng ký **trước** JwtAuthenticationFilter
- **Per-IP Rate Limiting**: Bucket4j tạo bucket cho mỗi IP, giới hạn request/phút
- **Per-User Rate Limiting**: Sau khi authenticate, giới hạn theo userId (chặn bot dùng nhiều IP cùng 1 account)
- **Endpoint-specific**: `/api/reading/chapter/*/content` có limit riêng (thấp hơn), vì đây là target chính của crawler
- Response `429 Too Many Requests` kèm `Retry-After` header

#### [NEW] `anticrawl/CrawlDetectorService.java`
- **Fingerprint Analysis**: Tổng hợp User-Agent, Accept-Language, Accept-Encoding, screen resolution (header), referrer
- **Behavior Pattern Detection**:
  - Đọc quá nhanh (< 3 giây giữa 2 chapter requests)
  - Đọc tuần tự toàn bộ chapters (chapter 1→2→3→...→N liên tục)
  - Không có mouse/scroll events (detect headless browser)
  - Request không có referrer (gọi API trực tiếp)
- **Scoring**: Mỗi behavior bất thường cộng điểm → vượt threshold → flag as suspicious
- Lưu suspicious score per IP/User vào in-memory `ConcurrentHashMap` (hoặc Redis nếu multi-instance)

#### [NEW] `anticrawl/CaptchaVerificationService.java`
- Gọi Google reCAPTCHA verify API
- Method `verifyCaptcha(String captchaToken)` → true/false
- Khi user bị flag suspicious → bắt buộc gửi captcha token trong header

#### [NEW] `anticrawl/ContentProtectionService.java`
- **Invisible Watermark**: Chèn zero-width characters (Unicode U+200B, U+200C) vào content trả về, encode userId → nếu content bị leak, trace được người copy
- **Content Chunking**: Không trả toàn bộ chapter content 1 lần, chia thành pages (mỗi page ~2000 ký tự), client phải request từng page
- **Dynamic Token**: Mỗi content response kèm 1 short-lived token, client phải gửi token này khi request page tiếp theo → bot không crawl song song được

#### [NEW] `anticrawl/HoneypotController.java`
- Tạo fake endpoints: `/api/reading/chapter/download/{id}`, `/api/books/export/{id}`
- Trả về fake content (hoặc delay 30s)
- Bất kỳ request nào vào honeypot → **auto-ban IP 24 giờ** + log alert
- Các link này được "giấu" trong HTML response (CSS `display:none`) — chỉ bot mới follow

#### [NEW] `anticrawl/BannedIpStore.java`
- In-memory store (`ConcurrentHashMap<String, Instant>`) cho banned IPs
- Auto-expire sau 24 giờ
- Check trong `RateLimitingFilter`

#### [NEW] `entity/UserActivity.java`
- Entity mới ghi log hoạt động chi tiết:
  - `userId`, `ipAddress`, `userAgent`, `endpoint`, `timestamp`, `suspiciousScore`
- Phục vụ cả Anti-Crawl (detect pattern) và Recommendation (behavior tracking)

#### [NEW] `repository/UserActivityRepository.java`
- JPA Repository cho UserActivity
- Custom queries: `findByUserIdOrderByTimestampDesc`, `countByIpAddressAndTimestampAfter`

#### [MODIFY] [SecurityConfig.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/security/SecurityConfig.java)
- Thêm `RateLimitingFilter` vào filter chain (trước JwtAuthenticationFilter)
- Permit honeypot endpoints
- Thêm WebSocket endpoints vào security config (chuẩn bị cho Phase 2)

#### [MODIFY] [ReadingService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java)
- `getChapterContent()`:
  - Gọi `ContentProtectionService.watermark(content, userId)` trước khi trả về
  - Gọi `CrawlDetectorService.recordAccess()` để track behavior
  - Hỗ trợ pagination (page param)

#### [MODIFY] [ReadingController.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/controller/ReadingController.java)
- `getChapterContent()`: Thêm optional `captchaToken` header, `page` param
- Thêm `@RequestHeader` cho fingerprint data

---

### Phase 2: WebSocket Notification Realtime

**Mục tiêu**: Push notification tức thời tới user khi có sách mới, chapter mới, thông báo hệ thống.

---

#### [MODIFY] [pom.xml](file:///d:/FS/Hibernate/demo1/pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

#### [NEW] `config/WebSocketConfig.java`
- `@EnableWebSocketMessageBroker`
- STOMP endpoint: `/ws` (với SockJS fallback)
- Application destination prefix: `/app`
- Broker destinations:
  - `/topic/books` — broadcast sách mới (all users)
  - `/topic/chapters/{bookId}` — broadcast chapter mới cho followers của book
  - `/topic/announcements` — admin announcements
  - `/user/queue/notifications` — personal notification cho từng user
  - `/user/queue/recommendations` — recommendation push cho user

#### [NEW] `config/WebSocketSecurityConfig.java`
- Intercept STOMP CONNECT frame → extract JWT từ header → authenticate
- Chỉ authenticated users mới subscribe được `/user/**` channels
- `/topic/**` cho phép cả authenticated và anonymous (public notifications)

#### [NEW] `entity/Notification.java`
- Entity mới persist notifications:
  - `id`, `userId` (nullable — null = broadcast), `type` (enum), `title`, `message`, `relatedEntityId`, `relatedEntityType`, `isRead`, `createdAt`
- Để user xem lại notifications đã miss khi offline

#### [NEW] `enums/NotificationType.java`
```java
enum NotificationType {
    NEW_BOOK,           // Sách mới được upload
    NEW_CHAPTER,        // Chapter mới cho sách user đang theo dõi
    ADMIN_ANNOUNCEMENT, // Thông báo từ admin
    RECOMMENDATION,     // Đề xuất truyện mới
    SYSTEM              // Thông báo hệ thống (maintenance, etc.)
}
```

#### [NEW] `dto/NotificationDTO.java`
- DTO cho notification: `id`, `type`, `title`, `message`, `relatedEntityId`, `isRead`, `createdAt`

#### [NEW] `repository/NotificationRepository.java`
- `findByUserIdAndIsReadFalseOrderByCreatedAtDesc` — unread notifications
- `findByUserIdOrderByCreatedAtDesc` — all notifications (paginated)
- `countByUserIdAndIsReadFalse` — unread count

#### [NEW] `service/NotificationService.java`
- Core notification logic:
  - `sendToUser(userId, notification)` → save to DB + push qua WebSocket `/user/queue/notifications`
  - `broadcast(notification)` → save to DB (userId=null) + push qua `/topic/announcements`
  - `notifyNewBook(book)` → broadcast to `/topic/books`
  - `notifyNewChapter(bookId, chapter)` → push to followers of that book
  - `markAsRead(notificationId, userId)`
  - `getUnreadCount(userId)`
  - `getUserNotifications(userId, pageable)`
- Sử dụng `SimpMessagingTemplate` để push messages

#### [NEW] `controller/NotificationController.java`
- REST API bổ sung (cho khi user reconnect, load history):
  - `GET /api/notifications` — danh sách notification (paginated)
  - `GET /api/notifications/unread-count` — số notification chưa đọc
  - `PUT /api/notifications/{id}/read` — đánh dấu đã đọc
  - `PUT /api/notifications/read-all` — đánh dấu tất cả đã đọc

#### [MODIFY] [BookService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/BookService.java)
- `createBook()`: Sau khi save → gọi `notificationService.notifyNewBook(savedBook)`
- `toggleBookVisibility()`: Khi book chuyển sang public → trigger notification

#### [MODIFY] [ChapterService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ChapterService.java)
- `createChapter()`: Sau khi save → gọi `notificationService.notifyNewChapter(bookId, savedChapter)`

#### [MODIFY] [SecurityConfig.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/security/SecurityConfig.java)
- Permit WebSocket handshake endpoint `/ws/**`
- Thêm notification API endpoints vào security rules

---

### Phase 3: Hệ thống Đề xuất Truyện (Recommendation Engine)

**Mục tiêu**: Đề xuất truyện phù hợp dựa trên lịch sử đọc, sở thích, và hành vi của users tương tự.

---

#### Thuật toán đề xuất (2 chiến lược kết hợp)

**Strategy 1 — Content-Based Filtering:**
```
Input:  User đã đọc Books [B1(category=Fiction), B2(category=Fiction), B3(category=Science)]
Logic:  Đếm category frequency → Fiction=2, Science=1
        Tìm books chưa đọc thuộc category Fiction (ưu tiên) > Science
        Bonus: cùng Author với sách đã đọc
Output: Ranked list of recommended books
```

**Strategy 2 — Collaborative Filtering (User-based):**
```
Input:  User A đã đọc [B1, B2, B3]
Logic:  Tìm users khác cũng đọc B1, B2 → User B đọc [B1, B2, B4, B5]
        B4, B5 là candidates (User A chưa đọc nhưng users tương tự đã đọc)
        Score = số users tương tự đã đọc book đó
Output: Ranked list by collaborative score
```

**Final Score = 0.6 × Content Score + 0.4 × Collaborative Score** (có thể tune)

---

#### [NEW] `recommendation/RecommendationService.java`
- **Core engine** tính recommendation:
  - `getRecommendations(userId, limit)` → `List<RecommendedBookDTO>`
  - Private methods:
    - `contentBasedScore(userId, candidateBook)` — dựa trên category overlap + author overlap
    - `collaborativeScore(userId, candidateBook)` — dựa trên users tương tự
    - `combineScores(contentScore, collabScore)` — weighted combination
  - **Logic flow**:
    1. Load user's reading history (books đã đọc + favorites)
    2. Extract category frequency map + author set
    3. Find candidate books (chưa đọc, isPublic=true)
    4. Score mỗi candidate theo cả 2 strategy
    5. Sort by final score, return top N

#### [NEW] `recommendation/UserSimilarityService.java`
- Tính **Jaccard Similarity** giữa 2 users dựa trên tập sách đã đọc:
  ```
  Similarity(A,B) = |BooksA ∩ BooksB| / |BooksA ∪ BooksB|
  ```
- Cache kết quả similarity (tính lại mỗi ngày vì cost cao)
- `findSimilarUsers(userId, minSimilarity, limit)` → top N users tương tự

#### [NEW] `dto/RecommendedBookDTO.java`
- DTO kết quả đề xuất:
  - `bookId`, `title`, `description`, `authorName`, `categoryName`
  - `score` (0.0 → 1.0), `reason` (String mô tả lý do đề xuất, VD: "Vì bạn đã đọc Fiction")
  - `recommendationType` (CONTENT_BASED / COLLABORATIVE / HYBRID)

#### [NEW] `controller/RecommendationController.java`
- `GET /api/recommendations` — lấy danh sách đề xuất cho authenticated user (mặc định 10 books)
- `GET /api/recommendations?limit=20` — custom limit
- `POST /api/recommendations/refresh` — force recalculate (admin/user trigger)

#### [NEW] `scheduler/RecommendationScheduler.java`
- `@Scheduled(cron = "0 0 2 * * *")` — chạy lúc 2h sáng mỗi ngày
- Pre-compute recommendations cho tất cả active users → lưu vào DB
- Push notification cho users có recommendations mới qua WebSocket (tích hợp Phase 2)

#### [NEW] `entity/UserRecommendation.java`
- Entity lưu pre-computed recommendations:
  - `id`, `userId`, `bookId`, `score`, `reason`, `recommendationType`, `createdAt`, `isViewed`
- Để không phải tính realtime mỗi lần user request

#### [NEW] `repository/UserRecommendationRepository.java`
- `findByUserIdAndIsViewedFalseOrderByScoreDesc` — recommendations chưa xem
- `findByUserIdOrderByScoreDesc(userId, pageable)` — paginated
- `deleteByUserIdAndCreatedAtBefore` — cleanup old recommendations

#### [MODIFY] [UserReadingProgressRepository.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/repository/UserReadingProgressRepository.java)
Thêm queries mới phục vụ recommendation:
```java
// Tất cả books user đã đọc
List<UserReadingProgress> findByUserId(Long userId);

// Tìm users khác cũng đọc cùng books
@Query("SELECT DISTINCT urp.user.id FROM UserReadingProgress urp WHERE urp.book.id IN :bookIds AND urp.user.id != :userId")
List<Long> findUserIdsWhoAlsoRead(@Param("bookIds") List<Long> bookIds, @Param("userId") Long userId);
```

#### [MODIFY] [UserFavoriteBookRepository.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/repository/UserFavoriteBookRepository.java)
Thêm queries:
```java
// Books được yêu thích bởi list of users
@Query("SELECT ufb.book FROM UserFavoriteBook ufb WHERE ufb.user.id IN :userIds AND ufb.book.id NOT IN :excludeBookIds")
List<Book> findFavoriteBooksByUsers(@Param("userIds") List<Long> userIds, @Param("excludeBookIds") List<Long> excludeBookIds);
```

#### [MODIFY] [BookRepository.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/repository/BookRepository.java)
Thêm queries:
```java
// Books by category, exclude already read
List<Book> findByCategoryIdAndIdNotIn(Long categoryId, List<Long> excludeIds);

// Books by author, exclude already read
List<Book> findByAuthorIdAndIdNotIn(Long authorId, List<Long> excludeIds);
```

#### [MODIFY] [ReadingService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java)
- `autoSaveReadingProgress()`: Sau khi save, log vào `UserActivity` để recommendation engine có thêm signal

---

## Tổng hợp Files

### Tạo mới (~22 files)

| # | Phase | File | Mục đích |
|---|---|---|---|
| 1 | 1 | `config/AntiCrawlProperties.java` | Typesafe config cho anti-crawl |
| 2 | 1 | `anticrawl/RateLimitingFilter.java` | Per-IP + Per-User rate limiting |
| 3 | 1 | `anticrawl/CrawlDetectorService.java` | Behavior analysis + fingerprinting |
| 4 | 1 | `anticrawl/CaptchaVerificationService.java` | Google reCAPTCHA verification |
| 5 | 1 | `anticrawl/ContentProtectionService.java` | Watermark + chunking + dynamic token |
| 6 | 1 | `anticrawl/HoneypotController.java` | Fake endpoints trap bots |
| 7 | 1 | `anticrawl/BannedIpStore.java` | In-memory banned IP management |
| 8 | 1 | `entity/UserActivity.java` | Activity logging entity |
| 9 | 1 | `repository/UserActivityRepository.java` | Activity queries |
| 10 | 2 | `config/WebSocketConfig.java` | STOMP + SockJS config |
| 11 | 2 | `config/WebSocketSecurityConfig.java` | JWT auth cho WebSocket |
| 12 | 2 | `entity/Notification.java` | Notification entity |
| 13 | 2 | `enums/NotificationType.java` | Notification type enum |
| 14 | 2 | `dto/NotificationDTO.java` | Notification DTO |
| 15 | 2 | `repository/NotificationRepository.java` | Notification queries |
| 16 | 2 | `service/NotificationService.java` | Core notification + WebSocket push |
| 17 | 2 | `controller/NotificationController.java` | REST API notifications |
| 18 | 3 | `recommendation/RecommendationService.java` | Core recommendation engine |
| 19 | 3 | `recommendation/UserSimilarityService.java` | Jaccard similarity calculation |
| 20 | 3 | `dto/RecommendedBookDTO.java` | Recommendation DTO |
| 21 | 3 | `controller/RecommendationController.java` | REST API recommendations |
| 22 | 3 | `scheduler/RecommendationScheduler.java` | Nightly pre-compute job |
| 23 | 3 | `entity/UserRecommendation.java` | Pre-computed recommendation entity |
| 24 | 3 | `repository/UserRecommendationRepository.java` | Recommendation queries |

### Sửa đổi (~10 files)

| # | File | Thay đổi |
|---|---|---|
| 1 | `pom.xml` | +Bucket4j, +spring-boot-starter-websocket |
| 2 | `application.properties` | +anti-crawl config, +websocket config |
| 3 | `SecurityConfig.java` | +RateLimitingFilter, +WebSocket endpoints, +notification endpoints |
| 4 | `ReadingService.java` | +watermark content, +activity logging, +crawl detection |
| 5 | `ReadingController.java` | +captcha header, +page param |
| 6 | `BookService.java` | +notify on new book |
| 7 | `ChapterService.java` | +notify on new chapter |
| 8 | `UserReadingProgressRepository.java` | +recommendation queries |
| 9 | `UserFavoriteBookRepository.java` | +collaborative filtering queries |
| 10 | `BookRepository.java` | +content-based filtering queries |

---

## Verification Plan

### Automated Tests
```bash
# Build project
./mvnw clean compile

# Run existing tests (ensure nothing breaks)
./mvnw test
```

### Phase 1 — Anti-Crawl Verification
1. **Rate Limit**: Gửi 31 request `/chapter/content` trong 1 phút → request thứ 31 nhận 429
2. **Honeypot**: Gọi `/api/reading/chapter/download/1` → IP bị ban, subsequent requests bị block
3. **Watermark**: Gọi `getChapterContent()` → inspect response, confirm zero-width chars chứa userId
4. **Behavior Detection**: Script gọi chapters 1→2→3→4→5 liên tục mỗi 1 giây → bị flag suspicious

### Phase 2 — WebSocket Verification
1. Kết nối WebSocket tới `ws://localhost:8081/ws` với JWT
2. Subscribe `/topic/books` → tạo book mới → confirm nhận notification
3. Subscribe `/user/queue/notifications` → confirm nhận personal notification
4. Gọi `GET /api/notifications/unread-count` → confirm count chính xác

### Phase 3 — Recommendation Verification
1. Tạo user, đọc vài books thuộc Fiction → `GET /api/recommendations` → confirm trả về Fiction books
2. Tạo 2 users với reading history overlap → confirm collaborative filtering hoạt động
3. Trigger scheduler manually → confirm `UserRecommendation` records được tạo
4. Confirm notification push cho recommendations mới


review và trả lời các câu hỏi mở:

Cần bạn quyết định:

Rate Limiting: Single instance hay multi-instance? (ảnh hưởng chọn Bucket4j in-memory hay Redis-backed)
CAPTCHA Provider: Google reCAPTCHA v3, hCaptcha, hay Cloudflare Turnstile?
Notification Types: Sách mới, Chapter mới, Admin Announcement, Recommendation — đủ chưa?
Recommendation depth: Content-based + Collaborative đơn giản (trong app) hay tích hợp ML service bên ngoài?
Anti-crawl response: Khi phát hiện bot → block IP luôn hay chỉ yêu cầu CAPTCHA?