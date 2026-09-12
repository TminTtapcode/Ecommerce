# P2-02 — Rule-based Recommendation

## Verification update — 2026-09-10

This update supersedes the earlier statement that the backend integration test was blocked. `RelatedProductsIntegrationTest` passed on the isolated MySQL schema `ecommerce_s103_s104_s105_s108`: **5 tests, 0 failures, 0 errors**. The checked cases cover exact-category and cross-shop eligibility, seed/HIDDEN/BANNED exclusion, default/clamped/invalid limits, no-fallback cold start, and no duplicate product when variants exist. Frontend `npm.cmd run build` passed; `npm.cmd run lint` exited 0 with 18 pre-existing warnings outside P2-02.

F13 is **IMPLEMENTED with scoped verification PASS**. It remains short of project-wide final verification until the configured full Maven regression/package and an explicit query-count/N+1 measurement are completed. No migration, tracking, personalization, Order/Cart/Review/Payment dependency, F10 dependency, or F12 dependency was introduced.

Ngày cập nhật: **2026-09-10**. Trạng thái: **đã implement backend/frontend theo Decision Set; backend integration verification đang chờ chạy lại do Maven test environment bị chặn.** Không thêm migration, tracking, view history, wishlist hoặc personalization.

## 1. Contract đã triển khai

- API public: GET /api/v1/products/{productId}/related?limit=x.
- Seed phải là Product ACTIVE thuộc shop không BANNED; missing/HIDDEN/BANNED đều trả 404 PRODUCT_NOT_FOUND.
- Candidate là Product ACTIVE cùng **exact category**, loại seed và loại toàn bộ shop BANNED.
- Cross-shop được cho phép.
- Không fallback sang category khác. Danh mục thiếu candidate trả danh sách ngắn hơn hoặc rỗng.
- limit default 8; giá trị lớn hơn 8 được clamp xuống 8; giá trị không dương trả 400 INVALID_INPUT.
- Sort id DESC để deterministic. ID không được gắn nhãn ngày tạo hoặc popularity.
- UI chỉ xuất hiện ở cuối ProductDetailPage; không sửa catalog “Gợi Ý Hôm Nay”.
- Không dùng Order, Cart, Review, Voucher hay Payment. Recommendation không phải AI/personalized/best-seller.

## 2. Backend

~~~
GET /api/v1/products/{id}/related
  → ProductController
  → ProductServiceImpl.getRelatedProducts
  → kiểm seed public eligibility dùng chung với product detail
  → ProductRepository exact category + ACTIVE + exclude seed
  → ShopInternalService banned IDs
  → ProductResponse[]
~~~

- ProductServiceImpl dùng một helper cho public seed eligibility của detail và related, tránh việc một API coi product BANNED là public còn API kia không.
- Query repository áp dụng PageRequest(0, min(limit, 8), id DESC). Khi có banned shops, điều kiện loại shop nằm trong query DB; không lấy candidate rộng rồi cắt trong Java.
- Repository chỉ fetch category. Variants/images giữ batch loading hiện có để tránh fetch hai collection bag cùng lúc.
- Không đổi SecurityConfig: GET /api/v1/products/** vốn đã là public.

## 3. Frontend

- productApi.getRelatedProducts(id, limit?) gọi endpoint mới.
- RelatedProductsSection dùng lại ProductCard, grid 2/3/4 cột, mặc định tối đa 8 item.
- Có loading skeleton, empty state và lỗi/retry cục bộ.
- Mỗi lần product ID đổi, request cũ bị vô hiệu hóa. Response cũng lọc lại seed như một hàng rào UI bổ sung.
- Lỗi recommendation không ảnh hưởng request hoặc error state của ProductDetail.

## 4. Tests và evidence

### Đã thêm

RelatedProductsIntegrationTest thiết kế 5 HTTP/MySQL cases:

1. Exact category, cross-shop, ID DESC, loại seed/HIDDEN/BANNED/category khác.
2. Default 8, clamp limit lớn và không fallback khi category rỗng.
3. Limit 0, âm và malformed trả S1-02 error envelope.
4. Seed missing/HIDDEN/BANNED đều trả cùng PRODUCT_NOT_FOUND để không rò rỉ eligibility.
5. Product nhiều variants vẫn xuất hiện đúng một lần.

### Đã chạy

| Check | Kết quả |
|---|---:|
| Frontend TypeScript + Vite build | PASS |
| Frontend oxlint | Exit 0; 18 warnings có sẵn, không thuộc P2-02 |
| Frontend headless HTTP smoke (vite preview) | PASS: / = 200, /products/1 = 200 |
| Maven main-source compile | PASS |
| Maven test compile / RelatedProductsIntegrationTest execution | Chưa có kết quả |

Maven wrapper của workspace lỗi nội bộ trước khi chạy. Maven trực tiếp tới bước test compilation nhưng sandbox không thể đọc jakarta.mail-api từ local cache. Yêu cầu chạy ngoài sandbox đã bị automatic approval review từ chối vì account công cụ đạt usage limit; vì vậy không thay bằng cách chạy vòng hoặc công bố integration test là pass.

## 5. Files changed

- src/main/java/com/tmt/ecommerce/product/controller/ProductController.java
- src/main/java/com/tmt/ecommerce/product/service/ProductService.java
- src/main/java/com/tmt/ecommerce/product/service/ProductServiceImpl.java
- src/main/java/com/tmt/ecommerce/product/repository/ProductRepository.java
- src/test/java/com/tmt/ecommerce/product/RelatedProductsIntegrationTest.java
- frontend/src/api/productApi.ts
- frontend/src/components/products/RelatedProductsSection.tsx
- frontend/src/pages/public/ProductDetailPage.tsx
- docs/PHASE2_P2_02_RECOMMENDATION_DESIGN.md
- docs/PHASE2_P2_02_RECOMMENDATION.md

## 6. Migration, rollback and limits

- Không có schema/data migration.
- Feature read-only; rollback ẩn UI/route mới và giữ catalog hiện có. Không có checkout/order state cần đảo.
- Chưa cập nhật PHASE2_IMPLEMENTATION_PLAN.md thành completed cho đến khi suite backend mới và full regression được chạy thành công.
- Chưa chứng minh query count/N+1 thực nghiệm hoặc full API behavior do integration run bị chặn. Đây là verification còn lại, không phải giới hạn nghiệp vụ của contract.
- Không mở rộng thành personalization, product popularity hoặc fallback cross-category trong P2-02.

## 7. Verification còn lại

1. Chạy RelatedProductsIntegrationTest trên MySQL schema test cách ly.
2. Chạy full configured Maven regression và package.
3. Kiểm query count/N+1 cho mapping 8 related products.
4. Thực hiện smoke HTTP/browser với seed active, category rỗng, hidden seed và banned-shop seed.
5. Khi toàn bộ pass, cập nhật Implementation Plan và trạng thái report thành completed.
