# Báo cáo hoàn thành P1-04 / Feature 7 — Advanced Search & Filters

Ngày hoàn thành: **2026-09-09**  
Trạng thái: **Đã hoàn thành toàn diện BE → FE → Tests → Verification** theo Quyết định **Decision Set D7**.

---

## 1. Tóm tắt kết quả kiểm chứng (Verification Summary)

- **Backend Integration Tests:** `AdvancedSearchIntegrationTest.java` (16 test cases mới) đạt **16/16 PASS**.
- **Regression Suite:** Chạy tích hợp cùng `ProductInternalServiceImplTest` và `NPlus1AuditTest` đạt **28/28 PASS** (0 failures, 0 errors, thời gian: ~35s, chạy trên database MySQL test thật).
- **Frontend Typecheck & Build:** `npx.cmd tsc -b --noEmit` đạt exit code 0.
- **Backward Compatibility:** Giữ 100% khả năng tương thích ngược với các caller cũ của `ProductService.getAllProducts(page, size, keyword)` và `productApi.getProducts(...)`.

---

## 2. Chi tiết triển khai theo Quyết định D7

| Quyết định | Nội dung phê duyệt | Chi tiết triển khai kỹ thuật |
|---|---|---|
| **D7.1 (Giá lọc & sắp xếp)** | Lọc theo `product.price` (giá gốc hiển thị) | Dùng `root.get("price")` trong JPA Specification, hỗ trợ cả `minPrice`, `maxPrice` hoặc khoảng `[minPrice, maxPrice]`. Đồng nhất hoàn toàn với giao diện người dùng, loại bỏ nguy cơ trùng lặp bản ghi và phân trang sai của variant. |
| **D7.2 (Danh mục con)** | Thuật toán duyệt cây đệ quy lấy con cháu (Category Descendants) | Dùng thuật toán duyệt theo chiều rộng (BFS Queue) với `categoryRepository.findByParentId(currentId)`. Khi lọc theo danh mục cha (VD: "Thời trang"), tự động gom toàn bộ sản phẩm thuộc danh mục con (VD: "Áo sơ mi"). |
| **D7.3 (Thương hiệu - Brand)** | Tạm gác Brand UI/API để giữ scope tinh gọn | Tập trung tối đa vào 4 bộ lọc cốt lõi: Từ khóa (Keyword), Danh mục (Category), Khoảng giá (Price range), và Sắp xếp (Sort). Không đưa code thừa vào UI. |
| **D7.4 (Whitelist sắp xếp)** | Whitelist an toàn: `price_asc`, `price_desc`, fallback `id_desc` | Không cho phép client inject field name hoặc expression lạ vào SQL. Nếu param sắp xếp không hợp lệ, hệ thống tự động fallback an toàn về `id DESC`. |
| **D7.5 (Kiểm soát phân trang & Eligibility)** | Kẹp trần `size <= 50`, bảo đảm tính hợp lệ (Active & Non-banned) | Giới hạn phân trang tối đa 50 phần tử/trang. Luôn áp dụng lọc `status = ACTIVE` và loại trừ shop bị `BANNED` (qua `ShopInternalService`) **trước khi phân trang**, bảo đảm catalog nhất quán. |

---

## 3. Các thành phần mã nguồn đã cập nhật

### Backend
1. [`CategoryRepository.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/repository/CategoryRepository.java): Bổ sung method `findByParentId(Long parentId)`.
2. [`ProductService.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/service/ProductService.java): Mở rộng method tìm kiếm với 7 tham số; cung cấp default overload 3 tham số để bảo đảm tương thích ngược.
3. [`ProductServiceImpl.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/service/ProductServiceImpl.java): Xây dựng JPA Specification linh hoạt với BFS category hierarchy, price range predicate, sort whitelist và size clamping.
4. [`ProductController.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/controller/ProductController.java): Thêm các `@RequestParam` tùy chọn (`categoryId`, `minPrice`, `maxPrice`, `sortBy`) và xác thực ràng buộc `minPrice <= maxPrice` (trả về HTTP 400 nếu vi phạm).

### Frontend
1. [`productApi.ts`](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/api/productApi.ts): Định nghĩa `ProductSearchParams` và hàm `getProducts` sinh URLSearchParams chuẩn.
2. [`ProductListPage.tsx`](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/pages/public/ProductListPage.tsx): Sidebar bộ lọc chuyên nghiệp gồm cây danh mục thật từ API, bộ lọc khoảng giá kèm nút Áp dụng/Xóa lọc, dropdown sắp xếp, đồng bộ 2 chiều với URL SearchParams, reset trang về 0 khi đổi tiêu chí lọc, và cơ chế AbortController chống race condition (stale response).
3. [`AdminProductListPage.tsx`](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/pages/admin/AdminProductListPage.tsx): Cập nhật tương thích với object call mới.

### Tests
1. [`AdvancedSearchIntegrationTest.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/test/java/com/tmt/ecommerce/product/AdvancedSearchIntegrationTest.java): 16 test cases bao phủ:
   - `noFilters_ShouldReturnOnlyACTIVEFromNonBannedShops`
   - `keywordFilter_ShouldMatchNameCaseInsensitive`
   - `categoryFilter_ShouldIncludeDescendants_D7_2`
   - `categoryFilter_DirectChild_ShouldOnlyReturnThatChild`
   - `priceRangeFilter_ShouldFilterByProductPrice_D7_1`
   - `priceFilter_MinOnly_ShouldReturnAtLeastMin`
   - `priceFilter_MaxOnly_ShouldReturnAtMostMax`
   - `sortByPriceAsc_ShouldReturnAscendingOrder_D7_4`
   - `sortByPriceDesc_ShouldReturnDescendingOrder_D7_4`
   - `unknownSortBy_ShouldFallbackToIdDesc_D7_4`
   - `sizeClamping_ShouldNeverExceed50_D7_5`
   - `combinedFilters_KeywordAndCategoryAndPrice`
   - `bannedShop_ProductNeverAppears_EvenWithMatchingFilters`
   - `hiddenProduct_NeverAppears_InPublicSearch`
   - `httpEndpoint_MinGreaterThanMax_Returns400`
   - `httpEndpoint_NoFilters_Returns200`
