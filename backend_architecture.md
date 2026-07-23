# Kiến trúc Hệ thống Backend Đọc Truyện (System Architecture & Solutions)

Tài liệu này ghi lại bức tranh tổng thể về cách hệ thống Backend (Spring Boot) được thiết kế, cùng với các vấn đề kỹ thuật chuyên sâu và những giải pháp đã được áp dụng để đảm bảo tính **Bảo mật (Security)**, **Hiệu suất (Performance)** và **Khả năng mở rộng (Scalability)**.

---

## 1. Thiết kế Hệ thống Tổng quan (System Overview)

Hệ thống tuân theo mô hình **Kiến trúc Nguyên khối linh hoạt (Modular Monolith)** với các thành phần chính:

1. **Spring Boot (Core):** Đóng vai trò là máy chủ xử lý API.
2. **Spring Security & JWT:** Xử lý xác thực không trạng thái (Stateless Authentication) và Phân quyền (RBAC). Chỉ cho phép Admin và Người đăng truyện sửa/xóa chương.
3. **MySQL (Database):** Lưu trữ metadata (Người dùng, Thông tin sách, Danh sách chương, Tiến độ đọc). Sử dụng Hibernate/JPA.
4. **Local File Storage:** Tách biệt dữ liệu chữ (nội dung chương) ra khỏi Database, lưu trữ dưới dạng file văn bản (.txt) nhằm tối ưu hóa chi phí lưu trữ cơ sở dữ liệu.
5. **Redis (In-Memory Cache):** Bộ nhớ đệm tốc độ cao nhằm giảm tải đọc (Read-heavy) cho Database khi có hàng ngàn User vào đọc truyện.
6. **RabbitMQ (Message Broker):** Xử lý luồng dữ liệu bất đồng bộ (Async Event-Driven). Tránh việc tính lượt view hoặc đồng bộ tiến độ đọc làm chậm thời gian phản hồi API.
7. **Elasticsearch:** (Tương lai) Dùng cho Search Engine.

---

## 2. Các vấn đề Bảo mật và Giải pháp (File Security - OWASP)

Khi cho phép User tương tác (tải lên/đọc) file, hệ thống đối mặt với nhiều rủi ro bảo mật. Các giải pháp sau đã được áp dụng:

### Vấn đề 1: Path Traversal (Xuyên thủng thư mục)
- **Tình trạng:** Hacker truyền tên file có dạng `../../../etc/passwd` để xóa hoặc đọc file của hệ điều hành.
- **Giải pháp:** 
  1. Không sử dụng tên file do client gửi lên. Đổi tên toàn bộ bằng `UUID`.
  2. Bổ sung **Path Canonicalization**: Chuẩn hóa mọi đường dẫn bằng `normalize()` và chặn đứng bằng `!filePath.startsWith(fileStorageLocation)`.

### Vấn đề 2: Tải file mã độc (Malicious File Upload)
- **Tình trạng:** Hacker tải file nhị phân (`.exe`, `.php`, hình ảnh chứa mã độc) thay vì text truyện. Khi hàm `Files.readString()` cố đọc nó, server có thể bị sập do `MalformedInputException`.
- **Giải pháp:** Áp dụng **Deep Content Validation**. Bắt buộc kiểm tra MIME Type `file.getContentType().startsWith("text/plain")` ngay từ tầng Service.

### Vấn đề 3: Tấn công từ chối dịch vụ (DoS bằng File Rác)
- **Tình trạng:** Bắn liên tục các file văn bản nặng hàng GB làm cạn kiệt băng thông mạng và sập RAM (OOM) khi lưu trữ/đọc.
- **Giải pháp:** Thiết lập **Resource Constraints** cứng từ tầng Servlet (`application.properties`): chặn `max-file-size=5MB` và `max-request-size=10MB`.

---

## 3. Các vấn đề Bộ nhớ và Giải pháp (Cache Architecture)

Hệ thống đọc truyện có đặc thù là Read-heavy (Tỷ lệ Đọc áp đảo Ghi). Nếu thiết kế Cache không cẩn thận, hệ thống sẽ sập dây chuyền.

### Vấn đề 1: Cache Stampede (Bầy đàn / Thundering Herd)
- **Tình trạng:** Một bộ truyện nổi tiếng hết hạn Cache (TTL = 0). Ngay lập tức, 1000 request ập đến. Vì Cache đang trống, cả 1000 request cùng lao xuống truy vấn Database, làm Database quá tải và sập lập tức.
- **Giải pháp 1 - Local Synchronization (Mutex Lock):** Áp dụng cấu hình `sync = true` cho các hàm `@Cacheable`. Khi Cache trống, chỉ **1 Thread** duy nhất được cấp quyền đi xuống DB. 999 Thread còn lại bị khóa và đứng chờ Thread 1 mang dữ liệu về cập nhật vào Cache.
- **Giải pháp 2 - Jitter TTL (Thời gian sống ngẫu nhiên):** Để tránh việc 1000 chương truyện cùng hết hạn vào cùng 1 tích tắc, chúng ta đã khởi tạo class `JitterRedisCacheWriter`. Mỗi khi 1 Key được đưa vào Cache, hệ thống sẽ tự động cộng thêm từ 1 - 5 phút ngẫu nhiên.

### Vấn đề 2: Cạn kiệt RAM Redis (Memory Exhaustion)
- **Tình trạng:** Lưu hàng triệu chương truyện chữ thô (JSON String) vào Redis sẽ cực kỳ tốn RAM (vì dung lượng lưu trong RAM rất đắt đỏ).
- **Giải pháp:** Triển khai **Zstandard (Zstd) Data Compression**.
  - Đã tích hợp thư viện `zstd-jni` và viết màng bọc `ZstdRedisSerializer`.
  - Mọi luồng dữ liệu trước khi vào Redis sẽ bị nén (thu nhỏ ~70-80% thể tích).
  - Chọn thuật toán `Zstd` của Facebook thay vì `GZIP` hay `LZ4` vì Zstd có tốc độ giải nén chớp nhoáng (phù hợp với Cache Hit) nhưng vẫn giữ được tỷ lệ nén tuyệt vời giúp cứu sống tài nguyên RAM của hệ thống.
