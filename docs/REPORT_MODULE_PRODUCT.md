# BÁO CÁO KIỂM TOÁN MÔ ĐUN: PRODUCT (SẢN PHẨM)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

Tài liệu này cung cấp cái nhìn chuyên sâu và rà soát kỹ thuật về Mô đun Sản phẩm (Product Module), một trong những core module quan trọng nhất của hệ thống thương mại điện tử đa người bán. Báo cáo dựa trên việc phân tích mã nguồn thực tế hiện có.

---

## 1. TỔNG QUAN MÔ ĐUN (MODULE OVERVIEW)
Mô đun Product quản lý toàn bộ vòng đời của sản phẩm, từ lúc người bán (Vendor) tạo mới, định giá theo các biến thể (Variants), tải ảnh lên thư viện lưu trữ đám mây (Cloudinary), đến quá trình kiểm duyệt, tìm kiếm, và hiển thị gợi ý (Recommendation) cho người dùng cuối.

## 2. MỤC ĐÍCH NGHIỆP VỤ (BUSINESS PURPOSE)
Xây dựng một danh mục hàng hóa (Catalog) linh hoạt cho phép hàng ngàn người bán khác nhau kinh doanh. Mô đun giải quyết bài toán phức tạp của thương mại điện tử: Một sản phẩm không chỉ có một mức giá, mà giá và lượng tồn kho thay đổi theo tổ hợp màu sắc/kích thước (Biến thể/SKU).

## 3. TRÁCH NHIỆM MÔ ĐUN (MODULE RESPONSIBILITIES)
- Quản lý phân cấp cây Danh mục (Category).
- Quản lý Thương hiệu (Brand).
- Cung cấp API Quản lý thông tin chung của Sản phẩm.
- Quản lý số lượng tồn kho vật lý thông qua ProductVariant.
- Quản lý đồng bộ/xóa hình ảnh trên Cloudinary.
- Cung cấp cơ chế tìm kiếm, lọc (theo giá, tên, danh mục) và gợi ý sản phẩm (Recommendation).

## 4. VỊ TRÍ TRONG KIẾN TRÚC (ARCHITECTURE POSITION)
- Nằm tại tầng lõi (Core Business) trong kiến trúc Modular Monolith.
- **Phụ thuộc vào:** Mô đun `Shop` (Để kiểm tra quyền sở hữu gian hàng trước khi đăng bán), Mô đun `Common` (Xử lý File Storage Cloudinary, Security).
- **Được gọi bởi:** Mô đun `Order` và `Cart` (Lấy thông tin giá và tên sản phẩm để snapshot, gọi `ProductInternalService` để trừ tồn kho khi thanh toán hoàn tất).

## 5. CẤU TRÚC GÓI BACKEND (BACKEND PACKAGE STRUCTURE)
```text
com.tmt.ecommerce.product
├── api/             # Internal Service API giao tiếp liên module
├── controller/      # REST API phục vụ Frontend (Public & Vendor)
├── dto/             # Request/Response Data Transfer Objects
├── entity/          # JPA Entities
├── enums/           # ProductStatus (ACTIVE, HIDDEN)
├── repository/      # Spring Data JPA Repositories
└── service/         # Business Logic Layer
```

## 6. CÁC THỰC THỂ (ENTITIES)
- `Product`: Thực thể chính (Root Entity), chứa thông tin cơ bản.
- `ProductVariant`: SKU vật lý, chứa thuộc tính động JSON (`attributes`), giá và số lượng tồn.
- `ProductImage`: URL ảnh và thông tin vị trí sắp xếp (`sortOrder`), cờ ảnh bìa (`isThumbnail`).
- `Category`: Danh mục đa cấp bậc (Cấu trúc đệ quy `parent_id`).
- `Brand`: Thương hiệu sản phẩm.

## 7. REPOSITORIES
- `ProductRepository`: Xử lý phân trang, tìm kiếm động (Specification logic) với các bộ lọc phức tạp.
- `ProductVariantRepository`: Cung cấp truy vấn `findByIdIn` hỗ trợ nạp dữ liệu giảm thiều N+1 queries.
- `ProductImageRepository`, `CategoryRepository`, `BrandRepository`.

## 8. SERVICES
- `ProductService` & `ProductServiceImpl`: Xử lý nghiệp vụ chính (CRUD, Recommendation).
- `ProductInternalServiceImpl`: Implement API dành riêng cho các mô đun khác gọi (ví dụ kiểm tra sản phẩm có tồn tại không).

## 9. CONTROLLERS
- `ProductController`: Public API (Cho Buyer và Khách vãng lai). Lấy danh sách, chi tiết, gợi ý.
- `VendorProductController`: Vendor API. Bắt buộc kiểm tra JWT Token có chứa Role VENDOR và kiểm tra Shop Ownership.
- `CategoryController`: Public API lấy cây danh mục.

## 10. DTOs
- **Request:** `ProductCreateRequest`, `ProductUpdateRequest`, `ProductVariantRequest`, `ProductImageRequest`.
- **Response:** `ProductResponse`, `ProductVariantResponse`, `ProductImageResponse`, `CategoryResponse`.

## 11. ĐẶC TẢ API (APIs)
- `GET /api/v1/products`: Lọc sản phẩm công khai.
- `GET /api/v1/products/{id}`: Xem chi tiết.
- `GET /api/v1/products/recommendations`: Xem danh sách gợi ý Hybrid (Lịch sử mua + Top bán chạy).
- `POST /api/v1/vendor/products`: Vendor tạo sản phẩm.
- `PUT /api/v1/vendor/products/{id}`: Vendor cập nhật.
- `DELETE /api/v1/vendor/products/{id}`: Vendor ẩn sản phẩm.

## 12. XÁC THỰC DỮ LIỆU (VALIDATION)
- Thực hiện qua `@Valid` tại Controller kết hợp với `jakarta.validation` trong DTOs (`@NotBlank`, `@Min`, `@NotNull`).
- Ràng buộc giá min < max tại `getAllProducts`.

## 13. PHÂN QUYỀN (AUTHORIZATION)
- Public API mở tự do.
- Vendor API chặn bằng `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`. Sử dụng tham số tiêm `@CurrentUserId`.

## 14. KIỂM TRA QUYỀN SỞ HỮU (OWNERSHIP CHECKS)
- **Cơ chế:** Khi Vendor sửa/xóa, service gọi `shopInternalService.isShopOwner(product.getShopId(), userId)`. Nếu sai, ném ngoại lệ `AccessDeniedException` hoặc `IllegalArgumentException`.
- Đảm bảo Vendor A không thể sửa sản phẩm của Vendor B.

## 15. VÒNG ĐỜI VÀ TRẠNG THÁI (LIFECYCLE/STATUS)
- Enum `ProductStatus`: `ACTIVE` (đang bán), `HIDDEN` (tạm ẩn/bị xóa mềm).
- Trạng thái này không ảnh hưởng đến dữ liệu lịch sử đơn hàng do đơn hàng đã snapshot thông tin.

## 16. LUỒNG THÊM/SỬA/XÓA (PRODUCT CRUD WORKFLOW)
- **Tạo mới:** Gửi payload JSON kèm mảng biến thể và mảng hình ảnh. Lưu Product gốc -> Lưu Variants -> Lưu Images.
- **Cập nhật (Chế độ Replace/Sync):** Gửi payload JSON đẩy đủ. Thuật toán kiểm tra `id` của mảng con. Cập nhật mảng con có `id`, thêm mới cái chưa có `id`, và **xóa mảng con không có mặt trong request**.
- **Xóa:** Là kỹ thuật xóa mềm (Soft delete) -> Chuyển status thành `HIDDEN`.

## 17. LUỒNG BIẾN THỂ (PRODUCT VARIANT WORKFLOW)
Mọi sản phẩm bắt buộc phải có ít nhất 1 biến thể (ngay cả sản phẩm không có size). Lượng tồn kho, SKU, Giá được quyết định hoàn toàn ở cấp biến thể để đơn giản hóa giao dịch khi thêm vào Giỏ hàng.

## 18. LUỒNG HÌNH ẢNH (PRODUCT IMAGE WORKFLOW)
- Dữ liệu hình ảnh được lưu thành mảng `ProductImage`.
- Hàm `normalizeThumbnails` tự động đảm bảo chỉ có duy nhất 1 ảnh làm ảnh bìa (thumbnail).
- Hàm `normalizeSortOrders` tự động chèn lại số thứ tự để hiển thị đúng Slider bên Frontend.

## 19. TÍCH HỢP CLOUDINARY (CLOUDINARY INTEGRATION)
Khi cập nhật sản phẩm và Vendor chọn xóa bớt ảnh, hệ thống thu thập danh sách `publicId`. Việc gọi API xóa ảnh trên Cloudinary được đẩy vào `TransactionSynchronizationManager.registerSynchronization(afterCommit)`. **Lý do:** Tránh trường hợp xóa ảnh thành công trên Cloud nhưng Database rollback do lỗi, gây ra tình trạng bất đồng bộ nghiêm trọng (ảnh trong DB còn URL nhưng link die).

## 20. QUYỀN GIAN HÀNG VÀ BỊ CẤM (SHOP OWNERSHIP & BAN LIST)
Tại các câu truy vấn công khai, hệ thống luôn gọi ngầm `shopInternalService.getBannedShopIds()`. Nếu Shop đang bị khóa (`BANNED`), toàn bộ sản phẩm của shop đó tự động tàng hình khỏi hệ thống tìm kiếm mà không cần phải can thiệp thay đổi Status của từng sản phẩm.

## 21. TÌM KIẾM, LỌC VÀ DANH MỤC CON (LISTING/SEARCH/FILTER)
- Sử dụng JPA Specification.
- Hỗ trợ DFS/BFS tìm kiếm sản phẩm đệ quy theo cây danh mục (Ví dụ: Tìm "Quần áo" sẽ ra cả "Áo thun" và "Quần Jean" bên trong).
- Chống tấn công SQL Injection bằng cách escape thủ công các ký tự `%` và `_` khi tìm kiếm `keyword`.

## 22. GỢI Ý SẢN PHẨM (RECOMMENDATION)
- Thuật toán Gợi ý Lai (Hybrid).
- **Bước 1:** Content-Based cá nhân hóa dựa trên lịch sử mua hàng của userId (`getRecentlyPurchasedVariantIds`).
- **Bước 2:** Bổ sung bằng Top sản phẩm bán chạy nhất hệ thống.
- **Bước 3:** Lấp đầy khoảng trống bằng sản phẩm mới nhất nếu chưa đủ limit.

## 23. FRONTEND PAGES
- `pages/public/ProductListPage.tsx` (Tìm kiếm công khai).
- `pages/public/ProductDetailPage.tsx` (Xem chi tiết).
- `pages/vendor/VendorProductListPage.tsx` (Danh sách nội bộ của người bán).
- `pages/vendor/VendorProductFormPage.tsx` (Tạo/Sửa sản phẩm cho người bán).
- `pages/admin/AdminProductListPage.tsx`.

## 24. FRONTEND COMPONENTS
- `components/products/ProductCard.tsx` (Hiển thị thẻ UI dùng chung).
- `components/products/RelatedProductsSection.tsx`.
- `components/reviews/ProductReviewSection.tsx` (Nhúng phần đánh giá vào trang chi tiết).

## 25. FRONTEND API CLIENTS
- File `api/productApi.ts` định nghĩa axios calls.
- Typescript Interface tại `api/types/product.types.ts`.

## 26. GIAO KẾT BE/FE (FE → BE FLOW)
- **Mismatch / Lưu ý hợp đồng:** Việc tải ảnh lên Cloudinary được FE thực hiện độc lập (trực tiếp qua widget hoặc endpoint media) trước khi gọi API Product. Khi gọi tạo/sửa Product, FE chỉ truyền chuỗi URL và Public ID vào payload của Product.
- Mọi API public trả về cấu trúc `ApiResponse<T>`, FE bọc `AxiosResponse` lại tương ứng.

## 27. MỐI QUAN HỆ DATABASE CHÍNH
- `Product` (1) - (N) `ProductVariant`.
- `Product` (1) - (N) `ProductImage`.
- `Category` (1) - (N) `Product`.
- `Shop` (1) - (N) `Product` (Dạng Soft FK).

## 28. PHƯƠNG THỨC CHÍNH VÀ TRÁCH NHIỆM

### `createProduct`
- **Mục đích:** Xử lý toàn bộ logic tạo một sản phẩm mới.
- **Input:** `userId`, `ProductCreateRequest`.
- **Validation:** Bắt buộc nhập đầy đủ Tên, Giá gốc, Tồn kho gốc, Danh mục.
- **Main Processing:** Lấy shopId, kiểm tra quyền Shop Owner, kiểm tra Shop Ban. Tạo root `Product`, lặp qua request để tạo `ProductImage` và `ProductVariant`. Cân bằng Thumbnail.
- **Database:** `save()` trên các Repository.
- **Exceptions:** `IllegalArgumentException` nếu gian hàng không hợp lệ.

### `updateProduct`
- **Mục đích:** Cập nhật dạng đồng bộ toàn bộ (Full Sync).
- **Main Processing:** Tìm sản phẩm. Kiểm tra Owner. Đối chiếu các `id` của mảng `variants` và `images` để Cập nhật/Thêm mới/Xóa tương ứng. Thu thập các ảnh bị xóa để dọn dẹp Cloudinary qua Transaction Sync.

### `getAllProducts`
- **Mục đích:** Tìm kiếm đa tiêu chí public.
- **Main Processing:** Xử lý an toàn phân trang (clamp max 50). Nạp cây thư mục bằng Queue BFS. Sử dụng JPA Criteria API để ráp điều kiện động `Predicates`. Tự động loại trừ sản phẩm của Shop Banned.

## 29. QUY TẮC NGHIỆP VỤ QUAN TRỌNG (BUSINESS RULES)
1. Shop bị cấm không được bán hàng, mọi sản phẩm lập tức "biến mất" khỏi public API, dù Status sản phẩm vẫn là ACTIVE.
2. Sản phẩm không có biến thể thì không được tồn tại (Tuy nhiên trên source code hiện tại, biến thể được cấp tự động hoặc gộp chung với gốc).
3. URL ảnh bị thay đổi trên FE sẽ đồng thời gọi xóa vĩnh viễn file trên Cloudinary để tối ưu chi phí lưu trữ server.

## 30. XỬ LÝ LỖI (ERROR HANDLING)
- Xử lý ngoại lệ tập trung qua `@RestControllerAdvice` ở Core Module. Các lỗi `AccessDeniedException` sẽ trả về `403 Forbidden`. Lỗi nghiệp vụ tung ra `AppException` trả về `400 Bad Request`.

## 31. KIỂM THỬ (TESTS)
- Hệ thống hỗ trợ Integration Tests hoặc Unit Tests (Không được nêu rõ trong file service nhưng tuân thủ cấu trúc của Spring Test).

## 32. HẠN CHẾ HIỆN TẠI (KNOWN LIMITATIONS)
- Khi sản phẩm hết hàng hoàn toàn ở tất cả các biến thể, trạng thái gốc (Root Product) không tự động chuyển sang `OUT_OF_STOCK` (Do không có Listener tự động tổng hợp số lượng).
- Thuật toán Recommendation mới dừng ở mức Heuristic đơn giản trong SQL/Java, chưa ứng dụng Machine Learning (AI).
- Quá trình tính toán BFS cây danh mục thực hiện trên Application Memory, có thể gây chậm nếu danh mục phân cấp hàng triệu node.

## 33. BẰNG CHỨNG MÃ NGUỒN (EVIDENCE/SOURCE FILES)
- `ProductServiceImpl.java`
- `VendorProductController.java`
- `Product.java`
- `ProductVariant.java`
- `productApi.ts` (Frontend)
