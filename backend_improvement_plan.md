# Kế hoạch Nâng cấp Kiến trúc Backend (Bảo mật & Cache)

Bản kế hoạch này mô tả các bước triển khai nhằm áp dụng các tiêu chuẩn thiết kế hệ thống tiên tiến (System Design) và bảo mật (OWASP) vào dự án đọc truyện. 

## Mục tiêu
- **Bảo mật File:** Đóng các lỗ hổng tải file nhị phân mã độc, chặn Path Traversal và ngăn chặn tấn công từ chối dịch vụ (DoS) qua file rác.
- **Tối ưu Cache:** Tăng tính ổn định của Redis, ngăn chặn các hiện tượng sập server dây chuyền (Cache Stampede, Cache Penetration, Thundering Herd).
- **Tiết kiệm Bộ nhớ (Memory Optimization):** Áp dụng thuật toán nén để giảm tải dung lượng RAM cho cụm Redis.

## Mục tiêu
- **Bảo mật File:** Đóng các lỗ hổng tải file nhị phân mã độc, chặn Path Traversal và ngăn chặn tấn công từ chối dịch vụ (DoS) qua file rác.
- **Tối ưu Cache:** Tăng tính ổn định của Redis, ngăn chặn các hiện tượng sập server dây chuyền (Cache Stampede, Cache Penetration, Thundering Herd).
- **Tiết kiệm Bộ nhớ (Memory Optimization):** Áp dụng thuật toán nén **Zstandard (Zstd)** để giảm tải cực đại dung lượng RAM nhưng vẫn đảm bảo tốc độ giải nén chớp nhoáng.

> [!NOTE]
> **User Review Approved**
> - **Chốt phương án Zstd:** Đã chốt sử dụng thuật toán nén Zstandard (Zstd) cho hệ thống Cache.
> - **Jitter TTL:** Đồng ý triển khai Custom `RedisCacheManager` để xử lý Random TTL chống Cache Stampede.

---

## Proposed Changes

### 1. Bảo mật Xử lý File (OWASP Guidelines)

#### [MODIFY] `application.properties`
- **Resource Constraints (Chống DoS):** Cấu hình giới hạn kích thước file cứng từ tầng Servlet để chặn đứng các payload quá lớn trước khi vào RAM.
```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

#### [MODIFY] `FileStorageService.java`
- **Deep Content Validation (Magic Bytes/Allowlisting):** Sửa hàm `saveFile`. Bắt buộc kiểm tra `file.getContentType()` phải là `text/plain` hoặc đọc thử các byte đầu tiên để xác nhận đây không phải là file thực thi/nhị phân.
- **Path Canonicalization:** Thêm cơ chế chặn đọc file ngoài luồng trong hàm `readFile` và `deleteFile`. 
```java
Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
if (!filePath.startsWith(this.fileStorageLocation)) {
    throw new SecurityException("Xâm phạm thư mục trái phép!");
}
```

---

### 2. Kiến trúc Cache In-Memory (Alex Xu & Martin Kleppmann)

#### [MODIFY] `RedisConfig.java`
- **Chống Cache Stampede (Jitter TTL):** Nâng cấp `RedisCacheManager` hiện tại bằng cách custom lại `RedisCacheConfiguration`. Nếu dùng `@Cacheable`, ta sẽ bọc CacheManager lại để mỗi lần tạo một Cache Entry, nó sẽ cộng thêm một khoảng Random(1-5 phút) vào TTL chuẩn.
- **Chống Cache Penetration:** Đảm bảo `cacheDefaults` không bị tắt tính năng `disableCachingNullValues()`, cho phép hệ thống lưu trữ cả các kết quả rỗng (Null/Empty List) để không bị đục thủng xuống DB khi user request ID ảo.
- **Nén dữ liệu Cache (Data Compression):** Thay vì lưu chuỗi JSON thô vào Redis, cấu hình thêm màng bọc nén `ZstdRedisSerializer` (dựa trên thuật toán **Zstandard** của Facebook) đè lên `GenericJackson2JsonRedisSerializer`.
  - *Lý thuyết:* Zstd cung cấp tỷ lệ nén cao tương đương GZIP (~70-80% cho văn bản chữ) nhưng có tốc độ giải nén chớp nhoáng ngang ngửa LZ4. Việc này giúp tiết kiệm 80% RAM Redis mà không làm sập CPU Backend mỗi khi có hàng ngàn User vào đọc truyện (Cache Hit).
  - *Triển khai:* Bổ sung thư viện `zstd-jni` vào `pom.xml`.

#### [MODIFY] `ReadingService.java` & `ChapterService.java`
- **Local Synchronization (Mutex Lock):** Cập nhật toàn bộ các Annotation `@Cacheable` để thêm thuộc tính `sync = true`.
Ví dụ: `@Cacheable(value = "chapters", key = "#bookId", sync = true)`
*Tác dụng:* Khi 1000 người cùng bấm vào một bộ truyện vừa hết hạn Cache, chỉ có 1 luồng (Thread) duy nhất được phép đi xuống Database để lấy dữ liệu, 999 luồng còn lại sẽ bị khóa (Lock) và đứng chờ lấy dữ liệu từ luồng đầu tiên, giúp DB không bị sập.

---

## Verification Plan

### Automated/Manual Tests
- **Security Test:** Gửi lên một file ảnh (`.jpg` hoặc `.exe`) thông qua Postman vào API `/add-to-book` để đảm bảo hệ thống quăng lỗi 400 (Bad Request).
- **Security Test:** Gửi một file lớn > 5MB để kiểm tra Spring có chặn ngay lập tức bằng lỗi `MaxUploadSizeExceededException` không.
- **Cache Jitter Test:** Quan sát các Key được tạo ra trong Redis CLI (`TTL <key>`) xem thời gian sống có bị lệch nhau ngẫu nhiên hay không.
- **Cache Sync Test:** Gửi 100 request đồng thời (bằng JMeter hoặc Apache Benchmark) vào API lấy danh sách truyện và xem log xem Database có bị hit 100 lần không (Nếu đúng, DB chỉ bị hit 1 lần).
