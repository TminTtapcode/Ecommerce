# Báo Cáo Hoàn Thành P1-05 (Feature 2 — Admin Dashboard Statistics)

Ngày: **2026-09-09**  
Trạng thái: **HOÀN THÀNH TOÀN DIỆN (BE → FE → Tests → Verification)**

---

## 1. Tổng quan phạm vi triển khai

Tính năng P1-05 cung cấp bảng điều khiển thống kê tổng quan (Admin Dashboard Statistics) cho quản trị viên, tổng hợp dữ liệu từ các module theo kiến trúc Modular Monolith:
- **Module Identity**: Expose `countUsers()` qua `IdentityInternalService`.
- **Module Shop**: Expose `countShops()` qua `ShopInternalService`.
- **Module Product**: Expose `countProducts()` qua `ProductInternalService`.
- **Module Order**: Expose `getDashboardOrderStats(startDate, endDate)` qua `OrderInternalService`, tính toán:
  - `totalOrders`: Tổng số đơn hàng trong khoảng ngày.
  - `ordersByStatus`: Phân bố số lượng đơn hàng theo từng trạng thái (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
  - `deliveredRevenue`: Tổng doanh thu chỉ tính từ các đơn hàng `DELIVERED` (loại bỏ unpaid, pending và cancelled để đảm bảo tính chuẩn xác doanh thu thực tế).
  - `averageOrderValue (AOV)`: `deliveredRevenue / deliveredCount` (làm tròn 2 chữ số thập phân).
  - `dailyChart`: Thống kê số lượng đơn hàng theo từng ngày (`yyyy-MM-dd`) trong khoảng thời gian lọc.
  - `recentOrders`: 10 đơn hàng mới nhất trên toàn hệ thống.
- **Module Statistics**: Package `com.tmt.ecommerce.statistics` độc lập với Controller `AdminStatisticsController` và Response DTO `AdminDashboardStatsResponse`, bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`.

---

## 2. API Contract

### `GET /api/v1/admin/statistics/dashboard`
- **Quyền truy cập**: `ROLE_ADMIN` (các role khác trả về `HTTP 403 Forbidden`, chưa đăng nhập trả về `HTTP 401/403`).
- **Query Parameters**:
  - `startDate` (optional, format: `yyyy-MM-dd`): Ngày bắt đầu lọc đơn hàng (inclusive).
  - `endDate` (optional, format: `yyyy-MM-dd`): Ngày kết thúc lọc đơn hàng (inclusive).
  *(Lưu ý: Các chỉ số baseline `totalUsers`, `totalShops`, `totalProducts` luôn phản ánh toàn bộ quy mô hệ thống).*
- **Response Structure (`200 OK`)**:
```json
{
  "status": 200,
  "message": "Lấy thống kê dashboard thành công",
  "data": {
    "totalUsers": 32,
    "totalShops": 9,
    "totalProducts": 11,
    "totalOrders": 9,
    "ordersByStatus": {
      "PENDING": 0,
      "CONFIRMED": 4,
      "SHIPPED": 1,
      "DELIVERED": 4,
      "CANCELLED": 0
    },
    "deliveredRevenue": 29990300.00,
    "averageOrderValue": 7497575.00,
    "dailyChart": [
      {
        "date": "2026-09-09",
        "orderCount": 9,
        "revenue": 0
      }
    ],
    "recentOrders": [ ... ]
  }
}
```

---

## 3. Giao diện Frontend (`AdminDashboardPage.tsx`)

- **Routing**: Đã thay thế placeholder tại route `/admin` trong `App.tsx` bằng `AdminDashboardPage`.
- **Giao diện hiện đại**:
  - 6 thẻ KPI nổi bật với icon màu sắc trực quan (Users, Shops, Products, Orders, Delivered Revenue, AOV).
  - Bộ lọc thời gian linh hoạt (Từ ngày - Đến ngày) kèm các nút bấm nhanh: **Hôm nay**, **7 ngày qua**, **30 ngày qua**, **Tất cả**.
  - Thanh phân bố trạng thái đơn hàng (Order Status Breakdown) với thanh tiến trình màu sắc tương ứng từng trạng thái.
  - Biểu đồ cột khối lượng đơn hàng hàng ngày (Daily Volume Bar Chart) hiển thị trực quan không phụ thuộc thư viện bên ngoài.
  - Bảng 10 đơn hàng gần đây với badge trạng thái, phương thức thanh toán và format tiền tệ VNĐ chuẩn xác.

---

## 4. Kết quả kiểm thử & Xác thực (Verification)

### 4.1. Unit & Integration Tests Backend
- **Tạo mới test suite**: `AdminStatisticsIntegrationTest` với **6/6 test cases pass**:
  1. `unauthenticatedRequest_Returns401or403`: Kiểm tra chặn truy cập khi chưa xác thực.
  2. `nonAdminRole_ReturnsForbidden403`: Kiểm tra chặn quyền `ROLE_USER` và `ROLE_VENDOR`.
  3. `adminAccess_SucceedsWith200AndValidStructure`: Xác thực payload `200 OK` đầy đủ cấu trúc.
  4. `statisticsAccuracy_DeliveredRevenueOnlyAndAOVCalculation`: Xác minh doanh thu chỉ tính từ đơn `DELIVERED` và AOV chuẩn xác.
  5. `dateRangeFiltering_FiltersOrdersAndKeepsBaselineCounts`: Xác minh khoảng ngày lọc đơn hàng chính xác trong khi baseline counts không bị ảnh hưởng.
  6. `recentOrders_ReturnsLatestOrders`: Xác minh danh sách 10 đơn hàng mới nhất có sắp xếp theo thời gian giảm dần.
- **Full Backend Regression Suite**:
  - **278/278 tests pass** (100% BUILD SUCCESS trên toàn bộ các modules: Order, Product, Identity, Shop, Payment, Voucher, Notification, Review, Statistics).

### 4.2. Frontend Build & Linter
- `npm run build`: **PASS** (`tsc -b && vite build` hoàn tất không lỗi cú pháp hoặc Type error).
- `npm run lint`: **0 errors** trên 93 files.

### 4.3. Xác thực Live API
- Chạy script kiểm tra thực tế `verify_dashboard.ps1` trên backend port 8080:
  - Đăng nhập tài khoản Admin `admin@ecommerce.com` thành công.
  - Gọi endpoint All-Time và Filtered (7 ngày qua): trả về dữ liệu chính xác, phân bổ trạng thái khớp 100% với đơn hàng thực tế trong cơ sở dữ liệu.
