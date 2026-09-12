# PRINT_REPORT_CUT_LIST.md
# Danh sách cắt giảm — REPORT_PRINT_VERSION.md

> So sánh giữa REPORT_FULL_DETAIL.md và REPORT_PRINT_VERSION.md

---

## PHẦN GIỮ NGUYÊN ✅

| Phần | Lý do giữ |
|---|---|
| Mở đầu (1 trang) | Bắt buộc theo mẫu báo cáo |
| Chương 1 — Tổng quan đề tài đầy đủ | Core: bài toán, mục tiêu, phạm vi |
| 1.3 Phát biểu bài toán (6 vấn đề kỹ thuật) | Nội dung học thuật quan trọng |
| 1.5 Phạm vi — Trong phạm vi và Ngoài phạm vi | Giúp người đọc hiểu giới hạn |
| 1.6 Phương pháp — phân chia phase | Thể hiện quy trình phát triển |
| 1.7 Khảo sát hệ thống liên quan (Shopee, Tiki, WooCommerce) | Bắt buộc theo báo cáo học thuật |
| Chương 2 — Cơ sở lý thuyết đầy đủ các khái niệm | Nền tảng lý thuyết bắt buộc |
| Bảng so sánh kiến trúc (Monolith / Modular Monolith / Microservices) | Minh họa lý do chọn kiến trúc |
| 2.2 Các phương pháp, mô hình, thuật toán | State Machine, Strategy, Pessimistic Lock, Idempotency |
| 2.3 Tổng quan công nghệ (bảng stack) | Technology choice cần minh bạch |
| 3.1 Giới thiệu hệ thống (đối tượng, chức năng) | Bối cảnh hệ thống |
| 3.2 Kiến trúc (Architecture diagram + module table) | Trọng tâm kỹ thuật |
| 3.2.3 Internal Service contracts | Đặc trưng Modular Monolith |
| 3.2.4 Luồng xử lý tổng quát | Mô tả request flow |
| 3.3 Use Case — Actor table | Xác định actor |
| 3.3.3 Đặc tả 8 Use Case cốt lõi | UC01–UC08 đầy đủ |
| 3.4.1 Class Diagram — danh sách entity + quan hệ | Thiết kế OOP |
| 3.4.2 Sequence Diagram — Checkout, IPN, Chat | 3 luồng phức tạp nhất |
| 3.4.4 State Diagram — Order, Shop | State Machine thực tế |
| 3.5 Thiết kế CSDL — 20 bảng (tổng quan) | ERD bắt buộc |
| 3.5.2 Chi tiết các bảng quan trọng: users, shops, products, categories, product_variants (inventory), carts, cart_items, orders, order_items, payments, vouchers | 11 bảng cốt lõi bắt buộc giữ đầy đủ cấu trúc cột |
| 3.5.4 Các mối quan hệ | Cardinality, FK quan trọng |
| 3.6 Thiết kế giao diện — 3 actor (Customer, Vendor, Admin) | UI evidence |
| 3.7.1 Môi trường triển khai | Stack thực tế |
| 3.7.2 Feature matrix (bảng status) | Minh chứng kết quả |
| 3.7.4 Hạn chế — 4 nhóm | Trung thực học thuật |
| Chương 4 — Kết luận đầy đủ | Bắt buộc |
| 4.3 Hướng phát triển ngắn/trung/dài hạn | Tầm nhìn |
| Tài liệu tham khảo (15 tài liệu) | Bắt buộc |

---

## PHẦN CẮT GIẢM ✂️

| Phần | Từ section | Lý do cắt |
|---|---|---|
| Phần 1.6.3 Phương pháp kiểm thử (chi tiết test name, count) | REPORT_FULL_DETAIL §1.6.3 | Quá chi tiết implementation; giữ tóm tắt 2-3 dòng |
| Phần 1.6.4 Quản lý kiến trúc và tài liệu | REPORT_FULL_DETAIL §1.6.4 | Thông tin nội bộ dự án, không cần cho báo cáo in |
| Phần 2.1.8 Payment IPN — workaround localhost chi tiết | REPORT_FULL_DETAIL §2.1.8 | Giữ khái niệm IPN, cắt workaround |
| Bảng so sánh technology chi tiết (version number từng package) | REPORT_FULL_DETAIL §2.3 | Giữ bảng stack tóm tắt; cắt bảng dependency chi tiết |
| Phần 3.2.3 danh sách cross-module chi tiết (6 dòng) | REPORT_FULL_DETAIL §3.2.3 | Rút gọn còn 3-4 ví dụ tiêu biểu |
| Phần 3.4.3 Activity Diagram — chi tiết 2 diagram | REPORT_FULL_DETAIL §3.4.3 | Giữ placeholder [INSERT DIAGRAM], cắt mô tả dài dòng |
| Phần 3.5.2 Chi tiết cột các bảng phụ: brands, product_images, reviews, review_images, notifications, chat_rooms, chat_messages, roles, user_roles | REPORT_FULL_DETAIL §3.5.2 | Giữ trong bảng 20 bảng tổng quan; cắt bảng chi tiết cột để tiết kiệm trang (chỉ giữ 11 bảng cốt lõi) |
| Phần 3.6.1 mô tả từng component giao diện quá chi tiết | REPORT_FULL_DETAIL §3.6 | Giữ: mục đích + bảng màn hình + ảnh placeholder + luồng chính; cắt: liệt kê chi tiết từng ô input con |
| Phần 3.7.3 danh sách 8 INSERT FIGURE | REPORT_FULL_DETAIL §3.7.3 | Giữ 4 ảnh tiêu biểu nhất (Trang sản phẩm, Checkout, Quản lý đơn Vendor, Duyệt shop Admin) |
| APPENDIX A — Feature Matrix đầy đủ 50 dòng | REPORT_FULL_DETAIL Appendix A | Rút gọn còn bảng tóm tắt 20 dòng trọng tâm |
| APPENDIX B — Database Table Summary (20 bảng) | REPORT_FULL_DETAIL Appendix B | Đã có ở mục 3.5.1 nên cắt bỏ phần phụ lục này |
| APPENDIX C/D/E — Screenshot/Video/Diagram Checklist | REPORT_FULL_DETAIL Appendix C-E | Tài liệu làm việc nội bộ, cắt khỏi bản in nộp |
| Phần phân tích 2.3.x chi tiết từng service (VNPay, Cloudinary, Gmail) | REPORT_FULL_DETAIL §2.3.4-2.3.6 | Rút gọn còn 3-4 dòng mỗi mục |

---

## ƯỚC TÍNH SỐ TRANG

| Chương | Trang ước tính (12pt, Times New Roman, 1.5 line) |
|---|---|
| Bìa + Lời cam đoan + Lời cảm ơn + Mục lục | ~6 trang |
| Mở đầu | ~1 trang |
| Chương 1 — Tổng quan | ~12 trang |
| Chương 2 — Cơ sở lý thuyết | ~14 trang |
| Chương 3 — Xây dựng hệ thống | ~45 trang |
| Chương 4 — Kết luận | ~5 trang |
| Tài liệu tham khảo | ~2 trang |
| Phụ lục (Feature Matrix rút gọn) | ~3 trang |
| **TỔNG** | **~88 trang** |

> Ước tính dựa trên format phổ biến: A4, lề 2.5cm, Times New Roman 13pt, line spacing 1.5, paragraph spacing 6pt.
> Số trang thực tế phụ thuộc vào số lượng hình ảnh và diagram được chèn.
