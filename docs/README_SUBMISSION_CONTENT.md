# HƯỚNG DẪN CÀI ĐẶT VÀ VẬN HÀNH HỆ THỐNG (SUBMISSION README)
# PROJECT SUBMISSION & SETUP GUIDE

> **Đề tài:** Xây dựng hệ thống thương mại điện tử đa người bán (Enterprise Multi-Vendor E-Commerce System)  
> **Kiến trúc:** Modular Monolith (Spring Boot 4 / Java 21) + Single Page Application (React 19 / TypeScript / Vite)  
> **Tài liệu tham chiếu:** [Báo cáo Đồ án chi tiết](file:///c:/Users/Dell/IdeaProjects/ecommerce/docs/REPORT_PRINT_VERSION.md) | [Đặc tả Cơ sở dữ liệu](file:///c:/Users/Dell/IdeaProjects/ecommerce/docs/DATABASE_TABLE_SPECIFICATION.md)

---

## MỤC LỤC CHI TIẾT (TABLE OF CONTENTS)
1. [Tổng quan dự án (Project Overview)](#1-tổng-quan-dự-án-project-overview)
2. [Công nghệ sử dụng (Technology Stack)](#2-công-nghệ-sử-dụng-technology-stack)
3. [Yêu cầu hệ thống (System Requirements)](#3-yêu-cầu-hệ-thống-system-requirements)
4. [Hướng dẫn cài đặt Backend (Backend Setup)](#4-hướng-dẫn-cài-đặt-backend-backend-setup)
5. [Hướng dẫn cài đặt Frontend (Frontend Setup)](#5-hướng-dẫn-cài-đặt-frontend-frontend-setup)
6. [Cấu hình cơ sở dữ liệu (Database Setup)](#6-cấu-hình-cơ-sở-dữ-liệu-database-setup)
7. [Biến môi trường & Mẫu cấu hình (Environment Variables)](#7-biến-môi-trường--mẫu-cấu-hình-environment-variables)
8. [Danh mục cổng mạng & Endpoints (Ports & Network Endpoints)](#8-danh-mục-cổng-mạng--endpoints-ports--network-endpoints)
9. [Các lệnh khởi động hệ thống (Startup Commands)](#9-các-lệnh-khởi-động-hệ-thống-startup-commands)
10. [Danh sách tài khoản kiểm thử (Test Accounts)](#10-danh-sách-tài-khoản-kiểm-thử-test-accounts)
11. [Phân quyền người dùng (User Roles & RBAC)](#11-phân-quyền-người-dùng-user-roles--rbac)
12. [Các thông số cấu hình quan trọng (Important Configuration)](#12-các-thông-số-cấu-hình-quan-trọng-important-configuration)
13. [Các dịch vụ bên ngoài (External Services)](#13-các-dịch-vụ-bên-ngoài-external-services)
14. [Thông tin tài khoản VNPay Sandbox (VNPay Sandbox Information)](#14-thông-tin-tài-khoản-vnpay-sandbox-vnpay-sandbox-information)
15. [Hướng dẫn Docker (Docker Instructions if actually used)](#15-hướng-dẫn-docker-docker-instructions-if-actually-used)
16. [Xử lý sự cố thường gặp (Troubleshooting)](#16-xử-lý-sự-cố-thường-gặp-troubleshooting)
17. [Kịch bản chạy thử nghiệm hệ thống (Demo Flow)](#17-kịch-bản-chạy-thử-nghiệm-hệ-thống-demo-flow)

---

## 1. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

Hệ thống Thương mại điện tử Đa người bán (**Enterprise Ecommerce**) là nền tảng marketplace B2C/C2C cho phép nhiều nhà bán hàng độc lập (Vendors) mở gian hàng và kinh doanh trên cùng một website, dưới sự kiểm soát và điều phối tập trung của Quản trị viên (Admin).

### Các đặc điểm kiến trúc và nghiệp vụ cốt lõi:
- **Kiến trúc Modular Monolith:** Toàn bộ backend được đóng gói trong một đơn vị triển khai (Spring Boot) nhưng phân tách nghiêm ngặt thành 10 module nghiệp vụ độc lập (`identity`, `shop`, `product`, `cart`, `order`, `payment`, `voucher`, `review`, `notification`, `chat`), giao tiếp qua hợp đồng nội bộ (Internal Service contracts) và sự kiện bất đồng bộ (`@TransactionalEventListener`).
- **Giỏ hàng & Checkout đa shop:** Khách hàng có thể gom sản phẩm từ nhiều shop khác nhau vào cùng một giỏ hàng. Khi thanh toán, hệ thống tự động gom nhóm theo shop và tách thành $N$ đơn hàng độc lập cho $N$ gian hàng tương ứng trong cùng một phiên giao dịch thanh toán (`payment_group_id`).
- **Phân bổ Voucher tỷ lệ (Proportional Split):** Mã giảm giá toàn sàn (`SYSTEM`) được tự động tính toán chiết khấu chia đều theo tỷ trọng doanh thu của từng shop trong đơn hàng.
- **Thanh toán bảo mật & Idempotent:** Tích hợp VNPay Sandbox với chữ ký HMAC-SHA512 và cơ chế khóa bi quan (`PESSIMISTIC_WRITE`) trên bảng `payments`, ngăn chặn tuyệt đối duplicate IPN webhook và double credit.
- **Giao tiếp thời gian thực (Realtime STOMP):** Khung chat nổi trực tiếp (`FloatingChatWidget`) giữa Khách hàng và Chủ shop, cùng hệ thống thông báo đẩy (Notification Push) qua WebSocket STOMP kèm xác thực JWT trên kết nối.
- **Báo cáo phân tích kinh doanh:** Dashboard thống kê doanh thu toàn sàn cho Admin (`/admin`) và trang phân tích chỉ số kinh doanh 30 ngày cho từng Vendor (`/vendor/analytics`).

---

## 2. CÔNG NGHỆ SỬ DỤNG (TECHNOLOGY STACK)

### Phía Backend
- **Ngôn ngữ lập trình:** Java 21 (LTS)
- **Framework nền tảng:** Spring Boot 4.1.0
- **Truy cập dữ liệu / ORM:** Spring Data JPA / Hibernate 6.x
- **Bảo mật & Phân quyền:** Spring Security, JJWT (io.jsonwebtoken 0.12.5)
- **Giao tiếp thời gian thực:** Spring WebSocket, STOMP Messaging
- **Thư điện tử:** Spring Mail (Gmail SMTP với TLS 587)
- **Tài liệu API:** SpringDoc OpenAPI 3.0.3 (Swagger UI)
- **Kiểm thử tự động:** JUnit 5, Mockito, AssertJ, Spring Boot Test

### Phía Frontend
- **Nền tảng:** React 19.2.8, TypeScript 6.0.2
- **Công cụ đóng gói (Build Tool):** Vite 8.2.0
- **Định tuyến (Routing):** React Router DOM 7.18.2
- **HTTP Client:** Axios 1.19.0 (tích hợp interceptor đính kèm JWT Bearer token và xử lý lỗi tập trung)
- **Client Realtime:** `@stomp/stompjs` 7.3.0, `sockjs-client` 1.6.1
- **Styling:** Tailwind CSS 4.3.3, Lucide React Icons

### Cơ sở dữ liệu & Hạ tầng dịch vụ ngoài
- **Hệ quản trị CSDL:** MySQL Server 8.0+
- **Lưu trữ CDN đa phương tiện:** Cloudinary Media Cloud SDK
- **Cổng thanh toán:** VNPay Payment Gateway (Môi trường Sandbox)
- **Mail Server:** Google Gmail SMTP Server (App Password)

---

## 3. YÊU CẦU HỆ THỐNG (SYSTEM REQUIREMENTS)

Trước khi tiến hành cài đặt, máy tính cần đáp ứng các yêu cầu phần mềm tối thiểu sau:

| Phần mềm / Công cụ | Phiên bản yêu cầu | Mục đích kiểm tra | Lệnh kiểm tra trên Terminal |
|---|---|---|---|
| **Java Development Kit (JDK)** | OpenJDK 21 hoặc Oracle JDK 21+ | Biên dịch và chạy Backend Spring Boot | `java -version` |
| **Node.js** | Node.js v18.x hoặc v20.x+ (LTS) | Môi trường thực thi JavaScript/Frontend | `node -v` |
| **npm** | npm v9.x hoặc v10.x+ | Quản lý gói thư viện phụ thuộc Frontend | `npm -v` |
| **MySQL Server** | MySQL 8.0.x trở lên | Lưu trữ cơ sở dữ liệu quan hệ | `mysql --version` |
| **Git** | Git 2.x+ | Quản lý mã nguồn dự án | `git --version` |

*(Dự án đã tích hợp sẵn Maven Wrapper `./mvnw` và `mvnw.cmd`, không bắt buộc cài đặt Maven rời trên hệ điều hành).*

---

## 4. HƯỚNG DẪN CÀI ĐẶT BACKEND (BACKEND SETUP)

1. Mở cửa sổ Terminal tại thư mục gốc của dự án (`c:\Users\Dell\IdeaProjects\ecommerce`):
   ```bash
   cd c:\Users\Dell\IdeaProjects\ecommerce
   ```

2. Kiểm tra tính toàn vẹn của Maven Wrapper và tải các thư viện phụ thuộc:
   - **Trên Windows:**
     ```powershell
     .\mvnw.cmd dependency:resolve
     ```
   - **Trên Linux / macOS:**
     ```bash
     chmod +x ./mvnw
     ./mvnw dependency:resolve
     ```

3. Biên dịch mã nguồn Backend:
   ```bash
   # Windows
   .\mvnw.cmd clean compile
   # Linux / macOS
   ./mvnw clean compile
   ```

4. Cấu hình biến môi trường kết nối CSDL và các dịch vụ bên ngoài (xem chi tiết tại [Mục 7](#7-biến-môi-trường--mẫu-cấu-hình-environment-variables)).

---

## 5. HƯỚNG DẪN CÀI ĐẶT FRONTEND (FRONTEND SETUP)

1. Mở một cửa sổ Terminal mới và di chuyển vào thư mục `frontend/`:
   ```bash
   cd c:\Users\Dell\IdeaProjects\ecommerce\frontend
   ```

2. Cài đặt các gói thư viện phụ thuộc của React và Vite:
   ```bash
   npm install
   ```

3. (Tùy chọn) Kiểm tra tệp cấu hình URL máy chủ Backend:
   - Frontend mặc định kết nối tới Backend tại `http://localhost:8080` (được định nghĩa qua biến `VITE_API_URL` trong [apiClient.ts](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/api/apiClient.ts)).
   - Nếu triển khai trên tên miền hoặc port khác, tạo tệp `frontend/.env` với nội dung:
     ```properties
     VITE_API_URL=http://localhost:8080
     ```

4. Kiểm tra biên dịch Type-check và đóng gói thử nghiệm:
   ```bash
   npm run build
   ```

---

## 6. CẤU HÌNH CƠ SỞ DỮ LIỆU (DATABASE SETUP)

1. Khởi động dịch vụ **MySQL Server** trên máy cục bộ (mặc định lắng nghe cổng `3306`).
2. Mở MySQL Workbench, DBeaver hoặc dòng lệnh `mysql` client và tạo cơ sở dữ liệu mới với bảng mã `utf8mb4`:

```sql
CREATE DATABASE IF NOT EXISTS ecommerce 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

3. **Cơ chế khởi tạo bảng tự động (Auto-DDL):**
   Backend được cấu hình `spring.jpa.hibernate.ddl-auto: update` trong `src/main/resources/application.yml`. Khi khởi động ứng dụng lần đầu tiên, Hibernate sẽ tự động sinh toàn bộ **22 bảng** và các chỉ mục khóa ngoại/chỉ mục tìm kiếm theo đúng đặc tả thực thể.

4. **Nạp dữ liệu mẫu ban đầu (Initial Seed Data):**
   - Người dùng có thể sử dụng giao diện hệ thống để đăng ký tài khoản, mở gian hàng và đăng sản phẩm mới.
   - Hoặc nạp các tệp script mẫu kiểm thử đã chuẩn bị trong thư mục `docs/migrations/` hoặc `docs/database/` nếu cần khôi phục nhanh dữ liệu kiểm thử.

---

## 7. BIẾN MÔI TRƯỜNG & MẪU CẤU HÌNH (ENVIRONMENT VARIABLES)

Hệ thống đọc các biến môi trường cấu hình tại `src/main/resources/application.yml`.  
Bạn có thể thiết lập trực tiếp trên terminal của hệ điều hành hoặc tạo tệp `.env` tại thư mục gốc của dự án (tệp này được nạp tự động qua `spring.config.import: optional:file:.env[.properties]`).

> ⚠️ **LƯU Ý BẢO MẬT TUYỆT ĐỐI:** Tuyệt đối không commit tệp `.env` chứa thông tin nhạy cảm thật lên Git. Dự án đã cung cấp tệp [.env.example](file:///c:/Users/Dell/IdeaProjects/ecommerce/.env.example) làm mẫu.

Dưới đây là bảng danh sách biến môi trường và mẫu giá trị placeholder:

| Tên biến môi trường | Ý nghĩa cấu hình | Giá trị mặc định / Gợi ý cấu hình |
|---|---|---|
| `DB_PASSWORD` | Mật khẩu truy cập MySQL của user `root` | `your_mysql_password` |
| `JWT_SECRET_KEY` | Chuỗi khóa bí mật HMAC-SHA256 ký JWT (tối thiểu 256-bit) | `your_hex_or_base64_encoded_256bit_secret_key` |
| `MAIL_USERNAME` | Địa chỉ Gmail gửi thông báo tự động | `your_email@gmail.com` |
| `MAIL_PASSWORD` | Mật khẩu ứng dụng 16 chữ số của Google (App Password) | `your_16_digit_google_app_password` |
| `CLOUDINARY_CLOUD_NAME` | Tên cloud lưu trữ trên Cloudinary Media CDN | `your_cloud_name` |
| `CLOUDINARY_API_KEY` | API Key xác thực tải ảnh Cloudinary | `your_cloudinary_api_key` |
| `CLOUDINARY_API_SECRET` | Khóa bí mật API Secret của Cloudinary | `your_cloudinary_api_secret` |
| `VNPAY_TMN_CODE` | Mã định danh đơn vị kết nối VNPay (Terminal ID) | `your_vnpay_tmn_code` |
| `VNPAY_SECRET_KEY` | Chuỗi băm bí mật tạo checksum giao dịch VNPay | `your_vnpay_secret_key` |
| `VNPAY_API_IP` | Địa chỉ IP máy chủ gọi đối soát giao dịch VNPay Sandbox | `127.0.0.1` |

### Tệp mẫu `.env` đặt tại thư mục gốc dự án:
```properties
# =====================================================
# Enterprise Ecommerce — Environment Variables Template
# =====================================================

# --- Database MySQL ---
DB_PASSWORD=your_mysql_password_here

# --- Bảo mật JWT (HMAC-SHA256 tối thiểu 256-bit) ---
JWT_SECRET_KEY=your_base64_or_hex_encoded_secret_key_minimum_256_bits_here

# --- Gmail SMTP (Gửi email thông báo tự động) ---
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_digit_google_app_password

# --- Cloudinary CDN (Lưu trữ ảnh sản phẩm & đánh giá) ---
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# --- Cổng thanh toán VNPay Sandbox ---
VNPAY_TMN_CODE=your_vnpay_tmn_code
VNPAY_SECRET_KEY=your_vnpay_secret_key
VNPAY_API_IP=127.0.0.1
```

---

## 8. DANH MỤC CỔNG MẠNG & ENDPOINTS (PORTS & NETWORK ENDPOINTS)

| Dịch vụ / Thành phần | Cổng mạng (Port) | Địa chỉ truy cập / Endpoint | Ghi chú vận hành |
|---|---|---|---|
| **Frontend Web App** | `5173` | `http://localhost:5173` | Giao diện Single Page Application cho Khách hàng, Vendor, Admin |
| **Backend API Service** | `8080` | `http://localhost:8080` | Máy chủ REST API Spring Boot |
| **Swagger UI (API Docs)** | `8080` | `http://localhost:8080/swagger-ui/index.html` | Giao diện kiểm thử trực quan các API Endpoint |
| **OpenAPI Specification** | `8080` | `http://localhost:8080/v3/api-docs` | Cấu hình đặc tả JSON OpenAPI v3 |
| **WebSocket STOMP Endpoint** | `8080` | `ws://localhost:8080/ws` | Kênh kết nối Realtime Chat và Thông báo đẩy |
| **MySQL Server** | `3306` | `localhost:3306` | Kết nối CSDL quan hệ `ecommerce` |

---

## 9. CÁC LỆNH KHỞI ĐỘNG HỆ THỐNG (STARTUP COMMANDS)

### Bước 1: Khởi động Backend (Spring Boot Service)

Mở một cửa sổ Terminal tại thư mục gốc dự án (`ecommerce/`):

**Trên Windows (PowerShell):**
```powershell
# 1. Gán biến môi trường phiên chạy (thay thế bằng giá trị thực tế của bạn)
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET_KEY="your_jwt_secret_key_minimum_256_bits"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_google_app_password"
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"
$env:VNPAY_TMN_CODE="your_vnpay_tmn"
$env:VNPAY_SECRET_KEY="your_vnpay_hash_secret"
$env:VNPAY_API_IP="127.0.0.1"

# 2. Chạy ứng dụng Spring Boot bằng Maven Wrapper
.\mvnw.cmd spring-boot:run
```

**Trên Linux / macOS (Bash):**
```bash
# 1. Gán biến môi trường
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET_KEY="your_jwt_secret_key_minimum_256_bits"
export MAIL_USERNAME="your_email@gmail.com"
export MAIL_PASSWORD="your_google_app_password"
export CLOUDINARY_CLOUD_NAME="your_cloud_name"
export CLOUDINARY_API_KEY="your_api_key"
export CLOUDINARY_API_SECRET="your_api_secret"
export VNPAY_TMN_CODE="your_vnpay_tmn"
export VNPAY_SECRET_KEY="your_vnpay_hash_secret"
export VNPAY_API_IP="127.0.0.1"

# 2. Khởi chạy máy chủ
chmod +x ./mvnw
./mvnw spring-boot:run
```

*Khi terminal xuất hiện dòng thông báo: `Started EcommerceApplication in ... seconds (process running for ...)`, Backend đã sẵn sàng phục vụ tại `http://localhost:8080`.*

---

### Bước 2: Khởi động Frontend (React / Vite Dev Server)

Mở một cửa sổ Terminal thứ hai, di chuyển vào thư mục `frontend`:

```bash
# 1. Di chuyển vào thư mục frontend
cd frontend

# 2. Cài đặt thư viện phụ thuộc (chỉ cần chạy lần đầu tiên)
npm install

# 3. Khởi chạy máy chủ phát triển Vite
npm run dev
```

*Frontend sẽ khởi chạy tại `http://localhost:5173`. Mở trình duyệt và truy cập vào địa chỉ này để bắt đầu trải nghiệm.*

---

## 10. DANH SÁCH TÀI KHOẢN KIỂM THỬ (TEST ACCOUNTS)

Hệ thống cung cấp sẵn các tài khoản kiểm thử tiêu chuẩn phục vụ hội đồng chấm và đánh giá đồ án:

| Tác nhân | Email đăng nhập | Mật khẩu mẫu | Vai trò (Role) | Trạng thái ban đầu |
|---|---|---|---|---|
| **Quản trị viên (Admin)** | `admin@test.com` | `Password123!` *(hoặc mật khẩu do bạn thiết lập)* | `ROLE_ADMIN` | Toàn quyền kiểm soát hệ thống, xem Dashboard toàn sàn |
| **Chủ gian hàng (Vendor)** | `vendor@test.com` | `Password123!` *(hoặc mật khẩu do bạn thiết lập)* | `ROLE_VENDOR` | Gian hàng `ACTIVE`, quản lý sản phẩm, đơn hàng và phân tích doanh thu |
| **Khách hàng (Customer)** | `buyer@test.com` | `Password123!` *(hoặc mật khẩu do bạn thiết lập)* | `ROLE_CUSTOMER` | Đã có sẵn sản phẩm trong giỏ hàng và lịch sử đơn hàng |

> **Mẹo tạo tài khoản kiểm thử mới:**  
> Bạn có thể nhấn **"Đăng Ký"** trên thanh điều hướng (`/register`) để tạo tài khoản Customer mới bất kỳ lúc nào; sau đó nhấn **"Kênh Người Bán"** để nộp hồ sơ mở shop mới và dùng tài khoản Admin để duyệt kích hoạt.

---

## 11. PHÂN QUYỀN NGƯỜI DÙNG (USER ROLES & RBAC)

Hệ thống áp dụng mô hình phân quyền dựa trên vai trò (Role-Based Access Control - RBAC) với 3 vai trò phân tách chặt chẽ:

```
+------------------------------------------------------------------------+
|                              USER ROLES                                |
+-------------------+--------------------+-------------------------------+
|   ROLE_CUSTOMER   |    ROLE_VENDOR     |          ROLE_ADMIN           |
+-------------------+--------------------+-------------------------------+
| - Xem sản phẩm    | - Quản lý sản phẩm | - Phê duyệt / từ chối shop    |
| - Giỏ hàng đa shop| - Quản lý biến thể | - Khóa / mở khóa gian hàng    |
| - Đặt hàng COD/VN | - Xử lý đơn hàng   | - Quản lý đơn hàng toàn sàn   |
| - Xác nhận nhận   | - Tạo voucher shop | - Tạo voucher toàn sàn        |
| - Viết đánh giá   | - Chat khách hàng  | - Xem Admin Dashboard toàn sàn|
| - Chat với Shop   | - Xem Analytics    | - Quản lý người dùng          |
+-------------------+--------------------+-------------------------------+
```

1. **`ROLE_CUSTOMER` (Khách hàng):**
   - Quyền hạn: Duyệt danh mục phân cấp theo thuật toán BFS, lọc sản phẩm theo giá và thuộc tính, thêm sản phẩm vào giỏ hàng từ nhiều shop, thanh toán trực tuyến qua VNPay hoặc tiền mặt COD, quản lý danh sách đơn hàng, xác nhận nhận hàng, viết đánh giá sản phẩm kèm ảnh, tương tác chat thời gian thực với người bán thông qua `FloatingChatWidget`, chỉnh sửa thông tin cá nhân.
2. **`ROLE_VENDOR` (Người bán hàng):**
   - Không gian truy cập: `/vendor/*`
   - Quyền hạn: Quản lý danh mục sản phẩm của shop, thêm biến thể sản phẩm (màu sắc, kích thước, tồn kho, giá), tải ảnh sản phẩm lên Cloudinary CDN, quản lý đơn hàng của gian hàng (Xác nhận $\rightarrow$ Gửi hàng), tạo và quản lý mã giảm giá riêng của Shop, xem báo cáo phân tích doanh thu 30 ngày (`/vendor/analytics`).
3. **`ROLE_ADMIN` (Quản trị viên):**
   - Không gian truy cập: `/admin/*`
   - Quyền hạn: Xem xét và phê duyệt/từ chối đơn đăng ký mở shop của người dùng, khóa (`BANNED`) hoặc mở khóa (`ACTIVE`) gian hàng vi phạm, giám sát tất cả đơn hàng trên toàn hệ thống, tạo mã giảm giá toàn sàn (`SYSTEM voucher`) với thuật toán chia sẻ chiết khấu theo tỷ trọng doanh thu, theo dõi tổng quan các chỉ số kinh doanh tại Admin Dashboard (`/admin`).

---

## 12. CÁC THÔNG SỐ CẤU HÌNH QUAN TRỌNG (IMPORTANT CONFIGURATION)

- **Thời hạn hiệu lực của JWT Token:** Mặc định cấu hình `86400000 ms` (24 giờ), được đính kèm vào Header `Authorization: Bearer <token>` trong tất cả các yêu cầu Axios và STOMP CONNECT frame.
- **Ranh giới giao dịch thanh toán & Chống Overselling:**
  - Nghiệp vụ Checkout được đóng gói trong một `@Transactional` duy nhất tại [OrderServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/order/service/OrderServiceImpl.java).
  - Trừ tồn kho tại bảng `product_variants` được kiểm soát tính cô lập chặt chẽ, nếu một biến thể bị thiếu hàng, toàn bộ giao dịch đặt hàng sẽ bị rollback, bảo vệ tính toàn vẹn dữ liệu.
- **Xử lý sự kiện sau khi Commit (`AFTER_COMMIT`):**
  - Các tác vụ phụ phát sinh sau khi đặt hàng (phát thông báo đẩy Realtime STOMP và gửi email xác nhận qua Gmail SMTP) được lắng nghe thông qua `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`, đảm bảo giao dịch CSDL đã lưu thành công trước khi gửi thông báo ra bên ngoài.
- **Cơ chế Idempotent cho Webhook VNPay:**
  - Giao dịch thanh toán được khóa bi quan `PESSIMISTIC_WRITE` trên bảng `payments`, kiểm tra trạng thái trước khi cập nhật, ngăn chặn xử lý trùng lặp khi VNPay gửi lặp lại IPN.

---

## 13. CÁC DỊCH VỤ BÊN NGOÀI (EXTERNAL SERVICES)

Dự án tích hợp 3 dịch vụ đám mây bên ngoài để đảm bảo tính năng thương mại điện tử hoàn chỉnh:

1. **Cloudinary Media Cloud CDN:**
   - Mục đích: Lưu trữ và tối ưu hóa hình ảnh sản phẩm do Vendor đăng tải và hình ảnh phản hồi đánh giá do Khách hàng tải lên.
   - Cơ chế: Backend nhận tệp `MultipartFile`, gửi lên Cloudinary qua Cloudinary Java SDK, nhận về URL ảnh bảo mật `https://res.cloudinary.com/...` để lưu vào CSDL.
2. **Google Gmail SMTP Server:**
   - Mục đích: Gửi email thông báo tự động (thông báo kết quả duyệt/từ chối gian hàng từ Admin tới Vendor).
   - Cổng kết nối: `smtp.gmail.com:587` với cơ chế mã hóa TLS (`starttls.enable=true`) và mật khẩu ứng dụng (Google App Password).
3. **Cổng thanh toán điện tử VNPay (Môi trường Sandbox):**
   - Mục đích: Xử lý giao dịch thanh toán trực tuyến bằng thẻ ngân hàng nội địa ATM qua cổng thanh toán VNPay Sandbox.
   - Giao thức: Chuyển hướng thanh toán người dùng sang giao diện VNPay với chữ ký số `HmacSHA512`, tiếp nhận kết quả phản hồi qua Return URL và IPN Webhook.

---

## 14. THÔNG TIN TÀI KHOẢN VNPAY SANDBOX (VNPAY SANDBOX INFORMATION)

Hệ thống sử dụng cổng thử nghiệm chính thức của VNPay Sandbox với các thông số:

- **Cổng thanh toán URL:** `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- **Phiên bản API:** `2.1.0`
- **Thuật toán băm chữ ký kiểm tra:** `HmacSHA512`
- **Thông tin thẻ ATM kiểm thử chính thức do VNPay cung cấp:**

| Thông tin thẻ | Giá trị kiểm thử thử nghiệm | Ghi chú |
|---|---|---|
| **Ngân hàng chấp nhận** | **NCB** (Ngân hàng Quốc Dân) | Chọn logo NCB trên giao diện cổng VNPay |
| **Số thẻ ATM** | `9704198526191432198` | Thẻ test sandbox |
| **Tên chủ thẻ** | `NGUYEN VAN A` | Viết hoa không dấu |
| **Ngày phát hành** | `07/15` | Tháng/Năm |
| **Mã xác thực OTP** | `123456` | Mã OTP cố định trong môi trường Sandbox |

---

## 15. HƯỚNG DẪN DOCKER (DOCKER INSTRUCTIONS IF ACTUALLY USED)

> ℹ️ **Lưu ý thực tế về cấu hình môi trường:**  
> Dự án hiện tại được thiết kế, kiểm thử và vận hành tối ưu trực tiếp trên môi trường **Local Development** (máy chủ vật lý chạy Java 21, Node.js v18+/v20+ và MySQL Server 8.0 native) nhằm phục vụ công tác phát triển nhanh, kiểm thử chức năng trực quan và bảo vệ dữ liệu thử nghiệm trong suốt quá trình làm đồ án.  
> 
> Trong mã nguồn hiện tại, hệ thống **chưa đóng gói các tệp Dockerfile / docker-compose.yml**. Toàn bộ kiến trúc container hóa (Dockerfile cho Spring Boot, Dockerfile multi-stage cho React/Nginx, và docker-compose điều phối toàn bộ hạ tầng gồm MySQL, Redis và App Service) đã được thiết kế chi tiết và đưa vào **Lộ trình phát triển tương lai** (được ghi nhận tại Chương 4 của Báo cáo Đồ án).

---

## 16. XỬ LÝ SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

### 1. Lỗi cổng mạng bị chiếm dụng (Port 8080 hoặc 5173 already in use)
- **Hiện tượng:** Backend dừng với lỗi `Port 8080 was already in use` hoặc Vite chuyển sang cổng khác `5174`.
- **Cách khắc phục trên Windows:**
  ```powershell
  # Tìm ID tiến trình (PID) đang giữ cổng 8080
  netstat -ano | findstr :8080
  # Dừng tiến trình chiếm dụng (thay <PID> bằng số tiến trình thực tế)
  taskkill /PID <PID> /F
  ```

### 2. Lỗi kết nối Cơ sở dữ liệu MySQL (Communications link failure / Access denied)
- **Hiện tượng:** Backend ném ra ngoại lệ `Communications link failure` hoặc `Access denied for user 'root'@'localhost'`.
- **Cách khắc phục:**
  - Kiểm tra dịch vụ MySQL Server đã chạy chưa (`Get-Service MySQL*` trong PowerShell hoặc mở `services.msc`).
  - Đảm bảo biến `DB_PASSWORD` đã truyền vào khớp chính xác với mật khẩu MySQL của bạn.
  - Đảm bảo cơ sở dữ liệu `ecommerce` đã được tạo trước bằng lệnh SQL `CREATE DATABASE IF NOT EXISTS ecommerce;`.

### 3. Gặp lỗi gửi email thông báo (MailAuthenticationException)
- **Hiện tượng:** Báo lỗi `Username and Password not accepted` khi Quản trị viên duyệt hồ sơ mở shop.
- **Cách khắc phục:**
  - Tài khoản Gmail bắt buộc phải bật tính năng **Bảo mật 2 lớp (2-Step Verification)**.
  - Vào phần Cài đặt Bảo mật tài khoản Google $\rightarrow$ tạo một **Mật khẩu ứng dụng (App Password)** gồm 16 ký tự để gán vào `MAIL_PASSWORD`, không sử dụng mật khẩu đăng nhập Gmail thông thường.

### 4. Lỗi thanh toán VNPay báo checksum không hợp lệ (Invalid Checksum)
- **Hiện tượng:** Cổng VNPay báo lỗi sai mã băm chữ ký số khi người dùng chuyển hướng thanh toán.
- **Cách khắc phục:**
  - Kiểm tra lại `VNPAY_TMN_CODE` và `VNPAY_SECRET_KEY` được truyền vào, đảm bảo sao chép chính xác từ portal Sandbox của VNPay và không có khoảng trắng thừa ở đầu/cuối chuỗi.

### 5. Kết nối WebSocket STOMP không nhận được tin nhắn chat / thông báo
- **Hiện tượng:** Tin nhắn chat không hiển thị realtime hoặc thông báo không nảy chuông.
- **Cách khắc phục:**
  - Kiểm tra xem token JWT trong `localStorage` có bị hết hạn hay không. Đăng xuất và đăng nhập lại để làm mới token.
  - Kiểm tra Console trình duyệt (F12) để xem kết nối `ws://localhost:8080/ws` có ở trạng thái `CONNECTED` hay không.

---

## 17. KỊCH BẢN CHẠY THỬ NGHIỆM HỆ THỐNG (DEMO FLOW)

Để trình diễn toàn bộ chu trình nghiệp vụ khép kín của đề tài trong vòng 6–8 phút, khuyến nghị mở 3 cửa sổ/tab trình duyệt ẩn danh độc lập đại diện cho 3 tác nhân:

```
+---------------------------------------------------------------------------------------------+
|                                    DEMO WORKFLOW OVERVIEW                                   |
+--------------------+--------------------------------+---------------------------------------+
| Tab 1 (Customer)   | Đăng nhập buyer@test.com       | Mua hàng đa shop, áp Voucher, VNPay   |
| Tab 2 (Vendor)     | Đăng nhập vendor@test.com      | Tiếp nhận đơn, giao hàng, xem báo cáo |
| Tab 3 (Admin)      | Đăng nhập admin@test.com       | Duyệt shop, Dashboard thống kê        |
+--------------------+--------------------------------+---------------------------------------+
```

### Bước 1: Mua hàng đa shop & Thanh toán trực tuyến VNPay (Tab Customer)
1. Đăng nhập tài khoản Khách hàng `buyer@test.com`.
2. Truy cập trang danh sách sản phẩm `/products`, sử dụng bộ lọc danh mục phân cấp theo cây BFS và bộ lọc khoảng giá.
3. Nhấp vào sản phẩm, lựa chọn phân loại biến thể (màu sắc/kích thước), nhấn **"Thêm vào giỏ hàng"**.
4. Truy cập giỏ hàng `/cart`, tick chọn các sản phẩm từ các shop khác nhau, kiểm tra tổng tiền và nhấn **"Mua Hàng"**.
5. Tại trang thanh toán `/checkout`:
   - Nhập địa chỉ giao nhận hàng.
   - Nhập mã voucher khuyến mãi (ví dụ voucher sàn `SALE20`), hệ thống tự động tính toán chiết khấu chia tỷ lệ cho từng shop.
   - Chọn phương thức thanh toán **VNPAY Sandbox**.
   - Nhấn **"Đặt Hàng"**.
6. Trình duyệt tự động chuyển hướng sang cổng VNPay Sandbox:
   - Chọn phương thức Thẻ nội địa $\rightarrow$ Chọn ngân hàng **NCB**.
   - Nhập số thẻ `9704198526191432198`, tên `NGUYEN VAN A`, ngày phát hành `07/15`.
   - Nhập mã xác thực OTP `123456`.
7. VNPay xác nhận giao dịch thành công và chuyển hướng về trang `/payment-result` với thông báo giao dịch hoàn tất.

### Bước 2: Tương tác trao đổi thời gian thực qua Chat (Tab Customer & Tab Vendor)
1. Tại Tab Customer, nhấn vào biểu tượng **Floating Chat Widget** ở góc dưới cùng bên phải màn hình.
2. Chọn shop vừa mua hàng và gửi tin nhắn: *"Chào shop, đơn hàng của mình khi nào được gửi đi ạ?"*.
3. Mở Tab Vendor (`vendor@test.com`), ngay lập tức tin nhắn của khách hàng xuất hiện theo thời gian thực mà không cần tải lại trang (nhờ giao thức WebSocket STOMP).
4. Vendor trả lời: *"Dạ shop đã đóng gói và chuẩn bị bàn giao cho bưu tá ạ"*.

### Bước 3: Quản lý đơn hàng & Phân tích kinh doanh (Tab Vendor)
1. Vendor truy cập vào trang Quản lý đơn hàng `/vendor/orders`.
2. Đơn hàng vừa đặt hiển thị ở trạng thái `CONFIRMED` (đã thanh toán thành công qua VNPay).
3. Vendor nhấn nút **"Xác nhận gửi hàng"** $\rightarrow$ Trạng thái đơn hàng chuyển sang `SHIPPED`.
4. Vendor truy cập vào trang Phân tích doanh thu `/vendor/analytics`:
   - Xem các thẻ chỉ số KPI kinh doanh (Tổng doanh thu, Tổng đơn hàng, Doanh thu trung bình).
   - Quan sát biểu đồ cột doanh thu theo ngày được cập nhật tự động.

### Bước 4: Khách hàng xác nhận nhận hàng & Đánh giá sản phẩm (Tab Customer)
1. Khách hàng truy cập vào trang Lịch sử đơn hàng `/orders`.
2. Tại đơn hàng đang ở trạng thái `SHIPPED`, khách hàng nhấn nút **"Đã nhận được hàng"**.
3. Hệ thống chuyển trạng thái đơn hàng sang `DELIVERED`.
4. Nút **"Viết đánh giá"** được kích hoạt:
   - Khách hàng chọn đánh giá 5 sao.
   - Nhập phản hồi nhận xét và chọn tải lên hình ảnh sản phẩm thực tế.
   - Nhấn gửi đánh giá $\rightarrow$ Đánh giá xuất hiện ngay trên trang chi tiết sản phẩm.

### Bước 5: Giám sát toàn sàn & Điều hành Quản trị (Tab Admin)
1. Mở Tab Admin (`admin@test.com`), truy cập trang Quản trị `/admin`.
2. Quan sát **Admin Dashboard**:
   - Thẻ thống kê KPI toàn sàn: Tổng doanh thu nền tảng, Tổng số người dùng, Tổng số đơn hàng, Tổng số gian hàng hoạt động.
   - Biểu đồ trực quan hóa doanh thu toàn hệ thống.
   - Bảng danh sách các đơn hàng mới nhất trên toàn sàn.
3. Truy cập `/admin/shop`:
   - Xem danh sách các gian hàng đã đăng ký.
   - Thử nghiệm thao tác kiểm duyệt gian hàng mới hoặc khóa (`BANNED`) và mở khóa (`ACTIVE`) gian hàng vi phạm.
4. Truy cập `/admin/vouchers`:
   - Tạo mới mã khuyến mãi toàn sàn (`SYSTEM`) để tiếp tục áp dụng cho các phiên mua sắm tiếp theo.
