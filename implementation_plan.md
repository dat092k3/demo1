# Nâng cấp Hệ thống: Redis Cache + Message Queue + Elasticsearch

## Phân tích hiện trạng

Hệ thống hiện tại là **Spring Boot Book Reading Platform** với kiến trúc monolithic đồng bộ:

| Thành phần | Hiện trạng | Vấn đề hiệu năng |
|---|---|---|
| **Database** | MySQL trực tiếp qua JPA | Mọi request đều hit DB, không cache |
| **Tìm kiếm** | Không có | Thiếu full-text search |
| **Xử lý** | Đồng bộ hoàn toàn | `autoSaveReadingProgress()` block response |
| **View count** | Chưa có | Chưa tracking lượt xem |

### Bottleneck cụ thể trong code

1. [BookService.getAllBooks()](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/BookService.java#L41-L58) — `findAll()` mỗi request, load toàn bộ
2. [ReadingService.getChapterContent()](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java#L139-L167) — Đồng bộ gọi `autoSaveReadingProgress()`, block user
3. [ReadingService.getBookChapters()](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java#L110-L137) — Query mỗi lần, không cache
4. Không có search — Không thể tìm kiếm sách

---

## User Review Required

> [!IMPORTANT]
> **Message Queue**: Đề xuất **RabbitMQ** (Spring AMQP tích hợp tốt). Nếu muốn **Kafka** (event streaming lớn), cho tôi biết.

> [!IMPORTANT]
> **Docker**: Redis, RabbitMQ, Elasticsearch cần Docker Desktop. Bạn đã cài chưa?

> [!WARNING]
> **Elasticsearch 8.x** yêu cầu Java 17+ (dự án đã đáp ứng).

## Open Questions

> [!IMPORTANT]
> 1. **View Count**: Đếm theo **Book** level hay cả **Chapter** level?
> 2. **Cache TTL**: Book list = 10 phút, Book detail = 30 phút, Chapter list = 15 phút — OK?
> 3. **Search Scope**: Tìm theo title, description, author name, chapter title, chapter content?

---

## Kiến trúc đề xuất

```
Client → Controller → Redis Cache (hit?) → Service → MySQL
                                    ↓
                              RabbitMQ (async)
                           ↙        ↓        ↘
                    View Count   Progress   ES Index
                    Consumer     Consumer   Consumer
                       ↓            ↓          ↓
                     Redis        MySQL    Elasticsearch
                   (counter)              (search index)
                       ↓
                 Scheduler (5min batch sync → MySQL)
```

---

## Proposed Changes — 4 Phases

### Phase 1: Infrastructure Setup

#### [NEW] docker-compose.yml
- Redis 7 (port 6379), RabbitMQ 3 + Management (5672, 15672), Elasticsearch 8 (9200)

#### [MODIFY] [pom.xml](file:///d:/FS/Hibernate/demo1/pom.xml)
- Thêm: `spring-boot-starter-data-redis`, `spring-boot-starter-amqp`, `spring-boot-starter-data-elasticsearch`

#### [MODIFY] [application.properties](file:///d:/FS/Hibernate/demo1/src/main/resources/application.properties)
- Redis, RabbitMQ, Elasticsearch connection config

---

### Phase 2: Redis Cache — Giảm tải DB Read

#### [NEW] config/RedisConfig.java
- `RedisCacheManager` custom TTL, `RedisTemplate` cho view count, JSON serializer

#### [MODIFY] [BookService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/BookService.java)
- `getAllBooks()` → `@Cacheable("books")`
- `getBookById()` → `@Cacheable(value="book", key="#id")`
- Write methods → `@CacheEvict`

#### [MODIFY] [AuthorService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/AuthorService.java)
- `@Cacheable("authors")` + `@CacheEvict`

#### [MODIFY] [CategoryService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/CategoryService.java)
- `@Cacheable("categories")` + `@CacheEvict`

#### [MODIFY] [ReadingService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ReadingService.java)
- Cache chapters list, favorites

#### [MODIFY] [ChapterService.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/service/ChapterService.java)
- `@CacheEvict` trên write methods

---

### Phase 3: RabbitMQ — Xử lý bất đồng bộ

#### [NEW] config/RabbitMQConfig.java
- Exchange, Queue, Binding cho 3 queues: view-count, reading-progress, search-index

#### [NEW] dto/message/ (3 files)
- `ViewCountMessage` (bookId, chapterId, userId, timestamp)
- `ReadingProgressMessage` (userId, bookId, chapterId, timestamp)
- `SearchIndexMessage` (entityType, entityId, action)

#### [NEW] messaging/MessagePublisher.java
- Publish messages to RabbitMQ queues

#### [NEW] messaging/ViewCountConsumer.java
- Tăng Redis counter (INCR), cực nhanh, không hit DB

#### [NEW] messaging/ReadingProgressConsumer.java
- Save reading progress vào DB bất đồng bộ

#### [NEW] messaging/SearchIndexConsumer.java
- Index/delete documents trong Elasticsearch

#### [NEW] scheduler/ViewCountSyncScheduler.java
- `@Scheduled` mỗi 5 phút: Redis view counts → batch update MySQL

#### [MODIFY] [Book.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/entity/Book.java)
- Thêm field `viewCount`

#### [MODIFY] [Chapter.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/entity/Chapter.java)
- Thêm field `viewCount`

#### [MODIFY] ReadingService.java
- `getChapterContent()`: Thay sync `autoSaveReadingProgress()` → async message
- Publish view count message

#### [MODIFY] BookService.java, ChapterService.java
- Publish search index messages khi create/update/delete

#### [MODIFY] [BookDTO.java](file:///d:/FS/Hibernate/demo1/src/main/java/com/example/demo/dto/BookDTO.java)
- Thêm `viewCount` field

---

### Phase 4: Elasticsearch — Full-text Search

#### [NEW] config/ElasticsearchConfig.java
- ES Client, Vietnamese analyzer (ICU)

#### [NEW] document/BookDocument.java
- `@Document(indexName = "books")`: id, title, description, authorName, categoryName

#### [NEW] document/ChapterDocument.java
- `@Document(indexName = "chapters")`: id, title, content, bookId, bookTitle

#### [NEW] repository/BookSearchRepository.java, ChapterSearchRepository.java
- `ElasticsearchRepository` interfaces

#### [NEW] service/SearchService.java
- `searchBooks(keyword, pageable)`, `searchChapters(keyword, pageable)`, `reindexAll()`

#### [NEW] controller/SearchController.java
- `GET /api/search/books?q=`, `GET /api/search/chapters?q=`, `POST /api/search/reindex`

#### [NEW] dto/SearchResultDTO.java
- Search results với highlight, score, pagination

#### [NEW] service/DataMigrationService.java
- One-time migration MySQL → Elasticsearch

---

## Tổng hợp

|   Loại     | Số lượng |
|------------|----------|
| Files mới  | ~20      |
| Files sửa  | ~10      |

## Verification Plan

### Automated
```bash
./mvnw clean compile
./mvnw test
docker-compose up -d && docker-compose ps
```

### Manual
1. **Redis**: Gọi GET 2 lần → lần 2 nhanh hơn, `redis-cli KEYS *` confirm cache
2. **RabbitMQ**: Đọc chapter → response nhanh, RabbitMQ UI có message, view count tăng trong Redis
3. **Elasticsearch**: Reindex → search books → verify results

review plan và trả lời các câu hỏi:

Message Queue: RabbitMQ hay Kafka?
Docker: Bạn đã cài Docker Desktop chưa?
View Count: Đếm lượt xem theo Book level hay cả Chapter level?
Cache TTL: Book list = 10p, Book detail = 30p, Chapter list = 15p — OK?
Search Scope: Tìm kiếm theo những field nào?
