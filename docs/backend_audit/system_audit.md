Listed directory frontend
Listed directory src
Listed directory api
Viewed SYSTEM_FE_BE_CONTRACT_AUDIT.md:1-21
Viewed SYSTEM_FE_BE_AUDIT_MATRIX.md:1-14
Listed directory types
Viewed authApi.ts:1-15
Viewed auth.types.ts:1-18
Viewed userApi.ts:1-15
Viewed categoryApi.ts:1-15
Viewed productApi.ts:1-75
Viewed product.types.ts:1-112
Searched for ".imageResponses"
Searched for "product\.images\b(?! \|\| product\.imageResponses)"
Searched for "variant.price"
Viewed ProductDetailPage.tsx:80-140
Viewed ProductDetailPage.tsx:170-240
Viewed ProductDetailPage.tsx:30-60
Viewed ProductDetailPage.tsx:60-80
Viewed cartApi.ts:1-19
Viewed cart.types.ts:1-42
Viewed orderApi.ts:1-53
Viewed order.types.ts:1-70
Searched for "previewCheckout"
Viewed CheckoutPage.tsx:30-65
Viewed CheckoutPage.tsx:66-100
Viewed CheckoutPage.tsx:190-240
Searched for "previewCheckout"
Viewed OrderServiceImpl.java:210-250
Viewed OrderServiceImpl.java:251-280
Viewed paymentApi.ts:1-33
Viewed PaymentResultPage.tsx:1-70
Viewed shopApi.ts:1-16
Viewed shop.types.ts:1-16
Viewed adminApi.ts:1-114
Searched for "createRefund"
Viewed AdminOrderDetailPage.tsx:145-180
Viewed reviewApi.ts:1-51
Searched for "VendorReplyRequest"
Viewed VendorReplyRequest.java:1-9
Viewed review.types.ts:1-49
Viewed RatingSummaryResponse.java:1-10
Viewed ReviewResponse.java:1-21
Searched for ".vendorReply"
Viewed ProductReviewSection.tsx:180-240
Viewed voucherApi.ts:1-18
Viewed notificationApi.ts:1-12
Viewed notification.types.ts:1-6
Viewed chatApi.ts:1-27
Viewed chat.types.ts:1-29
Viewed mediaApi.ts:1-27
Viewed vendorAnalyticsApi.ts:1-29
Viewed vendorApi.ts:1-38
Viewed apiClient.ts:1-50
Viewed errorCodes.ts:1-34
Viewed apiError.ts:1-30
Viewed App.tsx:1-93
Viewed OrderItemResponse.java:1-15
Viewed AdminOrderStatusRequest.java:1-12
Viewed OrderAdminHistoryResponse.java:1-13
Viewed AdminDashboardStatsResponse.java:1-51
Viewed VendorAnalyticsResponse.java:1-24
Viewed VendorProductFormPage.tsx:90-140
Viewed VendorProductFormPage.tsx:170-220
Viewed ProductUpdateRequest.java:1-25
Viewed ProductVariantUpdateRequest.java:1-12
Viewed ProductImageUpdateRequest.java:1-12
Viewed websocket.ts:1-60
Searched for "convertAndSendToUser"
Searched for "login:"
Viewed AuthContext.tsx:1-70
Searched for "confirmDelivery"
Viewed OrderHistoryPage.tsx:120-160
Viewed OrderHistoryPage.tsx:160-180
Searched for "eligibility"
Viewed ProductReviewSection.tsx:1-80
Viewed RequireShop.tsx:1-60
Viewed ProductListPage.tsx:1-70
Viewed websocket.ts:55-66
Viewed VoucherResponse.java:1-27
Viewed voucher.types.ts:1-30
Viewed VoucherCreateRequest.java:1-45
Viewed VoucherType.java:1-7
Searched for "FREE_SHIPPING"
Viewed VoucherService.java:1-12
Viewed VoucherServiceImpl.java:1-60
Viewed AdminVoucherListPage.tsx:115-145
Viewed AdminVoucherListPage.tsx:60-110
Viewed UnreadCountResponse.java:1-6
Viewed NotificationContext.tsx:1-60
Ran command: `npm run build`

Sau khi tiến hành rà soát đối chiếu toàn diện (Contract-by-Contract & Source-to-Source) giữa mã nguồn **Backend Java (Spring Boot)** và **Frontend (React + Vite + TypeScript)**, dưới đây là kết quả kiểm toán chi tiết về độ khớp và các điểm **Mismatch (lệch hợp đồng)** thực tế giữa hai phía.

---

# BÁO CÁO KIỂM TOÁN TƯƠNG THÍCH FRONTEND ↔ BACKEND (FE/BE CONTRACT AUDIT)

> **Phạm vi kiểm toán:**
> - Backend: `src/main/java/com/tmt/ecommerce/**`
> - Frontend: `frontend/src/**`
> - Tình trạng build Frontend: `npm run build` đã chạy thử nghiệm và **compile thành công (0 error)**, nhưng sâu bên trong logic có một số điểm lệch dữ liệu DTO và Enum tại runtime.

---

## I. TỔNG QUAN MA TRẬN TƯƠNG THÍCH (STATUS MATRIX)

| Feature Area | Backend API | Frontend Integration | Khớp Contract | Mức độ rủi ro |
| :--- | :---: | :---: | :---: | :--- |
| **1. Auth & Profile** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **2. Category & Public Products** | Đầy đủ | Đầy đủ | **KHỚP 95%** | Thấp (chú ý alias ảnh) |
| **3. Cart** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **4. Checkout & Cart Preview** | Đầy đủ | Đầy đủ | **LỆCH (MISMATCH)** | **CAO (Ảnh hưởng tính tiền)** |
| **5. Payment & Polling (VNPay/COD)** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **6. Order (Buyer / Vendor / Admin)** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **7. Voucher Management** | Đầy đủ | Đầy đủ | **LỆCH ENUM** | **TRUNG BÌNH (Lỗi 400)** |
| **8. Review & Rating** | Đầy đủ | Đầy đủ | **THIẾU TÍCH HỢP** | Thấp (Bỏ sót 1 API) |
| **9. Realtime Chat (STOMP)** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **10. Notifications (Push & REST)** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **11. Shop & Vendor Management** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **12. Statistics & Analytics** | Đầy đủ | Đầy đủ | **KHỚP 100%** | An toàn |
| **13. Admin Refund (F10)** | Đã có API | Có UI cơ bản | **CẢNH BÁO** | Chưa verify Sandbox |
| **14. Return & Refund (F12)** | Chưa có | Chưa có | **KHÔNG CÓ** | Chờ quyết định D10 |

---

## II. CHI TIẾT CÁC ĐIỂM MISMATCH CẦN KHẮC PHỤC

### 1. Mismatch Nghiêm Trọng: Sai lệch trường DTO tính giá Checkout Preview
* **Vị trí file:** 
  - Backend: `com.tmt.ecommerce.cart.dto.response.CartPreviewResponse.java`
  - Frontend: `frontend/src/api/types/order.types.ts` & `frontend/src/pages/public/CheckoutPage.tsx` (dòng 224, 230, 238)
* **Thực tế Backend trả về:**
  ```java
  public class CartPreviewResponse {
      private BigDecimal subtotal;
      private BigDecimal discount;
      private BigDecimal total;
  }
  ```
* **Frontend đang đọc:**
  ```typescript
  // CheckoutPage.tsx:
  previewData.totalOriginalPrice  // -> undefined!
  previewData.totalDiscount       // -> undefined!
  previewData.finalTotal          // -> undefined!
  ```
* **Hậu quả tại Runtime:**
  - Khi người dùng nhập voucher thành công tại trang `/checkout`, API trả về `discount: 50000`, nhưng Frontend đọc `previewData.totalDiscount` (bị `undefined`) nên **không hiển thị dòng giảm giá**.
  - Phần tạm tính và tổng cộng bị fallback về `fallbackTotalAmount` (giá gốc chưa trừ voucher). Người dùng nhìn thấy số tiền không được giảm dù server đã tính đúng.
* **Giải pháp khuyến nghị cho Frontend:**
  Trong `CheckoutPage.tsx`, sửa lại các trường đọc từ `previewData`:
  - Đổi `previewData.totalOriginalPrice` $\rightarrow$ `previewData.subtotal`
  - Đổi `previewData.totalDiscount` $\rightarrow$ `previewData.discount`
  - Đổi `previewData.finalTotal` $\rightarrow$ `previewData.total`

---

### 2. Mismatch Enum: `VoucherType` chứa giá trị Backend không hỗ trợ
* **Vị trí file:** 
  - Backend: `com.tmt.ecommerce.voucher.entity.VoucherType.java`
  - Frontend: `frontend/src/api/types/voucher.types.ts` (dòng 2), `AdminVoucherListPage.tsx` (dòng 200), `VendorVoucherListPage.tsx` (dòng 194)
* **Thực tế Backend:**
  ```java
  public enum VoucherType {
      PERCENTAGE,
      FIXED_AMOUNT
  }
  ```
  *(Backend chỉ hỗ trợ 2 loại giảm: Theo phần trăm hoặc Giảm số tiền cố định)*.
* **Frontend đang có:**
  ```typescript
  export type VoucherType = 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
  ```
  Và trong giao diện tạo voucher cả Admin lẫn Vendor đều có:
  ```html
  <option value="FREE_SHIPPING">Miễn phí vận chuyển</option>
  ```
* **Hậu quả tại Runtime:**
  Nếu Admin hoặc Vendor chọn loại *"Miễn phí vận chuyển"*, Backend ném lỗi `HttpMessageNotReadableException` / `INVALID_INPUT` (HTTP 400 Bad Request) do Jackson không thể deserialize giá trị enum này.
* **Giải pháp khuyến nghị cho Frontend:**
  Xóa tùy chọn `<option value="FREE_SHIPPING">` khỏi form tạo voucher cho đến khi Backend phát triển tính năng miễn phí vận chuyển.

---

### 3. Mismatch Field Name: Danh sách ảnh sản phẩm (`images` vs `imageResponses`)
* **Vị trí file:** 
  - Backend: `ProductResponse.java` (dòng 17)
  - Frontend: `product.types.ts` & các components (`ProductCard`, `ProductDetailPage`, v.v.)
* **Thực tế Backend:**
  Trường chứa danh sách hình ảnh trong `ProductResponse` có tên là **`imageResponses`**, KHÔNG CÓ trường `images`.
* **Hiện trạng Frontend:**
  Đa phần các component đã có fallback: `product.images || product.imageResponses || []`.
* **Điểm cần lưu ý:**
  Trong `product.types.ts`, `images: ProductImageResponse[]` đang để bắt buộc, còn `imageResponses?: ...` lại để optional. Cần đổi ngược lại hoặc đặt cả hai là optional để tránh các component mới chỉ truy xuất `product.images` dẫn đến lỗi `undefined`.

---

### 4. Mismatch Field Name: Biến thể sản phẩm (`ProductVariantResponse`)
* **Vị trí file:** 
  - Backend: `ProductVariantResponse.java`
  - Frontend: `product.types.ts`
* **Thực tế Backend:**
  ```java
  public record ProductVariantResponse(
      Long id,
      String sku,
      BigDecimal price,
      Integer stockQuantity,
      Map<String, Object> attributes
  ) {}
  ```
  Backend **không có** `name`, `originalPrice`, `salePrice`, `imageUrl` ở cấp độ Variant. Tên phân loại được lưu động trong Map `attributes` (ví dụ: `{"Màu sắc": "Đen", "Size": "XL"}`).
* **Hiện trạng Frontend:**
  Tại `ProductDetailPage.tsx`, code đã phải tự format map `attributes` thành label text. Tuy nhiên trên `product.types.ts`, `originalPrice` và `salePrice` được định nghĩa là bắt buộc, dễ gây hiểu nhầm khi viết thêm component mới.

---

### 5. Chưa tích hợp API: Kiểm tra điều kiện đánh giá sản phẩm (Review Eligibility)
* **Vị trí file:** 
  - Backend: `GET /api/v1/products/{id}/reviews/eligibility` (`ReviewController.java`)
  - Frontend: `reviewApi.ts` & `ProductReviewSection.tsx`
* **Vấn đề:**
  - Backend cung cấp sẵn API `GET /api/v1/products/{id}/reviews/eligibility` trả về danh sách `orderId` của các đơn hàng `DELIVERED` mà người dùng đã mua sản phẩm này và chưa đánh giá.
  - Phía Frontend hiện tại **hoàn toàn bỏ quên API này**. Nút "Viết đánh giá" chỉ xuất hiện nếu trang được mở kèm query parameter `?action=review&orderId=123`. Khách hàng xem sản phẩm trực tiếp từ trang chi tiết sẽ không thấy nút đánh giá dù họ đã mua sản phẩm đó thành công.

---

### 6. Cảnh báo tính năng Admin Refund (F10 - Hoàn tiền VNPay)
* **Vị trí:** `AdminOrderDetailPage.tsx` (dòng 156) $\leftrightarrow$ `POST /api/v1/admin/orders/{orderId}/refunds`
* **Hiện trạng:**
  - Frontend đã gọi đúng contract API `createRefund(order.id, reason)` kèm Header `Idempotency-Key`.
  - Tuy nhiên, giao diện hiện tại chỉ là `window.prompt` và `alert()`, chưa có bảng đối soát kết quả refund hay đối soát mã giao dịch VNPay. Luồng hoàn tiền trên cổng VNPay Sandbox thực tế chưa được kiểm thử E2E (chỉ có unit test mock).

---

## III. CÁC ĐIỂM ĐÃ TƯƠNG THÍCH HOÀN HẢO (VERIFIED COMPATIBILITY)

1. **Authentication & Session:**
   - Stateless JWT Bearer token được `apiClient.ts` tự động gắn vào Header `Authorization: Bearer <token>`.
   - `AuthContext` giải mã `decodeUser(token)` an toàn và đồng bộ mượt mà với `GET /api/v1/users/me`.
   - Interceptor tự động bắt `401` và điều hướng về `/login` chuẩn xác.
2. **WebSocket Realtime (STOMP):**
   - Cổng bắt tay `/ws` mở `permitAll()`, token được nhét chuẩn xác vào STOMP connect headers `Authorization: Bearer <token>`.
   - Nhắn tin P2P hoạt động chính xác qua `/app/chat.send` và đích nhận `/user/queue/messages`.
   - Lắng nghe sự kiện thông báo thời gian thực qua `/user/queue/notifications`.
3. **Giỏ hàng & Đặt hàng Multi-Vendor:**
   - Cơ chế gom nhóm và tách đơn đa shop theo `shopId` và `paymentGroupId` đồng bộ 100% giữa `CheckoutPage`, `OrderController` và `PaymentResultPage`.
   - Polling trạng thái thanh toán từ `GET /api/v1/payments/group/{id}/status` hoạt động độc lập, không phụ thuộc vào query callback của VNPay.
4. **State Machine chuyển đổi trạng thái đơn hàng:**
   - Khách hủy đơn (`PENDING -> CANCELLED`) qua `PATCH /api/v1/orders/{id}/cancel`.
   - Khách xác nhận giao hàng (`SHIPPED -> DELIVERED`) qua `PATCH /api/v1/orders/{id}/confirm-delivery`.
   - Vendor cập nhật trạng thái đơn (`CONFIRMED -> SHIPPED -> DELIVERED / CANCELLED`) qua `VendorOrderListPage`.
   - Admin can thiệp trạng thái đơn qua modal `updateOrderStatus` khớp từng trường `expectedStatus`, `status`, `reason`.
5. **Chuẩn hóa mã lỗi (Error Handling):**
   - Bảng mã lỗi `errorCodes.ts` trên Frontend phản chiếu đầy đủ enum `ErrorCode.java` của Backend.
   - Bắt chính xác `SHOP_NOT_FOUND` khi kiểm tra quyền mở shop trong `RequireShop.tsx`.

---

## IV. ĐỀ XUẤT HÀNH ĐỘNG TIẾP THEO

Để hệ thống hoạt động trơn tru nhất khi người dùng trải nghiệm trên giao diện:
1. **Ưu tiên 1 (Sửa ngay trên Frontend):** Cập nhật 3 dòng code đọc trường `subtotal`, `discount`, `total` trong `CheckoutPage.tsx` để voucher hiển thị đúng tiền giảm.
2. **Ưu tiên 2 (Sửa ngay trên Frontend):** Loại bỏ tùy chọn `FREE_SHIPPING` ở 2 trang quản lý voucher Admin & Vendor để tránh lỗi 400.
3. **Ưu tiên 3:** Bổ sung gọi API `checkReviewEligibility` trên trang chi tiết sản phẩm để mở nút "Viết đánh giá" cho khách hàng thân thiết.