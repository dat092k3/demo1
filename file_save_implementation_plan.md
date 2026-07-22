# Phase 6: Tích hợp Local File Storage — Lưu nội dung chương thành file cục bộ

## Mô tả

Nâng cấp hệ thống để thay thế việc lưu trữ toàn bộ nội dung văn bản (`content`) khổng lồ của các chương truyện trong MySQL bằng cách **lưu thành các file `.txt` độc lập trên ổ cứng máy chủ**. 
Hệ thống sẽ chỉ lưu lại **đường dẫn file (File Path)** trong Database để giảm tải tối đa cho MySQL (tránh hiện tượng phình to database).

## Phân tích Kiến trúc

### Luồng xử lý (Data Flow)

**Khi tạo/sửa chương (Upload):**
```
User gửi (File .txt hoặc Text) → API 
                                 ↓
                    FileStorageService (Lưu thành file uploads/chapters/book_1_chapter_1.txt)
                                 ↓
                    Lưu đường dẫn file vào MySQL thay vì lưu content
                                 ↓
                    Gửi sự kiện cho Elasticsearch index nội dung để tìm kiếm
```

**Khi đọc chương (Download/View):**
```
User gọi API GET → Lấy đường dẫn file từ MySQL → FileStorageService đọc nội dung từ file → Trả về cho User
```

### Tại sao chọn Local File Storage?
- **Hiệu suất cực cao:** Tốc độ đọc/ghi file trên ổ đĩa nội bộ cực nhanh, không có độ trễ mạng so với dùng Cloud.
- **Giảm tải Database:** MySQL không còn phải chứa hàng triệu dòng text dài, query sẽ siêu nhanh.
- **Bảo toàn dữ liệu:** Có thể dùng Docker Bind Mounts để ánh xạ thư mục `uploads` ra ngoài ổ cứng gốc, không bao giờ lo mất file.

---

## Proposed Changes (Các file cần tạo/sửa)

### Cấu hình (Properties)

#### [MODIFY] `src/main/resources/application.properties`
- Thêm đường dẫn thư mục lưu trữ file:
```properties
# File Storage Config
file.upload-dir=uploads/chapters
```

---

### Service Layer

#### [NEW] `src/main/java/com/example/demo/service/FileStorageService.java`
- Khởi tạo thư mục `uploads/chapters` nếu chưa tồn tại.
- Method `saveFile(Long bookId, Long chapterId, String content / MultipartFile file)`: Ghi nội dung vào file `.txt` và trả về đường dẫn tương đối.
- Method `readFile(String filePath)`: Đọc nội dung file ra String.
- Method `deleteFile(String filePath)`: Xóa file khi chương bị xóa.

---

### API & DTOs

#### [MODIFY] `src/main/java/com/example/demo/controller/ChapterController.java`
- Sửa đổi API `createChapter` và `updateChapter` để hỗ trợ nhận file (Ví dụ: `MultipartFile file`).

#### [MODIFY] `src/main/java/com/example/demo/dto/ChapterRequestDTO.java`
- Bổ sung khả năng linh hoạt: Người dùng có thể truyền `MultipartFile file` (nếu tải file lên) HOẶC chuỗi `content` thông thường (hệ thống sẽ tự động tạo file từ chuỗi này).

---

### Entity & Database

#### [MODIFY] `src/main/java/com/example/demo/entity/Chapter.java`
- **XÓA** cột `content` (LONGTEXT).
- **THÊM** cột `filePath` (String) để lưu đường dẫn file.

---

### Core Service (ChapterService)

#### [MODIFY] `src/main/java/com/example/demo/service/ChapterService.java`
- **Lưu chương:** Đẩy nội dung cho `FileStorageService` ghi ra ổ cứng → Nhận về `filePath` → Lưu `filePath` vào MySQL.
- **Lấy chương (GET):** Khi map từ Entity sang `ChapterDTO`, dùng `FileStorageService.readFile(filePath)` để trả về toàn bộ text cho frontend hiển thị như cũ.
- **Xóa chương:** Xóa file vật lý bằng `FileStorageService.deleteFile()`.
- **Đồng bộ Elasticsearch:** Khi tạo/sửa, vẫn gửi nội dung sang RabbitMQ để Elasticsearch đánh index phục vụ tìm kiếm.

---

## Verification Plan

### Automated Tests
```bash
./mvnw clean compile  # Đảm bảo code biên dịch thành công
./mvnw test           # Đảm bảo không có regression
```

### Manual
1. Tạo một chương mới có nội dung dài.
2. Mở thư mục gốc của project (demo1), kiểm tra xem thư mục `uploads/chapters` có xuất hiện file `.txt` mới không.
3. Kiểm tra MySQL: Xác nhận cột `content` đã mất và thay bằng cột `file_path`.
4. Gọi API lấy thông tin chương: Đảm bảo dữ liệu text vẫn được trả về bình thường (được đọc từ file).
5. Xóa chương: Xác nhận file `.txt` vật lý bị xóa sạch.
