# MASTER SYSTEM RE-AUDIT — CURRENT SOURCE (2026-09-11)

## 1. Executive Summary
Hệ thống hiện tại (source-first) đã hoàn thiện đáng kể so với báo cáo cũ. Các chức năng cốt lõi (Auth, Checkout, Payment IPN, Chat, Notification) hoạt động ổn định và có kiểm thử. Đặc biệt, việc khám phá sâu source code cho thấy tính năng F10 (Hoàn tiền VNPay) đã được phát triển backend khá hoàn chỉnh (có Ledger, API, Unit Test) nhưng thiếu tích hợp UI Frontend và thiếu bằng chứng chạy thực tế Sandbox. F12 (Trả hàng) hoàn toàn chưa có. Kiến trúc Modular Monolith được tuân thủ nghiêm ngặt.

## 2. Current Feature Status Matrix
| Feature | Old Status | Current BE | Current FE | FE/BE Contract | Tests | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Auth/Identity | COMPLETED | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |
| Checkout & IPN | COMPLETED | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |
| Delivery Confirm (F11)| PARTIAL | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |
| Payment Refund (F10)| MISSING | IMPLEMENTED | MISSING | MISMATCH (FE MISSING) | PARTIAL (Unit only)| BE ONLY / UNVERIFIED |
| Return (F12) | MISSING | MISSING | MISSING | N/A | NONE | BLOCKED BY D10 |
| Chat (STOMP) | PLANNED | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |
| Notification | PLANNED | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |
| Dashboard Stats | PLANNED | IMPLEMENTED | IMPLEMENTED | MATCH | VERIFIED | VERIFIED COMPLETE |

## 3. Old Report vs Current Source Changes
- **Cải tiến rõ rệt**: WebSockets cho Chat/Notification; Admin có quyền tác động trạng thái Order (Admin mutation); Delivery được kiểm soát bởi Buyer (D8).
- **Phát hiện mới**: F10 Refund đã được code backend (gồm `RefundAttempt`, `RefundService`, `AdminRefundController`) và có Unit Tests (`VnPayRefundGatewayTest`, `RefundReservationServiceTest`), nhưng Frontend chưa hề có giao diện xử lý F10. Báo cáo cũ chưa cập nhật tiến độ này.

## 4. FE/BE Contract Audit
- Mọi luồng chính (Auth, Product, Checkout, Dashboard) hoàn toàn khớp `Contract (FE ↔ BE)`.
- **Mismatch**: Backend mở cổng API F10 (`POST /api/v1/admin/orders/{id}/refunds`) nhưng Frontend không hề có nút hay Form gọi API này.

## 5. Business Flow Audit
- **Checkout & Caching**: Chạy thông suốt.
- **Delivery (F11)**: Hoạt động đúng chuẩn D8 (`BUYER + SHIPPED -> DELIVERED`).
- **Refund (F10)**: Bị đứt gãy tại bước thực thi do Admin không có giao diện.

## 6. Security Audit
- Xác thực/phân quyền (JWT, `@PreAuthorize`) hoạt động chính xác. IPN sử dụng Pessimistic Locking hiệu quả chống race conditions. 

## 7. Architecture Audit
- Tôn trọng triệt để Modular Monolith. `RefundAttempt` được khoanh vùng đúng trong module `payment`. Giao tiếp qua `OrderInternalService`.

## 8. F10 Detailed Status
- **Backend Code**: Đã tồn tại (Ledger, API, Gateway adapter).
- **Unit Tests**: Có (`VnPayRefundGatewayTest`, `RefundReservationServiceTest`).
- **FE Integration**: THIẾU HOÀN TOÀN.
- **Sandbox Execution Evidence**: KHÔNG CÓ (Chưa có bằng chứng chạy sandbox thực tế thành công).

## 9. F11 Detailed Status
- Tuân thủ chính xác D8: Buyer trực tiếp chuyển trạng thái `SHIPPED -> DELIVERED`. (VERIFIED)

## 10. F12/D10 Status
- BLOCKED BY D10. Không có bất kỳ code nào liên quan đến Return.

## 11. Test & Verification Status
- Unit / Integration Tests Backend: Có sẵn và pass với các luồng cốt lõi. F10 thiếu Integration Test.
- Frontend Build: Pass.

## 12. Findings by Severity
- **P0 Critical**: Không có.
- **P1 High**: F10 (Hoàn tiền) đứt gãy luồng do thiếu Frontend UI và bằng chứng Sandbox Integration.
- **P2 Medium**: F12 (Trả hàng) bị chặn chờ chính sách D10.
- **P3 Low**: Hoàn thiện nốt báo cáo tài liệu.

## 13. Prioritized Backlog
1. Tích hợp UI Frontend cho F10 (Admin Refund UI).
2. Viết Integration Test cho luồng F10.
3. Chạy Sandbox VNPay thực tế để verify F10.
4. Chờ D10 để làm F12.

## 14. Staged Roadmap
- **Stage 0 — Stabilization**: Dọn dẹp mismatch.
- **Stage 1 — F10 Sandbox Refund (Integration & FE)**: Xây dựng Admin UI cho Refund, gọi API đã có ở BE, và kiểm chứng thực tế bằng Sandbox VNPay.
- **Stage 2 — F12 Return**: Triển khai D10 Return Flow.
- **Stage 3 — Remaining Improvements**: Recommendation & Caching.
- **Stage 4 — Final Regression**: Chạy E2E, cập nhật Báo cáo `FINAL_REPORT_UPDATED.md`.

## 15. Exact Next Action
Bắt đầu **Stage 1 — F10 Sandbox Refund (Integration & FE)**: Triển khai giao diện Admin Refund (Frontend) để có thể gọi API Backend có sẵn, sau đó thực thi và xác minh Sandbox VNPay.
