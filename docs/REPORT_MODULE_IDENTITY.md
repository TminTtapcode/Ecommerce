# BÁO CÁO KIỂM TOÁN MÔ ĐUN: IDENTITY (XÁC THỰC)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Identity đảm nhận trách nhiệm cốt lõi về bảo mật, xác thực người dùng (Authentication) và phân quyền (Authorization). Đây là cửa ngõ đầu tiên mà mọi yêu cầu giao tiếp với hệ thống phải vượt qua.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Quản lý tài khoản người dùng, băm mật khẩu (Bcrypt).
- Cấp phát và xác thực JSON Web Token (JWT).
- Phân quyền người dùng dựa trên vai trò (Role-Based Access Control - RBAC).

## 3. CÁC THỰC THỂ (ENTITIES)
- `User`: Lưu trữ thông tin định danh (`email`, `password_hash`).
- `Role`: Phân loại quyền (`ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`).
- Mối quan hệ: Nhiều - Nhiều (`@ManyToMany`) thông qua bảng trung gian `user_roles`.

## 4. BẢO MẬT VÀ LUỒNG XÁC THỰC
- **Đăng nhập:** Người dùng cung cấp Email và Mật khẩu. Trả về Access Token (JWT).
- **Bộ lọc:** `JwtAuthenticationFilter` chặn mọi Request, giải mã Token, trích xuất thông tin người dùng và đẩy vào `SecurityContextHolder`.
- **Cấp quyền:** Spring Security sử dụng annotation `@PreAuthorize("hasRole('...')")` tại tầng Controller để kiểm soát truy cập.

## 5. ĐẶC TẢ API (APIs)
- `POST /api/v1/auth/register`: Đăng ký tài khoản (Tự động gán `ROLE_USER`).
- `POST /api/v1/auth/login`: Đăng nhập cấp Token.

## 6. GIAO KẾT BE/FE
- Mismatch: Hiện tại JWT là dạng Stateless (không lưu trạng thái trên Server), hệ thống chưa triển khai Blacklist Token. Việc Logout ở FE thực chất chỉ là thao tác xóa Token khỏi `localStorage`.

## 7. HẠN CHẾ HIỆN TẠI
- Chưa có tính năng Quên mật khẩu (Forgot Password) bằng Email OTP.
- Thiếu Refresh Token, người dùng sẽ bị văng ra khi Token hết hạn cứng.
