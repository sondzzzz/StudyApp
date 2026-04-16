# StudyApp - Ứng dụng Học tập Thông minh
​
**EduPro** là một ứng dụng di động hỗ trợ học tập toàn diện, được phát triển trên nền tảng Android. Ứng dụng giúp người dùng quản lý tiến độ học tập, tham gia các khóa học, ôn tập kiến thức qua Flashcards và thử thách bản thân với các bài Quiz thú vị.
​
## 🚀 Tính năng chính
​
*   **Hệ thống Khóa học:**
    *   Xem danh sách các khóa học có sẵn từ Firebase (`available_courses`).
    *   Đăng ký khóa học mới và theo dõi tiến độ hoàn thành (phần trăm hoàn thành, bài học hiện tại).
    *   Xem chi tiết danh sách bài học và nội dung từng bài.
*   **Học tập qua Flashcards:** Ôn tập kiến thức nhanh chóng thông qua giao diện thẻ nhớ trực quan.
*   **Thử thách Quiz:** Kiểm tra kiến thức sau mỗi khóa học với hệ thống câu hỏi trắc nghiệm.
*   **Theo dõi mục tiêu (Daily Goals):**
    *   Thiết lập và theo dõi số lượng bài học, flashcards, quiz cần hoàn thành mỗi ngày.
    *   Hệ thống tính điểm kinh nghiệm (XP) và chuỗi ngày học tập (Streak) để tạo động lực.
*   **Quản lý tài khoản:** Đăng ký và đăng nhập an toàn thông qua Firebase Authentication.
*   **Dữ liệu thời gian thực:** Đồng bộ hóa tiến trình học tập trên Cloud Firestore.
    ​
## 🛠 Công nghệ sử dụng
​
*   **Ngôn ngữ:** Java
*   **Framework:** Android SDK
*   **Backend:** Firebase (Authentication, Firestore)
*   **Thư viện hỗ trợ:**
    *   `Google Material Components`: Thiết kế giao diện hiện đại.
    *   `Gson`: Xử lý dữ liệu JSON.
    *   `RecyclerView`: Hiển thị danh sách khóa học và bài học tối ưu.
    *   `Firebase BOM`: Quản lý phiên bản các dịch vụ Firebase.
        ​
## 📂 Cấu trúc dự án

Dự án được cấu trúc theo mô hình chuẩn của Android với ngôn ngữ thuần **Java**:

```text
app/src/main/java/com/example/myapplication
├── models/                # Định nghĩa cấu trúc dữ liệu (POJO)
│   ├── User.java          # Thông tin tài khoản người dùng
│   ├── CourseProgress.java # Tiến độ học tập của từng khóa học
│   ├── AvailableCourse.java # Thông tin khóa học có sẵn trên hệ thống
│   ├── DailyGoal.java     # Mục tiêu học tập hàng ngày (XP, bài học...)
│   ├── Lesson.java        # Thông tin chi tiết bài học
│   ├── QuizQuestion.java  # Câu hỏi trắc nghiệm
│   └── UserProfile.java   # Thông tin chi tiết hồ sơ người dùng
├── HomeFragment.java      # Màn hình chính (Dashboard), theo dõi Streak và Goals
├── CourseFragment.java    # Hiển thị danh sách khóa học người dùng đã tham gia
├── QuizFragment.java      # Giao diện luyện tập trắc nghiệm
├── FlashcardFragment.java # Giao diện học tập qua thẻ nhớ (Flashcards)
├── ProfileFragment.java   # Quản lý và hiển thị thông tin cá nhân
├── LoginActivity.java     # Màn hình đăng nhập (Firebase Auth)
├── RegisterActivity.java  # Màn hình đăng ký tài khoản mới
├── LessonListActivity.java # Danh sách bài học của một khóa học
└── LessonDetailActivity.java # Nội dung chi tiết của từng bài học
```
​
## ⚙️ Cài đặt
​
1.  **Clone dự án:**
    ```bash
    git clone https://github.com/sondzzzz/StudyApp
    ```
2.  **Mở bằng Android Studio:** Chọn `File > Open` và trỏ đến thư mục dự án.
3.  **Cấu hình Firebase:**
    *   **Note:** Trong trường hợp bạn muốn sử dụng instance firebase của chúng tôi, không cần cấu hình gì cả, tiếp đến bước 4 luôn.
    *   Tạo project trên [Firebase Console](https://console.firebase.google.com/).
    *   Thêm ứng dụng Android với package name `com.example.myapplication`.
    *   Tải file `google-services.json` và đặt vào thư mục `app/`.
    *   Bật **Email/Password Authentication** và **Cloud Firestore**.
5. **Build & Run:** Nhấn `Shift + F10` để chạy ứng dụng trên máy ảo hoặc thiết bị thật.
    ​
## 📝 Giấy phép
​
Dự án này được phát triển cho mục đích giáo dục.