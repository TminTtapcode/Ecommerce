# P2-02 — Rule-based Recommendation

Ngày inspect: **2026-09-10**. Trạng thái: **Decision Set P2-02 đã được duyệt; implementation đang được kiểm chứng**.
Nguồn đối chiếu: `PHASE2_IMPLEMENTATION_PLAN.md`, `PHASE2_SPRINT1_PLAN.md`, `PHASE2_FEATURE_AUDIT.md`, `PHASE2_DEPENDENCY_GRAPH.md`, `docs/ARCHITECTURE.md`, `docs/PROJECT_CONTEXT.md`, các báo cáo P1/P2-01 và source hiện tại. Khi tài liệu và source khác nhau, source là nguồn quyết định.

## 1. Current implementation

### Backend

- Product module sở hữu `Product`, `ProductVariant`, `Category`, `Brand`, `ProductImage`, `ProductRepository`, `ProductVariantRepository` và `ProductServiceImpl`.
- Public catalog hiện có:
  - `GET /api/v1/products` với keyword, category descendants, price range, sort và paging.
  - `GET /api/v1/products/{id}` cho product `ACTIVE`; product thuộc shop `BANNED` bị trả như không tồn tại.
- `ProductServiceImpl` đã có logic eligibility dùng chung: product phải `ACTIVE`, shop không `BANNED`; filter banned được áp dụng trước paging trong catalog.
- `ProductInternalService` hiện chỉ cung cấp variant info, variant → product, product → shop và stock operations. Chưa có contract lấy batch product/category/eligibility dành cho recommendation.
- Không có package/service/controller/repository recommendation.
- Order lưu `OrderItem.productVariantId`, product name, unit price, quantity, subtotal; không lưu `productId` hoặc category snapshot. `OrderInternalService` chỉ có các contract phục vụ order/review/statistics, chưa có “recent products” hay co-purchase query.
- Cart lưu `productVariantId`, quantity và được map qua `ProductInternalService`; không có view history hoặc wishlist.
- Review lưu `productId`, rating và user/order IDs, nhưng chưa có internal contract cung cấp tín hiệu recommendation và không nên dùng review của người khác để suy ra hồ sơ người dùng.
- Payment group có nhiều order/shop và nhiều payment attempts; không có lý do để recommendation đọc Payment.

### Frontend

- `ProductListPage` gọi catalog API và hiện tiêu đề **“Gợi Ý Hôm Nay”** khi không có keyword. Đây chỉ là danh sách catalog theo sort mặc định `id_desc`, không phải recommendation.
- `ProductDetailPage` tải một product và review; chưa có khối sản phẩm liên quan.
- `ProductCard`, `productApi`, `ProductResponse` và layout public có thể tái sử dụng.
- Chưa có route/API client/type dành cho related products.

## 2. Existing reusable components

Có thể tái sử dụng:

1. **Eligibility và catalog mapping:** `ProductServiceImpl.getAllProducts`, `getProductById`, `ProductRepository` và `ProductResponse` đã xác định product ACTIVE, shop BANNED và mapping category/image/variant.
2. **Paging/DTO conventions:** Spring `Page<ProductResponse>`, `ApiResponse`, public `ProductController` và `productApi`.
3. **UI:** `ProductCard`, loading/error patterns và responsive grid của `ProductListPage`/`ProductDetailPage`.
4. **Module boundary:** Recommendation có thể nằm trong Product module vì baseline chỉ chọn sản phẩm theo category/eligibility. Nếu sau này composition dùng Order/Cart/Review, module mới chỉ được gọi qua internal contracts.
5. **Error/security:** GET public products đã permit trong `SecurityConfig`; related baseline không cần user identity.

Không nên sao chép nguyên `getAllProducts` thành một query riêng nếu chỉ khác điều kiện category và loại trừ product hiện tại. Nên tách predicate/query dùng chung hoặc thêm repository query rõ nghĩa, vẫn lọc eligibility trước giới hạn kết quả.

## 3. Requirements from the plan

`PHASE2_IMPLEMENTATION_PLAN.md` và audit yêu cầu baseline **rule-based cùng category**, loại product hiện tại/không eligible, deterministic fallback và giới hạn query. Popularity, co-purchase và personalized chỉ được cân nhắc sau khi có rule/data contract rõ ràng.

Endpoint được đề xuất trong plan là:

```http
GET /api/v1/products/{productId}/related
```

Baseline không yêu cầu bảng mới, search/view history, wishlist, AI label hay thay đổi checkout. Recommendation là downstream UI; catalog/checkout không được phụ thuộc vào việc endpoint này thành công.

## 4. Recommended baseline business rules

Đây là thiết kế đề xuất để người dùng duyệt, **chưa phải business contract đã chốt**:

| Rule | Đề xuất baseline | Cơ sở source / giới hạn |
|---|---|---|
| Seed | `productId` public path, product seed phải tồn tại và `ACTIVE`, shop không `BANNED` | Phù hợp catalog hiện tại |
| Similarity | Cùng `category_id` với seed | Category là dữ liệu hiện có; không cần history |
| Exclude | Loại seed, product không `ACTIVE`, product của shop `BANNED` | Tránh tự gợi ý item đang bị ẩn/không bán |
| Sort | `id DESC` deterministic, tie-breaker ID | Source chưa có createdAt/popularity contract |
| Limit | Server whitelist, đề xuất 4–8 item, không nhận size tùy ý vượt bound | Giới hạn query và payload |
| Fallback | Nếu category không đủ item, fallback về public ACTIVE catalog không bị banned, vẫn loại seed; thứ tự deterministic | Cần duyệt vì fallback khác semantics “cùng category” |
| Shop scope | Không giới hạn cùng shop; recommendation có thể đến từ shop khác | Catalog multi-vendor; cần duyệt nếu muốn cùng shop |
| Price/variant | Trả `ProductResponse` hiện có; không dùng giá/stock variant để chấm điểm | Tránh thêm ranking chưa có yêu cầu |
| Personalization | Không dùng user/order/cart/review trong baseline | Chưa có tracking/history contract |
| Empty | Trả danh sách rỗng, HTTP 200; không tự coi lỗi là “không có sản phẩm” | UI có empty state |

Baseline không được gọi là “AI”, “personalized” hoặc “best seller”. Nó chỉ là related products theo category với eligibility catalog.

## 5. Security and eligibility

- Endpoint baseline là public GET, không nhận `userId`, `shopId`, order ID hay dữ liệu lịch sử trong query/body.
- `productId` chỉ xác định seed public. Không được trả seed hoặc related item nếu seed/item bị `HIDDEN`, product status khác `ACTIVE`, hoặc shop là `BANNED`.
- Không thêm quyền vendor/admin vào public recommendation. Nếu sau này có personalized endpoint, user identity phải lấy từ `@CurrentUserId`; tuyệt đối không tin `userId` từ client.
- Product module tự truy vấn Product/Category/Shop eligibility thông qua `ShopInternalService.getBannedShopIds()` hoặc contract batch phù hợp; module khác không đọc ProductRepository/ShopRepository trực tiếp.
- Cần quyết định semantics khi seed không tồn tại hoặc không eligible: khuyến nghị trả `PRODUCT_NOT_FOUND`/404 để không làm lộ product bị ẩn/banned, nhất quán với `getProductById` hiện tại.
- Related response không được lộ shop/private data ngoài những gì `ProductResponse` public hiện đã trả.

## 6. API design

### Proposed baseline API

```http
GET /api/v1/products/{productId}/related?limit=6
```

Khuyến nghị `limit` là optional với default 6 và clamp server-side (ví dụ 1–12). Một lựa chọn an toàn hơn là không public `limit` ở bản đầu và cố định 6; cần chốt trước code.

Response giữ envelope hiện có:

```json
{
  "success": true,
  "status": 200,
  "message": "Lấy sản phẩm liên quan thành công",
  "data": [
    {
      "id": 42,
      "shopId": 7,
      "name": "...",
      "categoryId": 3,
      "categoryName": "...",
      "status": "ACTIVE",
      "variants": [],
      "images": []
    }
  ]
}
```

Không tạo DTO entity mới nếu `ProductResponse` đủ cho card hiện có. Nếu payload nhẹ hơn là cần thiết, tạo `RelatedProductResponse` trong Product module, không expose entity.

### Error behavior

- Seed missing/hidden/banned: 404 `PRODUCT_NOT_FOUND` (khuyến nghị, tránh phân biệt hidden/banned).
- `limit` sai hoặc ngoài bound: 400 `INVALID_INPUT` theo S1-02.
- Database/service failure: error handler hiện có; không trả 200 với danh sách rỗng để che lỗi.

## 7. Backend implementation plan

1. Chốt seed eligibility, fallback, limit và cross-shop policy trước khi sửa source.
2. Thêm method public trong `ProductService` và controller route dưới `/api/v1/products/{id}/related`.
3. Reuse eligibility predicate/logic của catalog. Query category + `status=ACTIVE` + excluded banned shops + `id <> seed`, sort ID DESC, bounded result. Nếu query fallback được duyệt, thực hiện query thứ hai chỉ khi category result thiếu.
4. Không gọi Order/Cart/Review trong baseline. Không thêm module mới, migration, tracking table hoặc job.
5. Nếu cần category descendants cho related (thay vì exact category), dùng `CategoryRepository.findDirectChildIds`/batch traversal hiện có hoặc chốt exact category; không âm thầm đổi semantics catalog.
6. Map `ProductResponse` trong Product module và giữ các contract cross-module hiện có không đổi.

## 8. Frontend implementation plan

1. Thêm `getRelatedProducts(productId, limit?)` và type response trong `productApi`/types.
2. Thêm `RelatedProductsSection` dùng `ProductCard` vào `ProductDetailPage`, đặt sau thông tin/review theo UX đã duyệt.
3. Có loading skeleton, empty state và retry/ẩn block khi API lỗi; lỗi recommendation không làm product detail fail.
4. Loại response cũ khi product ID đổi hoặc component unmount; không hiển thị product seed trùng.
5. Giữ catalog “Gợi Ý Hôm Nay” hiện tại cho đến khi có quyết định thay đổi; không tự gọi related API trên ProductListPage.
6. Responsive grid theo pattern public hiện có; không thêm nhãn AI/personalized.

## 9. Test plan before implementation

### Backend/API integration

- Public seed ACTIVE có related cùng category, không có seed trong kết quả, deterministic order và đúng limit.
- Seed category chỉ có chính nó → empty hoặc fallback theo contract đã duyệt.
- Related loại `HIDDEN`, product status khác `ACTIVE`, shop `BANNED`.
- Seed `HIDDEN`/banned/missing trả đúng 404 code, không lộ khác biệt nội bộ.
- Category khác không xuất hiện trong exact-category baseline; cross-shop behavior đúng contract.
- `limit` thiếu, hợp lệ, 0, âm, quá lớn, không phải số; range/date không liên quan.
- Nhiều variants của một product không nhân bản product trong response.
- Buyer/anonymous request được phép theo public policy; vendor/admin không có bypass riêng.
- DB fixture nhiều shop/category và empty category; kiểm query bound/N+1 nếu mapping images/variants.

### Future personalized signals (chưa implement)

Nếu sau này dùng Order/Cart/Review, cần test user A không nhận history của B, co-purchase group semantics (`orderId` hay `paymentGroupId`), cancelled/PENDING eligibility, duplicate products và cold-start. Không viết các test này như thể baseline đã có personalization.

### Frontend

- Detail load success/error/empty/seed change/unmount.
- API error không thay product detail thành error page.
- Card link đúng product, không duplicate seed, responsive at 1/0/n items.
- Build, lint, existing API tests và browser/headless smoke khi implementation được duyệt.

## 10. Files/modules expected to change

**Baseline backend:**

- `src/main/java/com/tmt/ecommerce/product/controller/ProductController.java`
- `src/main/java/com/tmt/ecommerce/product/service/ProductService.java`
- `src/main/java/com/tmt/ecommerce/product/service/ProductServiceImpl.java`
- `src/main/java/com/tmt/ecommerce/product/repository/ProductRepository.java` (chỉ nếu query hiện có không đủ)
- `src/test/java/com/tmt/ecommerce/product/...` hoặc suite integration mới cho related API.

**Baseline frontend:**

- `frontend/src/api/productApi.ts`
- `frontend/src/api/types/product.types.ts` nếu cần type riêng
- `frontend/src/pages/public/ProductDetailPage.tsx`
- `frontend/src/components/products/RelatedProductsSection.tsx` (nếu tách component)
- frontend test/smoke harness tương ứng.

Không dự kiến sửa Order, Cart, Payment, Voucher, Review, Shop schema hoặc `SecurityConfig` cho baseline. Nếu cần sửa ngoài danh sách này phải giải thích dependency và xin duyệt scope.

## 11. Risks and limitations

- `ProductResponse`/variant mapping có thể gây N+1 nếu related query map toàn bộ variants/images; cần kiểm query count trước khi gọi feature hoàn tất.
- Product không có `createdAt`; ID DESC chỉ là deterministic fallback, không được gắn nhãn “mới nhất” nếu semantics chưa chốt.
- Category hiện có parent tree nhưng chưa có category snapshot trong OrderItem; historical personalization không thể suy ra chắc chắn.
- Shop ban eligibility hiện gọi danh sách banned IDs; nếu số shop lớn, query `NOT IN` có thể cần batch/projection sau khi đo, không tối ưu trước bằng kiến trúc mới.
- Fallback toàn catalog có thể làm loãng “related”; cần business decision.
- Không có view history/wishlist/popularity/commission/margin data; không thể tuyên bố cá nhân hóa, best seller hay lợi nhuận.
- Recommendation là read-only; rollback chỉ cần ẩn UI/route và giữ catalog hoạt động. Không có migration baseline.

## 12. Decision Set P2-02 đã duyệt

1. Similarity dùng **exact category**; không quét category descendants.
2. **Không fallback**: category thiếu sản phẩm trả danh sách ngắn hơn hoặc rỗng.
3. Client có thể truyền `limit`; default 8 và server clamp tối đa 8.
4. Related products có thể đến từ mọi shop eligible, không chỉ shop seed.
5. Seed missing, HIDDEN hoặc thuộc shop BANNED trả `404 PRODUCT_NOT_FOUND`.
6. UI chỉ đặt khối **Sản phẩm liên quan** ở cuối `ProductDetailPage`.
7. Không thêm tracking, view history, wishlist hoặc Order/Cart/Review signals trong P2-02.

## 13. Recommended implementation order after approval

1. Backend Product query/service/controller + API integration tests.
2. Frontend API/types/related section + loading/empty/error handling.
3. Regression: product/catalog/ban enforcement/review/cart security; kiểm N+1/query bound.
4. Build/lint, headless/browser smoke ProductDetail với seed ACTIVE, empty category và hidden/banned negative cases.
5. Cập nhật báo cáo P2-02 bằng số test thực chạy, evidence, migration/rollback và limitations; chỉ sau đó cập nhật Implementation Plan.

**Kết luận audit:** P2-02 có thể triển khai an toàn ở baseline same-category, public, non-personalized và không cần migration. Chưa nên thêm Order/Cart/Review signals hoặc “fallback thông minh” trước khi các quyết định ở mục 12 được duyệt.
