# CẨM NANG VẤN ĐÁP BẢO VỆ ĐỒ ÁN — GIẢI THÍCH TRỰC TIẾP TRÊN SOURCE CODE
## TỪ CÚ PHÁP CƠ BẢN ĐẾN LÀM CHỦ TOÀN BỘ HỆ THỐNG VÀ LUỒNG NGHIỆP VỤ

> **Mục đích:** Hướng dẫn chi tiết từng dòng code trong mã nguồn thực tế của dự án.  
> **Phương pháp:** **"Vừa lồng code vừa giải thích dòng qua dòng"** $\rightarrow$ Giúp bạn hiểu rõ cú pháp (`useState`, `useEffect`, `async/await`, `@Transactional`, Java Streams), nắm chắc luồng dữ liệu và tự tin trả lời mọi câu hỏi chất vấn của Hội đồng bảo vệ.

---


## MỤC LỤC BÀI HỌC

1. [Phần 1: Giải mã Cú pháp Frontend qua Mã Nguồn Thực Tế](#phần-1-giải-mã-cú-pháp-frontend-qua-mã-nguồn-thực-tế)
   - [1.1. Cấu hình Mạng & Token Interceptor (`apiClient.ts`)](#11-cấu-hình-mạng--token-interceptor-apiclientts)
   - [1.2. Quản lý Đăng nhập Toàn cục với Context & Hook (`AuthContext.tsx`)](#12-quản-lý-đăng-nhập-toàn-cục-với-context--hook-authcontexttsx)
   - [1.3. Trang Giỏ hàng: State, Async/Await & Gom nhóm Shop (`CartPage.tsx`)](#13-trang-giỏ-hàng-state-asyncawait--gom-nhóm-shop-cartpagetsx)
   - [1.4. Trang Đặt hàng & Rẽ nhánh Thanh toán VNPay / COD (`CheckoutPage.tsx`)](#14-trang-đặt-hàng--rẽ-nhánh-thanh-toán-vnpay--cod-checkoutpagetsx)
   - [1.5. Kết nối Chat Thời gian thực với STOMP WebSocket (`websocket.ts`)](#15-kết-nối-chat-thời-gian-thực-với-stomp-websocket-websocketts)
2. [Phần 2: Giải mã Cú pháp Backend qua Mã Nguồn Thực Tế](#phần-2-giải-mã-cú-pháp-backend-qua-mã-nguồn-thực-tế)
   - [2.1. Bộ lọc Bảo mật JWT Filter (`JwtAuthenticationFilter.java`)](#21-bộ-lọc-bảo-mật-jwt-filter-jwtauthenticationfilterjava)
   - [2.2. Thêm vào giỏ với Khóa bi quan & Aggregate Root (`CartServiceImpl.java`)](#22-thêm-vào-giỏ-với-khóa-bi-quan--aggregate-root-cartserviceimpljava)
   - [2.3. Hàm Đặt hàng: Gom đơn đa shop & Trừ kho nguyên tử (`OrderServiceImpl.java`)](#23-hàm-đặt-hàng-gom-đơn-đa-shop--trừ-kho-nguyên-tử-orderserviceimpljava)
   - [2.4. Xác nhận Giao hàng & Hủy đơn Hoàn tồn kho (`OrderServiceImpl.java`)](#24-xác-nhận-giao-hàng--hủy-đơn-hoàn-tồn-kho-orderserviceimpljava)
   - [2.5. Xử lý Webhook IPN VNPay & Chống Gian lận Giá (`PaymentGatewayService.java`)](#25-xử-lý-webhook-ipn-vnpay--chống-gian-lận-giá-paymentgatewayservicejava)
   - [2.6. Đánh giá Sản phẩm & Kiểm tra Ranh giới Module (`ReviewServiceImpl.java`)](#26-đánh-giá-sản-phẩm--kiểm-tra-ranh-giới-module-reviewserviceimpljava)
   - [2.7. Gửi Tin nhắn Realtime qua STOMP (`ChatServiceImpl.java`)](#27-gửi-tin-nhắn-realtime-qua-stomp-chatserviceimpljava)
3. [Phần 3: Toàn bộ Cấu hình Hệ thống (Configurations) — Giải mã Từng Dòng Code](#phần-3-toàn-bộ-cấu-hình-hệ-thống-configurations--giải-mã-từng-dòng-code)
   - [3.1. Cấu hình Bảo mật Spring Security 6 & CORS (`SecurityConfig.java`)](#31-cấu-hình-bảo-mật-spring-security-6--cors-securityconfigjava)
   - [3.2. Cấu hình WebSocket STOMP Message Broker (`WebSocketConfig.java`)](#32-cấu-hình-websocket-stomp-message-broker-websocketconfigjava)
   - [3.3. Cấu hình Cloudinary Lưu trữ Media Đám mây (`CloudinaryConfig.java`)](#33-cấu-hình-cloudinary-lưu-trữ-media-đám-mây-cloudinaryconfigjava)
   - [3.4. Cấu hình Cổng VNPay & Ký số HMAC-SHA512 (`VnPayStrategy.java`)](#34-cấu-hình-cổng-vnpay--ký-số-hmac-sha512-vnpaystrategyjava)
4. [Phần 4: Hệ thống Kiểm tra Hợp lệ (Validation) & Xử lý Lỗi Toàn cục (Exception Handling)](#phần-4-hệ-thống-kiểm-tra-hợp-lệ-validation--xử-lý-lỗi-toàn-cục-exception-handling)
   - [4.1. Bộ Bắt lỗi Tập trung `@RestControllerAdvice` (`GlobalExceptionHandler.java`)](#41-bộ-bắt-lỗi-tập-trung-restcontrolleradvice-globalexceptionhandlerjava)
   - [4.2. Jakarta Bean Validation trên DTOs (`@Valid`, `@NotBlank`, `@Email`, v.v.)](#42-jakarta-bean-validation-trên-dtos-valid-notblank-email-vv)
   - [4.3. Bảng Mã Lỗi Chuẩn hóa `ErrorCode.java` & Chuẩn Phản hồi `ApiResponse.java`](#43-bảng-mã-lỗi-chuẩn-hóa-errorcodejava--chuẩn-phản-hồi-apiresponsejava)
   - [4.4. Cách Frontend Bắt & Ánh xạ Lỗi Chi tiết vào Form (`apiError.ts`)](#44-cách-frontend-bắt--ánh-xạ-lỗi-chi-tiết-vào-form-apierrorts)
5. [Phần 5: Phủ Toàn bộ Chức năng & Nghiệp vụ Nâng cao (Admin, Vendor, Customer)](#phần-5-phủ-toàn-bộ-chức-năng--nghiệp-vụ-nâng-cao-admin-vendor-customer)
   - [5.1. Admin: Phê duyệt, Khóa Shop & Khôi phục Trạng thái `prior_status` (`ShopService.java`)](#51-admin-phê-duyệt-khóa-shop--khôi-phục-trạng-thái-prior_status-shopservicejava)
   - [5.2. Admin: Can thiệp Cưỡng chế Trạng thái Đơn hàng & Audit Log (`AdminOrderService.java`)](#52-admin-can-thiệp-cưỡng-chế-trạng-thái-đơn-hàng--audit-log-adminorderservicejava)
   - [5.3. Admin: Hoàn tiền VNPay với `Idempotency-Key` (`RefundService.java` & `RefundReservationService.java`)](#53-admin-hoàn-tiền-vnpay-với-idempotency-key-refundservicejava--refundreservationservicejava)
   - [5.4. Admin: Tổng hợp Báo cáo Thống kê Toàn sàn qua Internal Contracts (`AdminStatisticsController.java`)](#54-admin-tổng-hợp-báo-cáo-thống-kê-toàn-sàn-qua-internal-contracts-adminstatisticscontrollerjava)
   - [5.5. Vendor: Báo cáo Doanh thu & Doanh số Theo Ngày (`VendorAnalyticsService.java`)](#55-vendor-báo-cáo-doanh-thu--doanh-số-theo-ngày-vendoranalyticsservicejava)
   - [5.6. Vendor: Quản lý Biến thể Sản phẩm Động Lưu JSON MySQL (`ProductVariant.java`)](#56-vendor-quản-lý-biến-thể-sản-phẩm-động-lưu-json-mysql-productvariantjava)
   - [5.7. Vendor: Phản hồi Đánh giá Khách hàng & Kiểm tra Quyền Gian hàng (`VendorReviewController.java`)](#57-vendor-phản-hồi-đánh-giá-khách-hàng--kiểm-tra-quyền-gian-hàng-vendorreviewcontrollerjava)
   - [5.8. Customer: Thuật toán Gợi ý Sản phẩm Thông minh Đa tầng (`ProductServiceImpl.getRecommendedProducts`)](#58-customer-thuật-toán-gợi-ý-sản-phẩm-thông-minh-đa-tầng-productserviceimplgetrecommendedproducts)
   - [5.9. Customer: Thống kê Điểm Đánh giá & Phân bổ 1-5 Sao (`ReviewServiceImpl.getRatingSummary`)](#59-customer-thống-kê-điểm-đánh-giá--phân-bổ-1-5-sao-reviewserviceimplgetratingsummary)
6. [Phần 6: Trọn bộ 20 Câu Hỏi Vấn Đáp Thực Chiến & Mẫu Trả Lời Ghi Điểm Tuyệt Đối](#phần-6-trọn-bộ-20-câu-hỏi-vấn-đáp-thực-chiến--mẫu-trả-lời-ghi-điểm-tuyệt-đối)

---
# PHẦN 1: GIẢI MÃ CÚ PHÁP FRONTEND QUA MÃ NGUỒN THỰC TẾ

## 1.1. Cấu hình Mạng & Token Interceptor (`apiClient.ts`)

*Vị trí file:* [frontend/src/api/apiClient.ts](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/api/apiClient.ts)

### 📌 Đoạn mã nguồn thực tế:
```typescript
import axios from 'axios';
import { getApiError } from './apiError';
import { ERROR_CODES } from './types/errorCodes';

// 1. Tạo một Axios instance dùng chung cho toàn bộ ứng dụng
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. REQUEST INTERCEPTOR: Chạy trước khi bất kỳ request nào bay ra mạng
// Tự động đọc JWT Token trong LocalStorage và gắn vào Header
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. RESPONSE INTERCEPTOR: Chạy ngay khi nhận được phản hồi từ Backend
// Bắt lỗi 401 Unauthorized toàn cục để tự động đăng xuất và chuyển về trang Login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const details = getApiError(error);
    if (details.status === 401 || details.errorCode === ERROR_CODES.UNAUTHORIZED) {
      localStorage.removeItem('token');
      // Tránh lặp vô tận nếu chính trang /login bị lỗi
      if (!error.config?.url?.startsWith('/api/v1/auth/')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật (Mẫu trả lời vấn đáp):
1. **`axios.create({ baseURL, headers })`:**
   - Tạo một đối tượng gọi API thống nhất. Thay vì phải gõ lại URL `http://localhost:8080` ở hàng chục file khác nhau, ta chỉ cần định nghĩa tập trung ở một nơi duy nhất.
2. **`apiClient.interceptors.request.use(...)` (Request Interceptor là gì?):**
   - Hoạt động giống như một **"trạm kiểm soát vé tự động"**. Mỗi khi một component (như xem giỏ hàng, đặt hàng, gửi chat) gọi API, interceptor này tự động kiểm tra xem `localStorage` có token không. Nếu có, nó tự gắn `Authorization: Bearer <token>` vào Header. Nhờ đó, lập trình viên không cần viết thủ công việc truyền token ở từng hàm.
3. **`apiClient.interceptors.response.use(...)` (Global Error Handling):**
   - Xử lý lỗi tập trung. Khi token hết hạn (sau 24h) hoặc không hợp lệ, Backend trả về HTTP Status `401 Unauthorized`. Interceptor lập tức bắt lấy lỗi này, xóa token cũ khỏi trình duyệt và tự động điều hướng người dùng về trang `/login`.

> **Hội đồng sẽ hỏi:** *"Tại sao em không dùng hàm `fetch()` mặc định của trình duyệt mà lại dùng thư viện `axios`?"*  
> **Trả lời:** *"Dạ thưa thầy/cô:  
> 1. `axios` có cơ chế **Interceptors** cực kỳ mạnh mẽ giúp tự động gắn JWT Token và tự động xử lý lỗi 401 tập trung tại 1 file duy nhất.  
> 2. `axios` tự động parse dữ liệu JSON (không cần phải gọi `res.json()` thủ công như `fetch`).  
> 3. Cú pháp `axios` ngắn gọn, hỗ trợ thiết lập timeout và tương thích tốt trên mọi trình duyệt."*

---

## 1.2. Quản lý Đăng nhập Toàn cục với Context & Hook (`AuthContext.tsx`)

*Vị trí file:* [frontend/src/contexts/AuthContext.tsx](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/contexts/AuthContext.tsx)

### 📌 Đoạn mã nguồn thực tế:
```tsx
import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { userApi } from '../api/userApi';
import type { Profile, ProfileUpdate } from '../api/userApi';
import { decodeUser } from './authSession';
import type { UserPayload } from './authSession';
import { websocketService } from '../services/websocket';

export type { UserPayload } from './authSession';

// 1. Interface định nghĩa dữ liệu Context cung cấp cho toàn bộ website
interface AuthContextType {
  user: UserPayload | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isVendor: boolean;
  login: (token: string) => void;
  logout: () => void;
  loadProfile: () => Promise<Profile | null>;
  saveProfile: (body: ProfileUpdate) => Promise<Profile | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // 2. Khởi tạo State người dùng bằng cách đọc Token từ LocalStorage khi vừa mở web
  const [user, setUser] = useState<UserPayload | null>(() => {
    const token = localStorage.getItem('token');
    return decodeUser(token);
  });
  const [loading] = useState<boolean>(false);

  // 3. Hàm tải thông tin hồ sơ người dùng từ Backend
  const loadProfile = useCallback(async (): Promise<Profile | null> => {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
      const response = await userApi.getProfile();
      const profile = response.data.data;
      if (profile) {
        setUser((prev) => (prev ? { ...prev, fullName: profile.fullName } : prev));
        return profile;
      }
      return null;
    } catch (error) {
      console.error('Lỗi khi tải thông tin cá nhân:', error);
      return null;
    }
  }, []);

  // 4. Hàm cập nhật thông tin cá nhân (họ tên, số điện thoại)
  const saveProfile = useCallback(async (body: ProfileUpdate): Promise<Profile | null> => {
    try {
      const response = await userApi.updateProfile(body);
      const profile = response.data.data;
      if (profile) {
        setUser((prev) => (prev ? { ...prev, fullName: profile.fullName } : prev));
        return profile;
      }
      return null;
    } catch (error) {
      console.error('Lỗi khi cập nhật hồ sơ:', error);
      throw error;
    }
  }, []);

  // 5. Khi vừa mở web: nếu đã có token thì kết nối WebSocket và tải hồ sơ
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      websocketService.connect(token);
      void loadProfile();
    }
  }, [loadProfile]);

  // 6. Đồng bộ đa tab trình duyệt (Cross-tab Sync qua StorageEvent)
  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'token') {
        const token = localStorage.getItem('token');
        const identity = decodeUser(token);
        setUser(identity);
        if (token) {
          websocketService.connect(token);
          void loadProfile();
        } else {
          websocketService.disconnect();
        }
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [loadProfile]);

  // 7. Hàm Đăng nhập: Lưu token, cập nhật state, kết nối WebSocket, nạp profile
  const login = (token: string) => {
    localStorage.setItem('token', token);
    const identity = decodeUser(token);
    setUser(identity);
    websocketService.connect(token);
    void loadProfile();
  };

  // 8. Hàm Đăng xuất: Xóa token, reset user về null, ngắt kết nối WebSocket
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    websocketService.disconnect();
  };

  // 9. Các cờ kiểm tra phân quyền tiện lợi
  const isAuthenticated = user !== null;
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const isVendor = user?.roles?.some((r) => r === 'ROLE_VENDOR' || r === 'ROLE_ADMIN') ?? false;

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isAdmin,
        isVendor,
        login,
        logout,
        loadProfile,
        saveProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// 10. Custom Hook để các Component gọi nhanh không cần import useContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật (Mẫu trả lời vấn đáp):
1. **Tại sao lại dùng React Context (`createContext`)?**
   - Giải quyết bài toán **Prop Drilling** (truyền tham số qua quá nhiều cấp component trung gian). Bất kỳ trang nào (như Header, trang Đơn hàng, trang Admin) chỉ cần gọi `const { isAuthenticated, user, logout } = useAuth();` là có thể lấy thông tin đăng nhập ngay lập tức.
2. **`useState(() => decodeUser(...))` (Lazy Initialization):**
   - Thay vì truyền thẳng `useState(decodeUser(...))`, ta truyền hàm callback `() => ...`. Cú pháp này giúp hàm giải mã JWT chỉ chạy **đúng 1 lần duy nhất** khi ứng dụng khởi động, tránh tốn CPU giải mã lại mỗi lần component re-render.
3. **`window.addEventListener('storage', handleStorageChange)` (Đồng bộ đa tab):**
   - Khi người dùng mở 2 tab trình duyệt cùng lúc. Nếu họ bấm "Đăng xuất" ở Tab 1, trình duyệt tự động kích hoạt sự kiện `StorageEvent`. Tab 2 lập tức phát hiện token bị xóa và tự động chuyển về trạng thái Chưa đăng nhập mà không cần người dùng phải bấm F5 tải lại trang.
4. **`login()` và `logout()` làm những việc gì?**
   - `login(token)`: Lưu token vào `localStorage`, giải mã lấy thông tin user, gọi `websocketService.connect(token)` để sẵn sàng nhận tin nhắn realtime và gọi `loadProfile()` lấy họ tên.
   - `logout()`: Xóa token khỏi `localStorage`, đặt `user = null`, ngắt kết nối WebSocket (`websocketService.disconnect()`).

---

## 1.3. Trang Giỏ hàng: State, Async/Await & Gom nhóm Shop (`CartPage.tsx`)

*Vị trí file:* [frontend/src/pages/public/CartPage.tsx](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/pages/public/CartPage.tsx)

### 📌 Đoạn mã nguồn thực tế:
```tsx
export const CartPage: React.FC = () => {
  // Lấy dữ liệu giỏ hàng từ CartContext
  const { cart, loading, fetchCart } = useCart();
  
  // State lưu danh sách ID các sản phẩm khách đang tick chọn mua
  const [selectedItemIds, setSelectedItemIds] = useState<number[]>([]);
  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null);
  const navigate = useNavigate();

  // 1. Gom nhóm sản phẩm theo từng Shop bằng hàm reduce
  const groupedItems = cart?.items.reduce((acc, item) => {
    if (!acc[item.shopId]) {
      acc[item.shopId] = [];
    }
    acc[item.shopId].push(item);
    return acc;
  }, {} as Record<number, CartItemResponse[]>) || {};

  // 2. Hàm cập nhật số lượng (+ / -) bất đồng bộ với Backend
  const handleUpdateQuantity = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return; // Không cho giảm dưới 1
    setUpdatingItemId(itemId);   // Đánh dấu item đang xoay spinner loading
    try {
      // Gửi request PUT lên server để cập nhật số lượng mới
      await cartApi.updateQuantity(itemId, { quantity: newQuantity });
      // Gọi lại fetchCart để đồng bộ lại toàn bộ tiền hàng mới nhất
      await fetchCart();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật số lượng');
    } finally {
      setUpdatingItemId(null);   // Tắt spinner dù thành công hay thất bại
    }
  };

  // 3. Xóa nhiều sản phẩm đã chọn cùng lúc (Bulk Remove)
  const handleBulkRemove = async () => {
    if (selectedItemIds.length === 0) return;
    if (!window.confirm(`Bạn có chắc muốn xóa ${selectedItemIds.length} sản phẩm?`)) return;

    try {
      // Promise.all: Bắn đồng thời N request xóa và chờ tất cả hoàn tất
      await Promise.all(selectedItemIds.map(id => cartApi.removeCartItem(id)));
      setSelectedItemIds([]); // Xóa sạch mảng đã chọn
      await fetchCart();      // Tải lại giỏ hàng
    } catch (error) {
      alert('Có lỗi xảy ra khi xóa sản phẩm');
    }
  };

  // 4. Bấm nút "Mua Hàng" -> Chuyển sang trang Checkout kèm danh sách ID đã chọn
  const handleProceedToCheckout = () => {
    navigate('/checkout', { state: { selectedItemIds } });
  };
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`reduce((acc, item) => ..., {})` (Gom nhóm dữ liệu):**
   - Khách mua 5 món từ 2 shop khác nhau (Shop A có 2 món, Shop B có 3 món). Hàm `reduce` duyệt qua mảng `cart.items`, lấy `shopId` làm chìa khóa (key) để nhóm các món hàng lại. Nhờ đó, giao diện hiển thị thành từng khối Shop riêng biệt giống hệt Shopee/Lazada.
2. **`async / await` trong `handleUpdateQuantity`:**
   - Cú pháp `await cartApi.updateQuantity(...)` bảo trình duyệt: *"Hãy đợi Backend kiểm tra tồn kho và lưu vào DB xong, sau đó `await fetchCart()` để lấy tổng tiền mới về"*.
3. **`Promise.all(...)`:**
   - Khi xóa 5 món hàng, thay vì dùng vòng lặp tuần tự chạy mất 5 giây, `Promise.all` bắn 5 request song song cùng một lúc và chỉ mất khoảng 0.2 giây để hoàn thành toàn bộ.
4. **`navigate('/checkout', { state: { selectedItemIds } })`:**
   - Chuyển trang của thư viện `react-router-dom` kèm truyền tham số ẩn (`state`). Trang Checkout nhận được đúng danh sách các món khách đã tick chọn mà không cần lưu rườm rà vào URL query string.

---

## 1.4. Trang Đặt hàng & Rẽ nhánh Thanh toán VNPay / COD (`CheckoutPage.tsx`)

*Vị trí file:* [frontend/src/pages/public/CheckoutPage.tsx](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/pages/public/CheckoutPage.tsx#L84-L125)

### 📌 Đoạn mã nguồn thực tế:
```tsx
const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault(); // 1. Chặn hành vi reload lại trang mặc định của thẻ <form>
    setError(null);

    // 2. Validate địa chỉ
    if (!shippingAddress.trim()) {
        setError("Vui lòng nhập địa chỉ giao hàng");
        return;
    }

    setSubmitting(true);
    try {
        // 3. Gửi thông tin đặt hàng lên Backend
        const result = await orderApi.checkout({
            shippingAddress,
            paymentMethod, // 'VNPAY' hoặc 'COD'
            cartItemIds: selectedItemIds,
            voucherCode: voucherCode.trim() || undefined
        });

        await fetchCart(); // Cập nhật lại số lượng giỏ hàng trên Header

        // 4. RẼ NHÁNH THANH TOÁN
        if (paymentMethod === 'VNPAY' && result.data?.paymentGroupId) {
            const paymentGroupId = result.data.paymentGroupId;
            // Gọi API tạo link thanh toán VNPay
            const paymentRes = await paymentApi.createPaymentUrl({ paymentGroupId });
            if (paymentRes.data?.paymentUrl) {
                // Chuyển hướng toàn bộ trình duyệt sang Cổng thanh toán VNPay
                window.location.href = paymentRes.data.paymentUrl;
                return;
            }
        }

        // Nếu là COD (Thanh toán khi nhận hàng) -> Điều hướng thẳng đến trang Lịch sử đơn hàng
        navigate('/orders', { replace: true });
    } catch (err: any) {
        setError(err.response?.data?.message || 'Có lỗi xảy ra khi đặt hàng');
        setSubmitting(false);
    }
};
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`e.preventDefault()`:**
   - Trong HTML, thẻ `<form onSubmit={...}>` có hành vi mặc định là gửi request HTTP POST và tải lại toàn bộ trang web (Full page reload). `e.preventDefault()` ngăn chặn việc này để biến nó thành tương tác Single Page Application mượt mà.
2. **Sự khác biệt giữa `window.location.href` và `navigate(...)`:**
   - `navigate('/orders')`: Điều hướng nội bộ trong ứng dụng React SPA (không tải lại trang).
   - `window.location.href = paymentUrl`: Rời khỏi website của mình để **chuyển hướng sang domain bên thứ ba** (Cổng thanh toán điện tử `https://sandbox.vnpayment.vn/...`).
3. **`navigate(..., { replace: true })`:**
   - `replace: true` thay thế lịch sử trình duyệt (History stack). Khi khách bấm nút Back trên trình duyệt, họ sẽ không bị quay trở lại trang Checkout để tránh việc vô tình đặt lại đơn hàng lần thứ hai (Duplicate order).

---

## 1.5. Kết nối Chat Thời gian thực với STOMP WebSocket (`websocket.ts`)

*Vị trí file:* [frontend/src/services/websocket.ts](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/services/websocket.ts)

### 📌 Đoạn mã nguồn thực tế:
```typescript
import { Client } from '@stomp/stompjs';
import type { ChatMessage, SendMessageRequest } from '../api/types/chat.types';
import type { NotificationResponse } from '../api/types/notification.types';

class WebSocketService {
  private client: Client | null = null;
  // Quản lý các hàm lắng nghe (Listeners) bằng Set để tránh trùng lặp
  private messages = new Set<(m: ChatMessage) => void>();
  private notifications = new Set<(n: NotificationResponse) => void>();
  private statuses = new Set<(connected: boolean) => void>();
  private connected = false;

  // 1. Kết nối tới WebSocket Server với Bearer Token
  connect(token: string) {
    if (this.client) {
      this.disconnect();
    }

    const api = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const url = import.meta.env.VITE_WS_URL || `${api.replace(/^http/, 'ws')}/ws`;

    const client = new Client({
      brokerURL: url,
      connectHeaders: {
        Authorization: `Bearer ${token}`, // Truyền token vào frame CONNECT của STOMP
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.status(true);

        // Đăng ký nhận tin nhắn chat riêng tư: /user/queue/messages
        client.subscribe('/user/queue/messages', (frame) => {
          try {
            const data = JSON.parse(frame.body);
            this.messages.forEach((cb) => cb(data)); // Bắn tin nhắn về React State
          } catch (e) {
            console.error('Error parsing chat message:', e);
          }
        });

        // Đăng ký nhận thông báo hệ thống: /user/queue/notifications
        client.subscribe('/user/queue/notifications', (frame) => {
          try {
            const data = JSON.parse(frame.body);
            this.notifications.forEach((cb) => cb(data)); // Bắn thông báo về React State
          } catch (e) {
            console.error('Error parsing notification:', e);
          }
        });
      },
      onStompError: () => {
        this.status(false);
      },
      onWebSocketClose: () => {
        this.status(false);
      },
    });

    this.client = client;
    client.activate(); // Kích hoạt bắt tay kết nối TCP
  }

  // 2. Ngắt kết nối khi đăng xuất
  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.status(false);
  }

  // 3. Đăng ký nhận tin nhắn chat (Observer Pattern)
  subscribe(cb: (m: ChatMessage) => void) {
    this.messages.add(cb);
    return () => {
      this.messages.delete(cb); // Hủy đăng ký khi Component unmount
    };
  }

  // 4. Đăng ký nhận thông báo hệ thống
  subscribeNotifications(cb: (n: NotificationResponse) => void) {
    this.notifications.add(cb);
    return () => {
      this.notifications.delete(cb);
    };
  }

  // 5. Theo dõi trạng thái Online/Offline của kết nối Socket
  onStatusChange(cb: (connected: boolean) => void) {
    this.statuses.add(cb);
    cb(this.connected);
    return () => {
      this.statuses.delete(cb);
    };
  }

  private status(value: boolean) {
    this.connected = value;
    this.statuses.forEach((cb) => cb(value));
  }

  isConnected() {
    return this.connected;
  }

  // 6. Gửi tin nhắn chat lên server qua kênh đích /app/chat.send
  sendMessage(body: SendMessageRequest): boolean {
    if (!this.client?.connected || !this.connected) return false;
    this.client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(body),
    });
    return true;
  }
}

export const websocketService = new WebSocketService();
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật (Mẫu trả lời vấn đáp):
1. **WebSocket hoạt động như thế nào trong dự án?**
   - Khác với HTTP (Client gửi request thì Server mới trả về response), WebSocket tạo ra một **đường truyền hai chiều liên tục (Full-duplex TCP Connection)**. Khi khách A nhắn tin cho Shop B, tin nhắn gửi lên Backend và Backend có thể chủ động "bắn" ngay lập tức sang màn hình của Shop B mà Shop B không cần phải bấm F5 hay gửi request hỏi thăm liên tục (Long polling).
2. **Giao thức STOMP mang lại lợi ích gì?**
   - STOMP là giao thức hướng tin nhắn chạy trên nền WebSocket, cung cấp sẵn các lệnh chuẩn: `CONNECT`, `SUBSCRIBE`, `SEND`. Phía Frontend chỉ cần gọi `client.subscribe('/user/queue/messages')` là có thể nhận diện đúng các tin nhắn gửi đích danh cho mình.
3. **Mẫu thiết kế Observer Pattern trong `subscribe()`:**
   - Dùng `Set<(data) => void>` để lưu danh sách các hàm lắng nghe. Khi có tin nhắn đến từ WebSocket, `this.messages.forEach(cb => cb(data))` kích hoạt đồng thời tất cả các Component (như Widget Chat nổi, Trang Chat riêng) cập nhật giao diện cùng lúc.

---

# PHẦN 2: GIẢI MÃ CÚ PHÁP BACKEND QUA MÃ NGUỒN THỰC TẾ

## 2.1. Bộ lọc Bảo mật JWT Filter (`JwtAuthenticationFilter.java`)

*Vị trí file:* `src/main/java/com/tmt/ecommerce/common/security/JwtAuthenticationFilter.java`

### 📌 Đoạn mã nguồn thực tế:
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Trích xuất Header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Nếu không có header hoặc không bắt đầu bằng "Bearer ", bỏ qua cho các filter tiếp theo
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Cắt bỏ 7 ký tự đầu ("Bearer ") để lấy chuỗi token nguyên bản
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 3. Nếu có email và chưa được xác thực trong SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Kiểm tra token có đúng chữ ký và còn hạn không
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // Danh sách quyền: ROLE_USER, ROLE_VENDOR...
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // THIẾT LẬP SECURITY CONTEXT: Kể từ dòng này, Spring Security biết user này là ai
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response); // Cho request đi tiếp vào Controller
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`OncePerRequestFilter`:**
   - Lớp cơ sở của Spring Security đảm bảo bộ lọc này chỉ thực thi **đúng một lần duy nhất cho mỗi HTTP Request**, tránh lãng phí tài nguyên CPU.
2. **`SecurityContextHolder.getContext().setAuthentication(authToken)`:**
   - Đây là "trái tim" của Spring Security. Khi đối tượng `authToken` được nhét vào `SecurityContext`, ở mọi tầng Controller phía sau, ta có thể dùng `@PreAuthorize("hasRole('ADMIN')")` hoặc lấy thông tin người dùng hiện tại qua `@CurrentUserId` một cách tự động.

---

## 2.2. Thêm vào giỏ với Khóa bi quan & Aggregate Root (`CartServiceImpl.java`)

*Vị trí file:* [CartServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/cart/service/CartServiceImpl.java#L29-L72)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional // Đảm bảo tính toàn vẹn giao dịch
public void addToCart(Long userId, CartItemRequest request) {

    // 1. Gọi ProductInternalService kiểm tra biến thể qua ranh giới module
    ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(request.getProductVariantId());

    if (!"ACTIVE".equals(variantInfo.status())) {
        throw new IllegalStateException("Sản phẩm này hiện không kinh doanh.");
    }

    if (variantInfo.stockQuantity() < request.getQuantity()) {
        throw new IllegalStateException("Số lượng tồn kho không đủ. Chỉ còn " + variantInfo.stockQuantity());
    }

    // 2. KHÓA BI QUAN (Pessimistic Lock): Khóa bản ghi giỏ hàng của user này lại
    Cart cart = cartRepository.findByUserIdForUpdate(userId)
            .orElseGet(() -> {
                Cart newCart = Cart.builder().userId(userId).build();
                return cartRepository.save(newCart);
            });

    // 3. Kiểm tra Shop bán sản phẩm này có bị cấm không
    shopInternalService.requireNotBannedForSale(variantInfo.shopId());

    // 4. AGGREGATE ROOT PATTERN: Quản lý CartItem thông qua Entity cha Cart
    Optional<CartItem> existingItemOpt = cart.getItems().stream()
            .filter(item -> item.getProductVariantId().equals(request.getProductVariantId()))
            .findFirst();

    if (existingItemOpt.isPresent()) {
        // Nếu đã có -> Tăng số lượng
        CartItem existingItem = existingItemOpt.get();
        int newQuantity = existingItem.getQuantity() + request.getQuantity();

        if (newQuantity > variantInfo.stockQuantity()) {
            throw new IllegalStateException("Tổng số lượng trong giỏ vượt quá tồn kho hiện tại.");
        }
        existingItem.setQuantity(newQuantity);
    } else {
        // Nếu chưa có -> Thêm mới CartItem vào danh sách items của Cart
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .productVariantId(request.getProductVariantId())
                .quantity(request.getQuantity())
                .build();
        cart.getItems().add(newItem);
    }

    cartRepository.save(cart); // Hibernate tự động Cascade lưu CartItem xuống bảng cart_items
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`findByUserIdForUpdate(userId)` (Khóa bi quan FOR UPDATE):**
   - Sinh câu lệnh SQL: `SELECT * FROM carts WHERE user_id = ? FOR UPDATE;`.
   - *Ý nghĩa:* Ngăn chặn hoàn toàn lỗi Race Condition khi khách hàng bấm nút "Thêm vào giỏ hàng" liên tục 5 lần trong 1 giây. Database sẽ khóa dòng giỏ hàng này lại, các request sau phải xếp hàng chờ request trước thực hiện xong, đảm bảo số lượng giỏ hàng được tính toán chính xác tuyệt đối.
2. **Aggregate Root trong DDD (Domain-Driven Design):**
   - Tại sao trong code không có `cartItemRepository.save(item)`? Vì `Cart` là thực thể gốc (Aggregate Root), còn `CartItem` là thực thể con phụ thuộc. Khi ta cấu hình `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`, chỉ cần lưu `cart`, Hibernate sẽ tự lo việc thêm/sửa/xóa các dòng trong bảng `cart_items`.

---

## 2.3. Hàm Đặt hàng: Gom đơn đa shop & Trừ kho nguyên tử (`OrderServiceImpl.java`)

*Vị trí file:* [OrderServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/order/service/OrderServiceImpl.java#L73-L187)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional
public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
    // 1. Sinh UUID duy nhất đại diện cho nhóm thanh toán (N đơn hàng dùng chung 1 UUID)
    String paymentGroupId = java.util.UUID.randomUUID().toString();

    // 2. Lấy dữ liệu giỏ hàng qua CartInternalService
    List<CartItemInternalDto> allCartItems = cartInternalService.getCartItems(userId);

    // 3. Lọc lấy các món khách tick chọn mua
    List<CartItemInternalDto> cartItems = allCartItems.stream()
            .filter(item -> request.cartItemIds().contains(item.cartItemId()))
            .toList();

    // 4. Gom nhóm sản phẩm theo từng Shop bằng Java Stream Grouping
    Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
            .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

    // 5. Tính toán Voucher (nếu có)
    VoucherDiscountResult voucherDiscount = null;
    if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
        voucherDiscount = voucherInternalService.calculateDiscount(...);
        boolean reserved = voucherInternalService.incrementUsage(voucherDiscount.voucherId());
        if (!reserved) throw new IllegalStateException("Mã giảm giá đã hết lượt sử dụng!");
    }

    List<Order> savedOrders = new ArrayList<>();

    // 6. DUYỆT TỪNG SHOP -> MỖI SHOP TẠO 1 ĐƠN HÀNG RIÊNG BIỆT
    for (Map.Entry<Long, List<CartItemInternalDto>> entry : itemsByShop.entrySet()) {
        Long shopId = entry.getKey();
        List<CartItemInternalDto> shopItems = entry.getValue();

        Order order = Order.builder()
                .userId(userId)
                .shopId(shopId)
                .paymentGroupId(paymentGroupId) // Dùng chung UUID paymentGroupId
                .status(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .paymentMethod(request.paymentMethod())
                .build();

        BigDecimal shopOrderTotal = BigDecimal.ZERO;

        for (CartItemInternalDto cartItem : shopItems) {
            BigDecimal subTotal = cartItem.unitPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
            shopOrderTotal = shopOrderTotal.add(subTotal);

            // TRỪ KHO NGUYÊN TỬ: Chạy câu UPDATE stock_quantity = stock_quantity - ?
            productInternalService.deductStock(cartItem.productVariantId(), cartItem.quantity());

            // SNAPSHOT PATTERN: Đóng băng giá, tên, ảnh vào bảng order_items
            OrderItem orderItem = OrderItem.builder()
                    .productVariantId(cartItem.productVariantId())
                    .productName(cartItem.productName())
                    .imageUrl(itemImageUrl)
                    .unitPrice(cartItem.unitPrice())
                    .quantity(cartItem.quantity())
                    .subTotal(subTotal)
                    .build();

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(shopOrderTotal.subtract(shopDiscount));
        savedOrders.add(orderRepository.save(order)); // Lưu đơn hàng của shop
    }

    // 7. Xóa các món đã mua thành công ra khỏi giỏ hàng
    List<Long> checkedOutItemIds = cartItems.stream().map(CartItemInternalDto::cartItemId).toList();
    cartInternalService.removeCartItems(userId, checkedOutItemIds);

    return new CheckoutResponse(paymentGroupId, mapToOrderResponses(savedOrders));
}
```

---

## 2.4. Xác nhận Giao hàng & Hủy đơn Hoàn tồn kho (`OrderServiceImpl.java`)

### 📌 Đoạn mã nguồn thực tế:
```java
// === CHỨC NĂNG 1: KHÁCH HÀNG BẤM "ĐÃ NHẬN HÀNG" ===
@Override
@Transactional
public OrderResponse confirmDelivery(Long userId, Long orderId) {
    // 1. Khóa đơn hàng chống thao tác đồng thời
    Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    // 2. Bảo mật: Đảm bảo người bấm đúng là chủ nhân đơn hàng
    if (!order.getUserId().equals(userId)) {
        throw new AppException(ErrorCode.ORDER_NOT_FOUND);
    }
    // 3. Kiểm tra máy trạng thái: Chỉ đơn đang SHIPPED mới được bấm nhận hàng
    if (order.getStatus() != OrderStatus.SHIPPED) {
        throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION);
    }

    // 4. Chuyển trạng thái sang DELIVERED, ghi nhận mốc thời gian và nguồn xác nhận là BUYER
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveredAt(LocalDateTime.now());
    order.setDeliveryConfirmedByUserId(userId);
    order.setDeliveryConfirmationSource(DeliveryConfirmationSource.BUYER);

    Order updatedOrder = orderRepository.save(order);
    return mapToOrderResponses(List.of(updatedOrder)).get(0);
}

// === CHỨC NĂNG 2: HỦY ĐƠN HÀNG & HOÀN LẠI TỒN KHO ===
@Override
@Transactional
public OrderResponse cancelOrder(Long userId, Long orderId) {
    Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getUserId().equals(userId)) throw new AccessDeniedException("Không có quyền");
    if (order.getStatus() != OrderStatus.PENDING) {
        throw new IllegalStateException("Chỉ đơn hàng đang chờ xử lý mới có thể hủy");
    }

    order.setStatus(OrderStatus.CANCELLED);

    // HOÀN TRẢ LẠI TỒN KHO KHI HỦY ĐƠN
    for (OrderItem item : order.getItems()) {
        productInternalService.restoreStock(item.getProductVariantId(), item.getQuantity());
    }

    return mapToOrderResponses(List.of(orderRepository.save(order))).get(0);
}
```

---

## 2.5. Xử lý Webhook IPN VNPay & Chống Gian lận Giá (`PaymentGatewayService.java`)

*Vị trí file:* [PaymentGatewayService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/payment/service/PaymentGatewayService.java#L53-L102)

### 📌 Đoạn mã nguồn thực tế:
```java
@Transactional
public void processVnPayIpn(Map<String, String> params) {
    PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.VNPAY);

    // 1. XÁC THỰC CHỮ KÝ HMAC-SHA512: Phát hiện ngay nếu hacker sửa tham số trên đường truyền
    if (!strategy.verifyIpnSignature(params)) {
        throw new IllegalArgumentException("Chữ ký VNPAY không hợp lệ! Phát hiện nghi vấn giả mạo.");
    }

    String txnRef = params.get("vnp_TxnRef");
    String responseCode = params.get("vnp_ResponseCode");

    // 2. KHÓA BI QUAN ĐƠN THANH TOÁN (SELECT ... FOR UPDATE)
    Payment payment = paymentRepository.findByTransactionIdForUpdate(txnRef)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + txnRef));

    // 3. IDEMPOTENCY CHECK: Nếu đơn đã được xử lý trước đó rồi thì bỏ qua ngay (tránh xác nhận 2 lần)
    if (payment.getStatus() != PaymentStatus.PENDING) {
        return;
    }

    // 4. CHỐNG GIAN LẬN GIÁ (Amount Verification): So khớp số tiền VNPay báo về với số tiền thực của đơn
    long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
    long expectedAmount = payment.getAmount().movePointRight(2).longValueExact(); // Nhân 100 vì VNPay bỏ dấu thập phân
    if (vnpAmount != expectedAmount) {
        throw new IllegalArgumentException("Số tiền thanh toán không khớp với hóa đơn.");
    }

    // 5. CẬP NHẬT TRẠNG THÁI VÀ GỌI ORDER MODULE
    if ("00".equals(responseCode)) {
        payment.setStatus(PaymentStatus.SUCCESS);
        // Gọi OrderInternalService chuyển trạng thái toàn bộ các đơn hàng trong nhóm sang CONFIRMED
        orderInternalService.confirmPendingOrdersByGroup(payment.getPaymentGroupId());
    } else {
        payment.setStatus(PaymentStatus.FAILED);
    }

    payment.setGatewayTransactionId(params.get("vnp_TransactionNo"));
    paymentRepository.save(payment);
}
```

---

## 2.6. Đánh giá Sản phẩm & Kiểm tra Ranh giới Module (`ReviewServiceImpl.java`)

*Vị trí file:* [ReviewServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/review/service/ReviewServiceImpl.java#L40-L77)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional
public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {

    // 1. Gọi OrderInternalService: Kiểm tra đơn hàng đã DELIVERED và thuộc về khách hàng chưa
    if (!orderInternalService.isOrderDeliveredAndBelongsToUser(request.orderId(), userId)) {
        throw new IllegalArgumentException("Đơn hàng chưa giao thành công hoặc không thuộc về bạn.");
    }

    // 2. Gọi ProductInternalService: Kiểm tra sản phẩm đánh giá có đúng nằm trong đơn hàng đó không
    List<Long> variantIdsInOrder = orderInternalService.getProductVariantIdsByOrderId(request.orderId());
    boolean hasProduct = variantIdsInOrder.stream()
            .map(productInternalService::getProductIdByVariantId)
            .anyMatch(pid -> pid != null && pid.equals(request.productId()));

    if (!hasProduct) {
        throw new IllegalArgumentException("Sản phẩm không có trong đơn hàng này.");
    }

    // 3. Chống spam: 1 đơn hàng chỉ được đánh giá 1 sản phẩm 1 lần duy nhất
    if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, request.orderId(), request.productId())) {
        throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.");
    }

    // 4. Lưu bài đánh giá vào Database
    Review review = Review.builder()
            .userId(userId)
            .productId(request.productId())
            .orderId(request.orderId())
            .rating(request.rating()) // 1 đến 5 sao
            .comment(request.comment())
            .imageUrls(request.imageUrls())
            .build();

    review = reviewRepository.save(review);

    // 5. Bắn Domain Event để gửi thông báo cho chủ Shop biết có review mới
    Long shopId = productInternalService.getShopIdByProductId(request.productId());
    eventPublisher.publishEvent(new ReviewCreatedEvent(
            review.getId(), userId, shopId, request.productId(), request.rating()
    ));

    return mapToReviewResponse(review);
}
```

---

## 2.7. Module Chat: Giao tiếp Hợp đồng Nội bộ, Chặn Tự Chat & Gửi Tin nhắn Realtime STOMP (`ChatServiceImpl.java`)

*Vị trí file:* [ChatServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/chat/service/ChatServiceImpl.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ShopInternalService shopInternalService;
    private final IdentityInternalService identityInternalService; // Hợp đồng nội bộ lấy tên user
    private final SimpMessagingTemplate messagingTemplate;

    // 1. Tạo hoặc mở phòng chat: Chặn shop tự chat với chính mình
    @Override
    @Transactional
    public ChatRoomResponse createOrGetRoom(CreateChatRoomRequest request, Long buyerId) {
        Long shopId = request.getShopId();

        Long shopOwnerId = shopInternalService.getUserIdByShopId(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        // CHẶN TỰ CHAT: Người mua không thể trùng với chủ sở hữu của shop
        if (buyerId.equals(shopOwnerId)) {
            throw new IllegalArgumentException("Bạn không thể tự trò chuyện với cửa hàng của chính mình.");
        }

        ChatRoom room = chatRoomRepository.findByBuyerIdAndShopId(buyerId, shopId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .buyerId(buyerId)
                            .shopId(shopId)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return mapToChatRoomResponse(room, buyerId);
    }

    // 2. Lấy danh sách phòng chat của tôi (Khử trùng lặp & Lọc bỏ phòng tự chat)
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        List<ChatRoomResponse> responses = new ArrayList<>();
        Set<Long> seenRoomIds = new HashSet<>(); // Set chống trùng lặp phòng

        // Lấy các phòng mình đóng vai trò Người mua
        chatRoomRepository.findAllByBuyerIdOrderByUpdatedAtDesc(userId)
            .forEach(room -> {
                Long shopOwnerId = shopInternalService.getUserIdByShopId(room.getShopId()).orElse(null);
                if (shopOwnerId != null && shopOwnerId.equals(userId)) {
                    return; // Bỏ qua nếu là phòng tự chat
                }
                if (seenRoomIds.add(room.getId())) {
                    responses.add(mapToChatRoomResponse(room, userId));
                }
            });

        // Lấy các phòng mình đóng vai trò Chủ shop
        shopInternalService.findShopIdByUserId(userId).ifPresent(shopId -> {
            chatRoomRepository.findAllByShopIdOrderByUpdatedAtDesc(shopId)
                .forEach(room -> {
                    if (room.getBuyerId().equals(userId)) {
                        return; // Bỏ qua nếu là phòng tự chat
                    }
                    if (seenRoomIds.add(room.getId())) {
                        responses.add(mapToChatRoomResponse(room, userId));
                    }
                });
        });

        // Sắp xếp phòng có tin nhắn mới nhất lên đầu danh sách
        responses.sort((r1, r2) -> {
            if (r1.getUpdatedAt() == null && r2.getUpdatedAt() == null) return 0;
            if (r1.getUpdatedAt() == null) return 1;
            if (r2.getUpdatedAt() == null) return -1;
            return r2.getUpdatedAt().compareTo(r1.getUpdatedAt());
        });

        return responses;
    }

    // 3. Gửi tin nhắn qua STOMP WebSocket
    @Override
    @Transactional
    public void sendMessage(ChatMessageRequest request, Long senderId) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ChatRoomNotFoundException("Phòng chat không tồn tại"));

        validateAccess(room, senderId);

        ChatMessage message = ChatMessage.builder()
                .roomId(room.getId())
                .senderId(senderId)
                .content(request.getContent())
                .build();
        message = chatMessageRepository.save(message);

        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        Long recipientId = getRecipientId(room, senderId);
        ChatMessageResponse response = mapToChatMessageResponse(message);

        // Bắn tin nhắn thời gian thực qua kênh riêng /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                response
        );
        if (!recipientId.equals(senderId)) {
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/messages",
                    response
            );
        }
    }

    // 4. Ánh xạ dữ liệu phòng chat: Gọi IdentityInternalService lấy tên người dùng
    private ChatRoomResponse mapToChatRoomResponse(ChatRoom room, Long currentUserId) {
        String lastMessage = null;
        LocalDateTime lastMessageAt = null;

        var messageOpt = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(room.getId());
        if (messageOpt.isPresent()) {
            lastMessage = messageOpt.get().getContent();
            lastMessageAt = messageOpt.get().getCreatedAt();
        }

        long unreadCount = chatMessageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), currentUserId);

        String shopName = shopInternalService.getShopNameById(room.getShopId())
                .orElse("Shop #" + room.getShopId());

        // TUÂN THỦ MODULAR MONOLITH: Không gọi UserRepository, chỉ gọi Interface Contract
        String buyerName = identityInternalService.getUserFullNameOrDefault(room.getBuyerId());

        return ChatRoomResponse.builder()
                .id(room.getId())
                .buyerId(room.getBuyerId())
                .shopId(room.getShopId())
                .shopName(shopName)
                .buyerName(buyerName) // Bổ sung tên người mua cho góc nhìn của Shop
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .unreadCount(unreadCount)
                .updatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt() : room.getCreatedAt())
                .build();
    }
}
```

---


# PHẦN 3: TOÀN BỘ CẤU HÌNH HỆ THỐNG (CONFIGURATIONS) — GIẢI MÃ TỪNG DÒNG CODE

Trong một ứng dụng doanh nghiệp, tầng cấu hình (**Configuration Layer**) quyết định tính an toàn, hiệu năng mạng và khả năng giao tiếp thời gian thực của toàn bộ hệ thống. Dưới đây là 4 file cấu hình cốt lõi của dự án được giải phẫu chi tiết.

---

## 3.1. Cấu hình Bảo mật Spring Security 6 & CORS (`SecurityConfig.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/common/security/SecurityConfig.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/common/security/SecurityConfig.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${application.websocket.allowed-origins:http://localhost:[*],http://127.0.0.1:[*]}")
    private List<String> allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestSecurityExceptionHandler restSecurityExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger & OpenAPI Documentation (Public)
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()

                        // 2. Authentication & Realtime WebSocket handshake (Public)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()

                        // 3. Webhook IPN VNPay (Server-to-Server không có Token người dùng)
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/vnpay/ipn").permitAll()

                        // 4. Các API Đọc thông tin công khai (Browsing Catalog)
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/shops/**").permitAll()

                        // 5. Mọi request khác (Tạo đơn, Thanh toán, Admin, Vendor) bắt buộc có JWT
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler)
                )
                // 6. Cấu hình STATELESS: Không bao giờ lưu Session/Cookie trên RAM Server
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                // 7. Cắm JwtAuthenticationFilter đứng TRƯỚC filter xác thực UsernamePassword mặc định
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`@EnableMethodSecurity`:**
   - Kích hoạt phân quyền bảo mật cấp độ phương thức bằng annotation `@PreAuthorize("hasRole('ADMIN')")` hoặc `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`. Giúp bảo vệ Controller chặt chẽ ngay tại từng hàm xử lý.
2. **`csrf(AbstractHttpConfigurer::disable)` (Tại sao lại tắt CSRF?):**
   - **Bản chất CSRF (Cross-Site Request Forgery):** Là hình thức tấn công dựa vào việc trình duyệt tự động gửi kèm Cookie phiên đăng nhập (`JSESSIONID`) khi gọi request sang domain khác.
   - **Lý do dự án tắt CSRF:** Ứng dụng xây dựng theo chuẩn **REST API + JWT Bearer Token**. Token được lưu trữ tại `localStorage` của trình duyệt và chỉ được gửi kèm khi code JavaScript chủ động gán vào Header `Authorization: Bearer <token>`. Vì trình duyệt không bao giờ tự động đính kèm JWT vào request chéo domain, ứng dụng **miễn nhiễm tự nhiên với CSRF**. Tắt CSRF giúp tối ưu hiệu năng và loại bỏ cơ chế CSRF Token phức tạp không cần thiết.
3. **`SessionCreationPolicy.STATELESS`:**
   - Hướng dẫn Spring Boot **không bao giờ cấp phát `HttpSession` trên RAM**. Mỗi request gửi lên server là độc lập hoàn toàn. Ưu điểm: Tiết kiệm tối đa bộ nhớ máy chủ và sẵn sàng mở rộng theo chiều ngang (Horizontal Scaling / chạy nhiều container sau Load Balancer) mà không cần Sticky Session.
4. **`addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`:**
   - Đặt `JwtAuthenticationFilter` chạy trước bộ lọc kiểm tra tài khoản mặc định của Spring. Nhờ đó, token trong Header `Authorization` được bóc tách, xác thực chữ ký và nạp thông tin quyền hạn (`SecurityContextHolder`) trước khi bất kỳ Controller nào được triệu gọi.
5. **`CorsConfigurationSource`:**
   - Khắc phục rào cản cùng nguồn gốc của trình duyệt (SOP - Same-Origin Policy). Cho phép Client chạy ở port `5173` gọi API sang port `8080`, cho phép truyền Header `Authorization` và bật `allowCredentials(true)`.

---

## 3.2. Cấu hình WebSocket STOMP Message Broker (`WebSocketConfig.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/common/websocket/WebSocketConfig.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/common/websocket/WebSocketConfig.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final SocketSessions socketSessions;

    @Value("${application.websocket.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. Kích hoạt In-Memory Message Broker cho các kênh nhận tin (/queue riêng tư, /topic broadcast)
        registry.enableSimpleBroker("/queue", "/topic");

        // 2. Tiền tố cho message từ client gửi lên Controller (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // 3. Tiền tố gửi tin nhắn riêng đích danh từng người dùng: /user/{userId}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 4. Đăng ký Endpoint kết nối bắt tay WebSocket /ws với CORS pattern
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 5. Gắn Interceptor để xác thực JWT ngay khi Client gửi frame CONNECT
        registration.interceptors(jwtChannelInterceptor);
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`@EnableWebSocketMessageBroker`:**
   - Kích hoạt kiến trúc WebSocket có Message Broker dựa trên giao thức ứng dụng STOMP (Simple Text Oriented Messaging Protocol).
2. **`enableSimpleBroker("/queue", "/topic")`:**
   - `/topic`: Kênh truyền thông tin dạng Broadcast (1 người gửi, hàng ngàn người nhận - ví dụ flash sale toàn sàn).
   - `/queue`: Kênh truyền thông tin dạng Point-to-Point (1-1 riêng tư - ví dụ tin nhắn chat giữa khách hàng và gian hàng).
3. **`setUserDestinationPrefix("/user")`:**
   - Cho phép gửi tin nhắn đích danh tới từng người dùng. Khi phía server gọi `messagingTemplate.convertAndSendToUser(userId, "/queue/messages", payload)`, Spring Boot sẽ tự động chuyển đổi thành đường dẫn nội bộ `/user/{userId}/queue/messages`, chỉ duy nhất client của người dùng đó mới nhận được bản tin.
4. **`c.interceptors(inbound)` (`JwtChannelInterceptor`):**
   - WebSocket bắt đầu bằng một HTTP Handshake, sau đó nâng cấp lên kết nối TCP hai chiều. Vì kết nối STOMP sau đó không qua HTTP FilterChain thông thường, ta phải dùng Channel Interceptor để đọc `Authorization: Bearer <jwt>` từ Native Headers của frame `CONNECT`, giải mã danh tính và gán Principal cho phiên WebSocket đó.

---

## 3.3. Cấu hình Cloudinary Lưu trữ Media Đám mây (`CloudinaryConfig.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/media/config/CloudinaryConfig.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/media/config/CloudinaryConfig.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Configuration
public class CloudinaryConfig {

    @Value("${application.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${application.cloudinary.api-key}")
    private String apiKey;

    @Value("${application.cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`@Value("${application.cloudinary.cloud-name}")`:**
   - Trích xuất thông tin cấu hình từ file cấu hình `application.yml` hoặc biến môi trường container, không bao giờ để lộ API Key bí mật trong mã nguồn Git.
2. **`@Bean public Cloudinary cloudinary()`:**
   - Đăng ký đối tượng `Cloudinary` thành một Singleton Bean trong Spring IoC Container. Bất kỳ service nào (như `CloudinaryMediaService`) chỉ cần khai báo inject là có thể sử dụng ngay hàm upload ảnh lên Cloudinary CDN toàn cầu.
3. **`"secure", true`:**
   - Bắt buộc Cloudinary luôn trả về URL ảnh sử dụng giao thức bảo mật HTTPS (`https://res.cloudinary.com/...`), chống giả mạo và bảo mật hình ảnh hiển thị trên ứng dụng.

---

## 3.4. Cấu hình Cổng VNPay & Ký số HMAC-SHA512 (`VnPayStrategy.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/payment/strategy/VnPayStrategy.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/payment/strategy/VnPayStrategy.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Service
public class VnPayStrategy implements PaymentStrategy {
    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${payment.vnpay.secret-key}")
    private String secretKey;

    @Value("${payment.vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${payment.vnpay.return-url}")
    private String returnUrl;

    @Override
    public String createPaymentUrl(PaymentOrderData orderData, String ipAddress, String txnRef) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_ReturnUrl", returnUrl);

        // VNPay quy định số tiền phải nhân với 100 (đơn vị xu/hào)
        BigDecimal amountVnPay = orderData.amount().multiply(new BigDecimal("100"));
        vnpParams.put("vnp_Amount", String.valueOf(amountVnPay.longValue()));
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderData.description());
        vnpParams.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        vnpParams.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // BẮT BUỘC: Sắp xếp các tham số theo thứ tự Alphabet a-z trước khi băm
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append("=")
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        // Tạo chữ ký bảo mật HMAC-SHA512
        String vnpSecureHash = HashUtil.hmacSha512(secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnpPayUrl + "?" + query.toString();
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`vnp_Amount = amount * 100`:**
   - VNPay chuẩn hóa số tiền theo định dạng nguyên thủy không có dấu thập phân. Ví dụ: 50.000 VNĐ sẽ được nhân với 100 thành `5000000`.
2. **`vnp_ExpireDate = now.plusMinutes(15)`:**
   - Thiết lập thời gian sống của URL thanh toán là 15 phút. Quá 15 phút mà khách chưa hoàn tất quét mã QR hay nhập OTP ngân hàng, giao dịch tự động hết hạn, tránh treo đơn hàng.
3. **`Collections.sort(fieldNames)`:**
   - Nguyên tắc bất biến của thuật toán chữ ký: Các tham số phải được sắp xếp theo đúng thứ tự từ điển (alphabet) trước khi băm. Nếu không sắp xếp, chuỗi ký số sẽ không khớp và VNPay sẽ từ chối thanh toán với mã lỗi `Checksum Failed`.
4. **`HashUtil.hmacSha512(secretKey, hashData.toString())`:**
   - Mã hóa băm đối xứng với thuật toán HMAC-SHA512. Chống giả mạo số tiền và mã giao dịch trên đường truyền mạng Internet.

# PHẦN 4: HỆ THỐNG KIỂM TRA HỢP LỆ (VALIDATION) & XỬ LÝ LỖI TOÀN CỤC (EXCEPTION HANDLING)

Hệ thống chuyên nghiệp không bao giờ để ứng dụng bị văng lỗi 500 kèm stacktrace lộ thông tin nội bộ. Dự án đã xây dựng mô hình kiểm soát lỗi tập trung 2 tầng cực kỳ chặt chẽ: **Validate đầu vào tại DTO** $
ightarrow$ **Xử lý lỗi tập trung tại GlobalExceptionHandler** $
ightarrow$ **Ánh xạ lỗi lên giao diện người dùng qua apiError.ts**.

---

## 4.1. Bộ Bắt lỗi Tập trung `@RestControllerAdvice` (`GlobalExceptionHandler.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/common/exception/GlobalExceptionHandler.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/common/exception/GlobalExceptionHandler.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi nghiệp vụ có định danh ErrorCode (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return response(ex.getErrorCode(), ex.getMessage());
    }

    // 2. Bắt lỗi sai tham số Bean Validation (@Valid ở Controller)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error ->
                errors.putIfAbsent(
                        error instanceof FieldError field ? field.getField() : "_global",
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "Dữ liệu không hợp lệ"
                )
        );
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .status(400)
                .errorCode(ErrorCode.INVALID_INPUT.name())
                .message(ErrorCode.INVALID_INPUT.getDefaultMessage())
                .fieldErrors(errors)
                .build());
    }

    // 3. Bắt lỗi phân quyền không đủ thẩm quyền (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return response(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage());
    }

    // 4. Bắt lỗi chưa đăng nhập hoặc token không hợp lệ (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return response(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage());
    }

    // 5. Chốt chặn cuối cùng cho các lỗi không lường trước (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Unhandled API exception", ex);
        return response(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> response(ErrorCode code, String message) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.error(code.getHttpStatus().value(), code, message));
    }
}
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`@RestControllerAdvice`:**
   - Kết hợp giữa `@ControllerAdvice` và `@ResponseBody`. Nó biến class thành một "bộ đón lỗi toàn cầu" (Global Interceptor) cho toàn bộ các Controller trong ứng dụng. Lập trình viên không cần viết các khối `try-catch` lặp đi lặp lại ở từng hàm Controller.
2. **`ex.getBindingResult().getAllErrors().forEach(...)`:**
   - Khi một form gửi lên có nhiều trường sai (ví dụ: email để trống, mật khẩu dưới 6 ký tự), hàm này gom toàn bộ các vi phạm vào một `Map<String, String> errors` dạng:  
     `{"email": "Email không được để trống", "password": "Mật khẩu phải có ít nhất 6 ký tự"}`  
     và trả về client với mã HTTP 400.
3. **`fieldErrors(errors)` trong `ApiResponse`:**
   - Phản hồi JSON trả về có cấu trúc đồng nhất:
     ```json
     {
       "status": 400,
       "errorCode": "INVALID_INPUT",
       "message": "Dữ liệu đầu vào không hợp lệ",
       "fieldErrors": {
         "email": "Định dạng email không hợp lệ"
       }
     }
     ```
     Frontend có thể dễ dàng đọc key và hiển thị dòng chữ đỏ ngay bên dưới ô nhập liệu tương ứng.

---

## 4.2. Jakarta Bean Validation trên DTOs (`@Valid`, `@NotBlank`, `@Email`, v.v.)

*Vị trí file ví dụ:* [src/main/java/com/tmt/ecommerce/identity/dto/UserRegisterRequest.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/identity/dto/UserRegisterRequest.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Getter
@Setter
public class UserRegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;
}
```

### 📌 Kích hoạt tại Controller bằng `@Valid`:
```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody UserRegisterRequest request) { // <-- @Valid kích hoạt kiểm tra
    return ResponseEntity.ok(authService.register(request));
}
```

### 💡 Các Annotation Bean Validation được dùng phổ biến trong dự án:
- `@NotBlank`: Bắt buộc chuỗi không được `null`, không được rỗng và không được chỉ chứa dấu cách trắng (dùng cho tên sản phẩm, họ tên, mật khẩu).
- `@NotNull`: Bắt buộc đối tượng không được `null` (dùng cho Enum trạng thái, số tiền, ID).
- `@Email`: Kiểm tra định dạng email hợp lệ theo chuẩn RFC.
- `@Size(min = ..., max = ...)`: Ràng buộc độ dài chuỗi ký tự (ví dụ: lý do từ chối tối đa 500 ký tự).
- `@Min(value = 1)`: Ràng buộc giá trị số tối thiểu (ví dụ: số lượng mua trong giỏ hàng phải $\ge 1$).

---

## 4.3. Bảng Mã Lỗi Chuẩn hóa `ErrorCode.java` & Chuẩn Phản hồi `ApiResponse.java`

*Vị trí file:* [src/main/java/com/tmt/ecommerce/common/exception/ErrorCode.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/common/exception/ErrorCode.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Getter
public enum ErrorCode {
    // Xác thực & Phân quyền
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Yêu cầu đăng nhập"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Email đã được sử dụng"),

    // Gian hàng
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng"),
    SHOP_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Bạn đã sở hữu gian hàng rồi"),
    SHOP_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "Tên gian hàng đã tồn tại"),
    SHOP_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Gian hàng chưa được kích hoạt hoặc đã bị khóa"),
    SHOP_PRIOR_STATUS_MISSING(HttpStatus.CONFLICT, "Không thể tự động mở khóa; cần đối soát trạng thái trước khi khóa"),

    // Sản phẩm & Tồn kho
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"),
    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không đủ"),

    // Đơn hàng & Thanh toán
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"),
    ORDER_STATE_CONFLICT(HttpStatus.CONFLICT, "Trạng thái đơn hàng đã thay đổi; vui lòng tải lại"),
    INVALID_ORDER_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "Chuyển đổi trạng thái đơn hàng không hợp lệ"),
    REFUND_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "Idempotency key belongs to a different refund command"),

    // Mã giảm giá
    VOUCHER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá"),
    VOUCHER_EXPIRED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết hạn"),
    VOUCHER_USAGE_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết lượt sử dụng"),

    // Hệ thống
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang gặp sự cố, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
```

### 💡 Lợi ích kiến trúc:
Thay vì trả về các chuỗi String ngẫu nhiên, việc gắn từng lỗi với một Enum `ErrorCode` giúp:
1. Đảm bảo mã HTTP Status (`400`, `401`, `403`, `404`, `409`, `500`) luôn chính xác theo chuẩn RESTful.
2. Frontend có thể lập trình bắt mã lỗi theo `errorCode` (ví dụ `if (err.errorCode === 'INSUFFICIENT_STOCK')`) để hiển thị thông báo nghiệp vụ chuyên biệt thay vì dựa vào câu thông báo text tiếng Việt dễ thay đổi.

---

## 4.4. Cách Frontend Bắt & Ánh xạ Lỗi Chi tiết vào Form (`apiError.ts`)

*Vị trí file:* [frontend/src/api/apiError.ts](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/api/apiError.ts)

### 📌 Đoạn mã nguồn thực tế:
```typescript
import axios from 'axios';

export interface ApiErrorDetails {
  status?: number;
  errorCode?: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

export function getApiError(error: unknown, fallback = 'Không thể thực hiện yêu cầu.'): ApiErrorDetails {
  if (!axios.isAxiosError(error)) return { message: fallback };
  const body: unknown = error.response?.data;
  if (typeof body !== 'object' || body === null || Array.isArray(body)) {
    return { status: error.response?.status, message: fallback };
  }

  const res = body as Record<string, unknown>;
  const fields = res.fieldErrors;
  const fieldErrors = (typeof fields === 'object' && fields !== null && !Array.isArray(fields))
    ? Object.fromEntries(Object.entries(fields).filter((e): e is [string, string] => typeof e[1] === 'string'))
    : undefined;

  return {
    status: error.response?.status,
    errorCode: typeof res.errorCode === 'string' ? res.errorCode : undefined,
    message: typeof res.message === 'string' && res.message ? res.message : fallback,
    fieldErrors: fieldErrors && Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
  };
}
```

### 📌 Ứng dụng thực tế trên Form React (`RegisterForm.tsx`):
```tsx
const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    await authApi.register(formData);
    toast.success('Đăng ký tài khoản thành công!');
  } catch (err) {
    const apiError = getApiError(err);
    if (apiError.fieldErrors) {
      // Ánh xạ lỗi từng trường (email, password, fullName) vào State
      setFieldErrors(apiError.fieldErrors);
    } else {
      toast.error(apiError.message);
    }
  }
};

// Hiển thị lỗi bên dưới ô input:
<input name="email" value={formData.email} onChange={handleChange} />
{fieldErrors.email && <span className="text-red-500 text-sm">{fieldErrors.email}</span>}
```

# PHẦN 5: PHỦ TOÀN BỘ CHỨC NĂNG & NGHIỆP VỤ NÂNG CAO (ADMIN, VENDOR, CUSTOMER)

---

## 5.1. Admin: Phê duyệt, Khóa Shop & Khôi phục Trạng thái `prior_status` (`ShopService.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/shop/service/ShopService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/shop/service/ShopService.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Transactional
public void banShop(Long actorId, Long shopId, String reason) {
    // 1. Khóa ghi bi quan để chống tranh chấp dữ liệu khi nhiều admin cùng thao tác
    Shop shop = shopRepository.findByIdForUpdate(shopId)
            .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND, "Không tìm thấy gian hàng."));

    if (shop.getStatus() == ShopStatus.BANNED) {
        throw new AppException(ErrorCode.SHOP_INVALID_STATUS_TRANSITION, "Gian hàng này hiện đã bị khóa.");
    }

    if (reason != null && reason.length() > 500) throw new AppException(ErrorCode.INVALID_INPUT);

    // 2. LƯU LẠI TRẠNG THÁI TRƯỚC ĐÓ VÀO prior_status
    ShopStatus previous = shop.getStatus();
    shop.setPriorStatus(previous);
    shop.setStatus(ShopStatus.BANNED);

    // 3. Bắn Event thông báo trạng thái thay đổi
    eventPublisher.publishEvent(new ShopStatusChangedEvent(
            actorId, shopId, previous, ShopStatus.BANNED, reason));
    shopRepository.save(shop);
}

@Transactional
public void unbanShop(Long actorId, Long shopId) {
    Shop shop = shopRepository.findByIdForUpdate(shopId)
            .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND, "Không tìm thấy gian hàng."));

    if (shop.getStatus() != ShopStatus.BANNED) {
        throw new AppException(ErrorCode.SHOP_INVALID_STATUS_TRANSITION, "Gian hàng này không ở trạng thái bị khóa.");
    }

    // 4. ĐỌC LẠI prior_status ĐỂ KHÔI PHỤC ĐÚNG TRẠNG THÁI BAN ĐẦU
    ShopStatus restored = shop.getPriorStatus();
    if (restored != ShopStatus.ACTIVE && restored != ShopStatus.PENDING) {
        throw new AppException(ErrorCode.SHOP_PRIOR_STATUS_MISSING);
    }
    shop.setStatus(restored);
    shop.setPriorStatus(null); // Reset lại trường prior_status

    eventPublisher.publishEvent(new ShopStatusChangedEvent(
            actorId, shopId, ShopStatus.BANNED, restored, null));
    shopRepository.save(shop);
}
```

### 💡 Ý nghĩa nghiệp vụ của `prior_status`:
Một gian hàng khi bị Admin khóa vi phạm (`BANNED`) có thể xuất phát từ hai trạng thái:
1. Gian hàng đang chờ duyệt hồ sơ (`PENDING`).
2. Gian hàng đang hoạt động bình thường (`ACTIVE`).

Nếu không lưu `prior_status`, khi Admin bấm **Mở khóa (Unban)**, hệ thống sẽ không biết phải chuyển shop về đâu. Nếu tự động đưa về `ACTIVE`, một shop lừa đảo chưa từng được kiểm duyệt hồ sơ sẽ nghiễm nhiên được quyền bán hàng. Cột `prior_status` đảm bảo tính toàn vẹn nghiệp vụ và an toàn tuyệt đối cho sàn.

---

## 5.2. Admin: Can thiệp Cưỡng chế Trạng thái Đơn hàng & Audit Log (`AdminOrderService.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/order/service/AdminOrderService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/order/service/AdminOrderService.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Transactional
public OrderResponse update(Long id, AdminOrderStatusRequest request) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal))
        throw new AccessDeniedException("Missing admin principal");

    // 1. Khóa ghi đơn hàng
    Order order = load(id, true);

    // 2. Chống ghi đè trạng thái cũ (Optimistic Lock Guard qua expectedStatus)
    if (order.getStatus() != request.expectedStatus()) 
        throw new AppException(ErrorCode.ORDER_STATE_CONFLICT);

    // 3. Kiểm tra tính hợp lệ của luồng chuyển trạng thái
    if (!transitions(order.getStatus()).contains(request.status()))
        throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION);

    // 4. Kiểm tra điều kiện thanh toán (Đơn VNPay chưa thanh toán không được chuyển sang DELIVERED)
    if (!paymentAllows(order, request.status())) 
        throw new AppException(ErrorCode.ORDER_PAYMENT_CONDITION_NOT_MET);

    OrderStatus oldStatus = order.getStatus();
    
    // 5. Cập nhật trạng thái đơn hàng
    OrderResponse response = orderService.applyOrderTransition(order, request.status(),
            request.status() == OrderStatus.DELIVERED ? DeliveryConfirmationSource.ADMIN : null,
            request.status() == OrderStatus.DELIVERED ? principal.getId() : null);

    // 6. GHI LỊCH SỬ KIỂM TOÁN VÀO BẢNG order_admin_status_history
    history.saveAndFlush(OrderAdminStatusHistory.builder()
            .orderId(id)
            .actorUserId(principal.getId())
            .oldStatus(oldStatus)
            .newStatus(request.status())
            .reason(request.reason())
            .createdAt(Instant.now())
            .build());

    return response;
}
```

### 💡 Bản chất kỹ thuật & Điểm cộng đồ án:
- **Tranh chấp khiếu nại (Dispute Resolution):** Khi có mâu thuẫn giữa Người mua và Shop (ví dụ: Shop báo đã giao nhưng khách khiếu nại chưa nhận được), Admin có quyền tối cao để can thiệp trạng thái.
- **Audit Trail (Bảng `order_admin_status_history`):** Mọi hành động can thiệp của Admin đều được ghi nhận vĩnh viễn (Ai can thiệp? Trạng thái cũ là gì? Trạng thái mới là gì? Lý do can thiệp là gì? Vào thời gian nào?). Điều này ngăn chặn việc Admin nội bộ lạm quyền trục lợi.
- **`expectedStatus`:** Đảm bảo Admin luôn nhìn thấy trạng thái mới nhất trước khi ra quyết định, tránh tình trạng 2 Admin cùng lúc thao tác trên 1 đơn hàng bị xung đột.

---

## 5.3. Admin: Hoàn tiền VNPay với `Idempotency-Key` (`RefundService.java` & `RefundReservationService.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/payment/service/RefundReservationService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/payment/service/RefundReservationService.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Transactional
RefundAttempt reserve(RefundableOrderSnapshot order, Long actorId, String idempotencyKey, String reason) {
    // 1. Kiểm tra Idempotency-Key chống gửi lệnh hoàn tiền trùng lặp (Anti-Double Refund)
    var replay = refunds.findByIdempotencyKey(idempotencyKey);
    if (replay.isPresent()) {
        RefundAttempt existing = replay.get();
        if (!existing.getOrderId().equals(order.orderId()) || !existing.getActorUserId().equals(actorId)
                || !existing.getReason().equals(reason)) 
            throw new AppException(ErrorCode.REFUND_IDEMPOTENCY_CONFLICT);
        return existing; // Trả về kết quả cũ nếu cùng lệnh gọi lại
    }

    // 2. Khóa ghi giao dịch thanh toán gốc
    List<Payment> candidates = payments.findSuccessfulVnpayByGroupForUpdate(order.paymentGroupId());

    // Kiểm tra lại Idempotency-Key sau khi giữ khóa ghi (Double-Check Pattern)
    replay = refunds.findByIdempotencyKey(idempotencyKey);
    if (replay.isPresent()) {
        RefundAttempt existing = replay.get();
        if (!existing.getOrderId().equals(order.orderId()) || !existing.getActorUserId().equals(actorId)
                || !existing.getReason().equals(reason)) {
            throw new AppException(ErrorCode.REFUND_IDEMPOTENCY_CONFLICT);
        }
        return existing;
    }
    if (candidates.size() != 1) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);
    Payment payment = candidates.getFirst();

    // 3. KIỂM TRA CHẶNG CUỐI: Tổng số tiền hoàn không được vượt quá số tiền khách đã trả
    BigDecimal orderReserved = refunds.sumReservedAmountByOrderId(order.orderId(), RESERVED);
    BigDecimal amount = order.totalAmount().subtract(orderReserved);
    if (amount.signum() <= 0) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);

    BigDecimal paymentReserved = refunds.sumReservedAmountByPaymentId(payment.getId(), RESERVED);
    if (paymentReserved.add(amount).compareTo(payment.getAmount()) > 0) 
        throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);

    // 4. Lưu bản ghi giữ chỗ trạng thái REQUESTED
    return refunds.save(RefundAttempt.builder()
            .orderId(order.orderId())
            .paymentId(payment.getId())
            .paymentGroupId(order.paymentGroupId())
            .amount(amount)
            .currency("VND")
            .idempotencyKey(idempotencyKey)
            .merchantRequestId("R" + UUID.randomUUID().toString().replace("-", "").substring(0, 31))
            .status(RefundStatus.REQUESTED)
            .actorUserId(actorId)
            .reason(reason)
            .build());
}
```

### 💡 Tại sao cần `Idempotency-Key` trong nghiệp vụ hoàn tiền?
- Khi Admin ấn nút "Hoàn tiền cho khách", mạng có thể bị lag hoặc chập chờn. Nếu Admin mất kiên nhẫn bấm đúp (double-click) hoặc trình duyệt tự động gửi lại request (retry), nếu không có `Idempotency-Key`, hệ thống sẽ gọi API sang VNPay 2 lần $
ightarrow$ **Tài khoản công ty bị mất tiền 2 lần cho cùng 1 đơn hàng**.
- `Idempotency-Key` sinh ra ở máy khách (UUID ngẫu nhiên duy nhất cho mỗi phiên thao tác). Khi request thứ 2 bay đến, DB phát hiện key này đã có trong bảng `refund_attempts`, nó lập tức trả về kết quả của request đầu tiên mà không bắn API hoàn tiền lần nữa.

---

## 5.4. Admin: Tổng hợp Báo cáo Thống kê Toàn sàn qua Internal Contracts (`AdminStatisticsController.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/statistics/controller/AdminStatisticsController.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/statistics/controller/AdminStatisticsController.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderInternalService orderInternalService;
    private final IdentityInternalService identityInternalService;
    private final ShopInternalService shopInternalService;
    private final ProductInternalService productInternalService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardStatsResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end   = endDate   != null ? endDate.atTime(LocalTime.MAX) : null;

        // Gọi qua Internal Contract của từng Module riêng biệt
        long totalUsers    = identityInternalService.countUsers();
        long totalShops    = shopInternalService.countShops();
        long totalProducts = productInternalService.countProducts();
        DashboardOrderStatsDto orderStats = orderInternalService.getDashboardOrderStats(start, end);

        // Đóng gói dữ liệu biểu đồ và đơn hàng gần đây
        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse(
            totalUsers, totalShops, totalProducts,
            orderStats.totalOrders(), orderStats.totalRevenue(),
            orderStats.dailyChart(), orderStats.recentOrders()
        );

        return ApiResponse.success(response);
    }
}
```

### 💡 Điểm sáng kiến trúc Modular Monolith:
`AdminStatisticsController` cần số liệu từ 4 module khác nhau (User, Shop, Product, Order). Thay vì chọc thẳng vào các Repository của các module đó (vi phạm ranh giới), Controller này chỉ đứng ở tầng điều phối và gọi qua **Internal Service Interfaces**. Mỗi module chịu trách nhiệm tự đếm số liệu của mình và trả về DTO độc lập.

---

## 5.5. Vendor: Báo cáo Doanh thu & Doanh số Theo Ngày (`VendorAnalyticsService.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/statistics/service/VendorAnalyticsService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/statistics/service/VendorAnalyticsService.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Service
@RequiredArgsConstructor
public class VendorAnalyticsService {
    private final ShopInternalService shopInternalService;
    private final OrderInternalService orderInternalService;

    @Transactional(readOnly = true)
    public VendorAnalyticsResponse getOwnShopAnalytics(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        // 1. Tìm shopId thuộc sở hữu của Vendor đang đăng nhập
        Long shopId = shopInternalService.findShopIdByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime end = endDate == null ? null : endDate.atTime(java.time.LocalTime.MAX);

        // 2. Lấy dữ liệu phân tích tài chính chỉ thuộc về Shop này
        ShopOrderAnalyticsDto data = orderInternalService.getShopOrderAnalytics(shopId, start, end);

        return new VendorAnalyticsResponse(
                "ORDER_CREATED_AT",
                startDate, endDate,
                data.totalOrders(),
                data.ordersByStatus(),
                data.deliveredOrderCount(),
                data.fulfilledGrossOrderValue(),
                data.voucherDiscountAmount(),
                data.fulfilledOrderValue(),
                data.averageFulfilledOrderValue(),
                data.dailyChart().stream()
                        .map(point -> new VendorAnalyticsResponse.DailyChartPoint(
                                point.date(), point.createdOrderCount(), point.fulfilledOrderValue()))
                        .toList());
    }
}
```

### 💡 Bảo mật dữ liệu đa người bán (Multi-tenant Isolation):
Vendor chỉ được xem doanh thu của **chính gian hàng mình**. Hàm `shopInternalService.findShopIdByUserId(userId)` lấy `userId` từ JWT Token đã được xác thực, ngăn chặn hoàn toàn việc Vendor này xem trộm dữ liệu tài chính của Vendor khác.

---

## 5.6. Vendor: Quản lý Biến thể Sản phẩm Động Lưu JSON MySQL (`ProductVariant.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/product/entity/ProductVariant.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/entity/ProductVariant.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Entity
@Table(name = "product_variants")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // HIBERNATE 6: Ánh xạ tự động Map<String, Object> thành cột JSON MySQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "json")
    private Map<String, Object> attributes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;
}
```

### 💡 Giải pháp kỹ thuật đỉnh cao:
- **Vấn đề thực tế của E-commerce:** Quần áo thì có thuộc tính `{ "Màu sắc": "Đỏ", "Size": "XL" }`, còn Điện thoại thì có `{ "Dung lượng": "256GB", "Màu": "Titan" }`. Nếu dùng bảng quan hệ truyền thống EAV (Entity-Attribute-Value), ta phải JOIN 4-5 bảng cực kỳ chậm.
- **Giải pháp:** Sử dụng tính năng native JSON của MySQL kết hợp với `@JdbcTypeCode(SqlTypes.JSON)` của Hibernate 6. Java làm việc với `Map<String, Object>` cực kỳ linh hoạt, còn Database lưu trữ dạng Binary JSON tối ưu dung lượng và hỗ trợ truy vấn chỉ mục.

---

## 5.7. Vendor: Phản hồi Đánh giá Khách hàng & Kiểm tra Quyền Gian hàng (`VendorReviewController.java`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/review/service/ReviewServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/review/service/ReviewServiceImpl.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional
public ReviewResponse replyReview(Long vendorUserId, Long reviewId, VendorReplyRequest request) {
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài đánh giá."));

    // 1. Kiểm tra sản phẩm này thuộc Shop nào qua ProductInternalService
    Long shopId = productInternalService.getShopIdByProductId(review.getProductId());

    // 2. Kiểm tra User đang đăng nhập có phải là chủ sở hữu của Shop đó hay không
    if (shopId == null || !shopInternalService.isShopOwner(shopId, vendorUserId)) {
        throw new IllegalArgumentException("Bạn không có quyền phản hồi đánh giá của sản phẩm này.");
    }

    // 3. Lưu phản hồi của Người bán và thời gian phản hồi
    review.setVendorReply(request.replyComment());
    review.setVendorRepliedAt(LocalDateTime.now());

    review = reviewRepository.save(review);
    return mapToReviewResponse(review);
}
```

---

## 5.8. Customer: Thuật toán Gợi ý Sản phẩm Thông minh Đa tầng (`ProductServiceImpl.getRecommendedProducts`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/product/service/ProductServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/product/service/ProductServiceImpl.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional(readOnly = true)
public Page<ProductResponse> getRecommendedProducts(Long userId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 50));
    int bufferLimit = safeLimit * 3;

    Set<Product> recommendationSet = new LinkedHashSet<>();
    List<Long> purchasedProductIds = new ArrayList<>();
    List<Long> bannedShopIds = shopInternalService.getBannedShopIds();

    // TẦNG 1: GỢI Ý CÁ NHÂN HÓA (Dựa trên lịch sử mua sắm của khách)
    if (userId != null) {
        List<Long> recentVariantIds = orderInternalService.getRecentlyPurchasedVariantIds(userId, bufferLimit);
        if (!recentVariantIds.isEmpty()) {
            List<ProductVariant> purchasedVariants = productVariantRepository.findByIdIn(recentVariantIds);
            List<Product> purchasedProducts = purchasedVariants.stream()
                    .map(ProductVariant::getProduct)
                    .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                    .distinct().toList();

            purchasedProductIds = purchasedProducts.stream().map(Product::getId).toList();
            List<Long> categoryIds = purchasedProducts.stream()
                    .map(p -> p.getCategory().getId()).distinct().toList();

            if (!categoryIds.isEmpty()) {
                Pageable pageable = PageRequest.of(0, safeLimit, Sort.by("id").descending());
                // Tìm các sản phẩm cùng danh mục nhưng loại trừ sản phẩm đã mua và shop bị cấm
                List<Product> personalized = productRepository.findByCategoryIdsExcluding(
                        categoryIds, purchasedProductIds, ProductStatus.ACTIVE, bannedShopIds, pageable);
                recommendationSet.addAll(personalized);
            }
        }
    }

    // TẦNG 2: FALLBACK SẢN PHẨM BÁN CHẠY NHẤT TOÀN SÀN (Top-Selling Products)
    if (recommendationSet.size() < safeLimit) {
        List<Long> topVariantIds = orderInternalService.getTopSellingVariantIds(bufferLimit);
        if (!topVariantIds.isEmpty()) {
            List<ProductVariant> topVariants = productVariantRepository.findByIdIn(topVariantIds);
            for (ProductVariant pv : topVariants) {
                Product p = pv.getProduct();
                if (p.getStatus() == ProductStatus.ACTIVE && !recommendationSet.contains(p)
                        && !purchasedProductIds.contains(p.getId()) && !bannedShopIds.contains(p.getShopId())) {
                    recommendationSet.add(p);
                    if (recommendationSet.size() >= safeLimit) break;
                }
            }
        }
    }

    // TẦNG 3: FALLBACK SẢN PHẨM MỚI NHẤT (Newest Active Products)
    if (recommendationSet.size() < safeLimit) {
        int needed = safeLimit - recommendationSet.size();
        Pageable pageable = PageRequest.of(0, needed, Sort.by("id").descending());
        List<Long> excluded = new ArrayList<>(purchasedProductIds);
        recommendationSet.forEach(p -> excluded.add(p.getId()));

        List<Product> newest = productRepository.findNewestExcluding(excluded, ProductStatus.ACTIVE, bannedShopIds, pageable);
        recommendationSet.addAll(newest);
    }

    List<Product> finalProducts = recommendationSet.stream().limit(safeLimit).toList();
    return new PageImpl<>(mapToProductResponsesSafe(finalProducts));
}
```

### 💡 Giải thích thuật toán gợi ý (Hybrid Recommendation Algorithm):
1. **Tier 1 (Personalized Preference):** Khách hàng đã mua điện thoại thì hệ thống sẽ ưu tiên gợi ý các sản phẩm thuộc danh mục "Phụ kiện điện thoại", "Thiết bị công nghệ" nhưng không gợi ý lại chính chiếc điện thoại đã mua.
2. **Tier 2 (Social Proof / Trending):** Nếu khách hàng là người mới toanh (Cold-start problem) hoặc chưa đủ sản phẩm cùng danh mục, hệ thống tự động bù đắp bằng các sản phẩm có lượt mua cao nhất sàn.
3. **Tier 3 (Freshness Fallback):** Nếu vẫn chưa đủ số lượng yêu cầu, hệ thống sẽ lấy các sản phẩm mới lên sàn gần đây nhất.
4. **Bộ lọc rác:** Luôn chủ động loại bỏ sản phẩm của các shop đang bị Admin khóa (`bannedShopIds`).

---

## 5.9. Customer: Thống kê Điểm Đánh giá & Phân bổ 1-5 Sao (`ReviewServiceImpl.getRatingSummary`)

*Vị trí file:* [src/main/java/com/tmt/ecommerce/review/service/ReviewServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/review/service/ReviewServiceImpl.java)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional(readOnly = true)
public RatingSummaryResponse getRatingSummary(Long productId) {
    Long totalReviews = reviewRepository.countByProductId(productId);
    if (totalReviews == 0) {
        return new RatingSummaryResponse(0.0, 0L, Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L));
    }

    // 1. Tính điểm trung bình và làm tròn 1 chữ số thập phân (ví dụ: 4.8 sao)
    Double avgRatingRaw = reviewRepository.getAverageRatingByProductId(productId);
    Double averageRating = Math.round(avgRatingRaw * 10.0) / 10.0;

    // 2. Truy vấn gom nhóm số lượt đánh giá theo từng số sao (GROUP BY rating)
    List<Object[]> ratingCountsRaw = reviewRepository.getRatingCountsByProductId(productId);
    Map<Integer, Long> ratingCounts = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
        ratingCounts.put(i, 0L); // Khởi tạo giá trị mặc định cho đủ 5 mức sao
    }
    for (Object[] row : ratingCountsRaw) {
        Integer rating = (Integer) row[0];
        Long count = (Long) row[1];
        ratingCounts.put(rating, count);
    }

    return new RatingSummaryResponse(averageRating, totalReviews, ratingCounts);
}
```

### 💡 Trải nghiệm người dùng (UX):
Hàm này cung cấp dữ liệu cho biểu đồ thanh sao trên trang chi tiết sản phẩm (Bao nhiêu % đánh giá 5 sao, 4 sao, 3 sao...), giúp người mua có cái nhìn trực quan và minh bạch trước khi quyết định mua hàng.

# PHẦN 6: TRỌN BỘ 20 CÂU HỎI VẤN ĐÁP THỰC CHIẾN & MẪU TRẢ LỜI GHI ĐIỂM TUYỆT ĐỐI

### Câu 1: Em hãy giải thích luồng hoạt động từ lúc người dùng gõ URL đến khi nhìn thấy sản phẩm trên màn hình?
- **Trả lời:**
  1. Trình duyệt tải ứng dụng React SPA từ Vite Server.
  2. Router kích hoạt component `ProductListPage.tsx`. Hook `useEffect` chạy hàm `fetchProducts()`.
  3. `productApi.getAllProducts()` dùng `apiClient` (Axios) gửi request HTTP GET tới `http://localhost:8080/api/v1/products`.
  4. Phía Backend: `JwtAuthenticationFilter` kiểm tra request (cho qua vì API public trong `SecurityConfig`). `ProductController.getAllProducts()` nhận request, gọi `ProductServiceImpl.getAllProducts()`.
  5. `ProductRepository` chạy truy vấn Hibernate xuống MySQL kèm `@EntityGraph` để lấy sản phẩm và danh mục chỉ trong 1 câu lệnh SQL duy nhất.
  6. Controller đóng gói kết quả vào `ApiResponse<Page<ProductResponse>>` gửi về dạng JSON (HTTP 200).
  7. Axios nhận JSON, hàm setter `setProducts(data)` được gọi $
ightarrow$ React re-render và hiển thị các Card sản phẩm lên màn hình.

---

### Câu 2: Tại sao em lại chia hệ thống thành Modular Monolith thay vì Microservices hay Monolith truyền thống?
- **Trả lời:** *"Dạ thưa thầy/cô:
  - Nếu làm **Monolith truyền thống (Spaghetti code)**: Các module gọi entity, repository của nhau bừa bãi, sửa 1 module sẽ làm hỏng toàn bộ hệ thống, cực kỳ khó mở rộng.
  - Nếu làm **Microservices**: Quá cồng kềnh đối với quy mô hiện tại, tốn kém tài nguyên máy chủ, gặp vấn đề trễ mạng (Network Latency) và đau đầu với bài toán nhất quán dữ liệu phân tán (Distributed Transactions / Saga pattern).
  - **Modular Monolith** là sự kết hợp hoàn hảo nhất: Chạy chung 1 tiến trình Spring Boot (triển khai cực kỳ nhẹ nhàng), nhưng bên trong code được phân lập nghiêm ngặt: Mỗi module có ranh giới riêng, không chọc trực tiếp vào DB của nhau mà bắt buộc giao tiếp qua các **Internal Service & DTO**. Khi công ty phát triển có hàng triệu người dùng, ta có thể bốc từng module ra thành microservice độc lập chỉ trong vài ngày mà không phải viết lại logic."*

---

### Câu 3: Làm thế nào em đảm bảo 2 người không mua vượt quá số lượng tồn kho (Race Condition)?
- **Trả lời:** *"Dạ thưa thầy/cô, em sử dụng kỹ thuật **Pessimistic Concurrency Control (Khóa ghi bi quan)** ở tầng Database:
  Trong hàm `ProductInternalServiceImpl.deductStock`, em chạy câu lệnh SQL có điều kiện kiểm tra tồn kho nguyên tử:
  `UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?;`
  Database sẽ khóa dòng bản ghi đó lại. Request nào đến trước sẽ trừ kho thành công. Request nào đến sau nếu thấy kho không còn đủ sẽ nhận số dòng cập nhật là 0. Khi đó code của em ném lỗi `InsufficientStockException` để Rollback giao dịch của người đến sau, đảm bảo tồn kho không bao giờ bị âm."*

---

### Câu 4: Snapshot Pattern trong bảng `order_items` giải quyết vấn đề gì?
- **Trả lời:** *"Dạ thưa thầy/cô, trong bảng `order_items`, em cố tình lưu lại `unit_price`, `product_name` và `image_url` tại thời điểm đặt đơn. Nếu em chỉ lưu `product_variant_id` rồi JOIN sang bảng sản phẩm để lấy giá, thì khi người bán đổi tên hoặc tăng giá từ 100k lên 200k, toàn bộ lịch sử đơn hàng cũ của khách hàng sẽ bị hiển thị sai giá và báo cáo doanh thu tài chính trong quá khứ bị sai lệch hoàn toàn. Snapshot Pattern giúp hóa đơn có giá trị bất biến theo thời gian."*

---

### Câu 5: Hệ thống xử lý thế nào nếu khách hàng thanh toán VNPay xong tắt máy không quay lại web?
- **Trả lời:** *"Dạ hệ thống của em vẫn cập nhật đơn hàng thành công 100%. Bởi vì VNPay có cơ chế **IPN (Instant Payment Notification)** là một Webhook ngầm gọi trực tiếp từ server VNPay sang server của em (`/api/v1/payments/vnpay/ipn`). Khi VNPay gửi IPN, hệ thống của em xác thực chữ ký số HMAC-SHA512, dùng khóa `findByTransactionIdForUpdate` để khóa đơn, so khớp số tiền và xác nhận đơn hàng độc lập, hoàn toàn không phụ thuộc vào việc trình duyệt của khách có mở hay không."*

---

### Câu 6: Lỗi N+1 Query là gì và em đã xử lý nó như thế nào?
- **Trả lời:** *"Dạ N+1 Query xảy ra khi ta truy vấn 1 danh sách cha (ví dụ 10 sản phẩm) và Hibernate tự động bắn thêm 10 câu lệnh SQL phụ để lấy danh mục con (Category). Để giải quyết:
  1. Với quan hệ `@ManyToOne`, em dùng `@EntityGraph(attributePaths = {"category"})` trong `ProductRepository` để Hibernate sinh câu lệnh `LEFT JOIN` lấy cả sản phẩm và danh mục chỉ trong **1 câu SQL duy nhất**.
  2. Với quan hệ `@OneToMany`, em gắn `@BatchSize(size = 50)` trên entity `Order` để Hibernate gom nhiều ID thành câu lệnh `WHERE order_id IN (?, ?, ...)` tối đa 50 đơn 1 lần, loại bỏ hoàn toàn việc bắn query lẻ tẻ."*

---

### Câu 7: Tại sao hàm gửi thông báo lại dùng `@TransactionalEventListener(phase = AFTER_COMMIT)`?
- **Trả lời:** *"Dạ thưa thầy/cô, nếu dùng EventListener thông thường, sự kiện thông báo sẽ chạy cùng trong luồng đặt hàng. Nếu việc lưu đơn hàng vào DB bị lỗi và phải Rollback, nhưng thông báo đã lỡ gửi đi rồi thì khách hàng sẽ nhận được thông báo 'Đặt hàng thành công' cho một đơn hàng không hề tồn tại. Sử dụng `phase = AFTER_COMMIT` đảm bảo thông báo chỉ được tạo khi và chỉ khi toàn bộ dữ liệu đơn hàng đã được ghi xuống đĩa cứng MySQL thành công 100%."*

---

### Câu 8: Thuộc tính `prior_status` trong bảng `shops` có ý nghĩa gì?
- **Trả lời:** *"Dạ đây là chi tiết nghiệp vụ em thiết kế: Một shop khi bị Admin khóa (`BANNED`) có thể trước đó đang ở trạng thái hoạt động (`ACTIVE`) hoặc đang chờ duyệt (`PENDING`). Em lưu trạng thái cũ vào `prior_status`. Khi Admin mở khóa (`UNBAN`), hệ thống đọc lại `prior_status` để trả shop về đúng trạng thái nguyên bản của nó, tránh việc một shop chưa từng được duyệt hồ sơ sau khi unban lại vô tình trở thành active."*

---

### Câu 9: Trình bày quy trình hoàn tiền (Refund) đơn hàng qua VNPay?
- **Trả lời:** *"Dạ khi khách hủy đơn đã thanh toán online hoặc Admin kích hoạt hoàn tiền:
  1. `AdminRefundController` gọi `RefundService.create(orderId, actorId, idempotencyKey, reason)`.
  2. Hệ thống kiểm tra `Idempotency-Key` trong bảng `refund_attempts` để chống gửi lệnh trùng lặp.
  3. Dùng khóa bi quan `findSuccessfulVnpayByGroupForUpdate` để khóa giao dịch thanh toán gốc và kiểm tra tổng tiền hoàn không vượt quá số tiền khách đã trả.
  4. Gửi request có chữ ký HMAC sang cổng API hoàn tiền của VNPay. Khi VNPay xác nhận mã `00`, hệ thống cập nhật trạng thái `CONFIRMED`."*

---

### Câu 10: WebSocket STOMP khác gì so với Raw WebSocket?
- **Trả lời:** *"Dạ Raw WebSocket chỉ là một đường truyền byte thô hai chiều, không có định dạng bản tin hay cơ chế định tuyến người nhận. STOMP là giao thức tầng ứng dụng chạy trên WebSocket, có sẵn các lệnh chuẩn như `CONNECT`, `SUBSCRIBE`, `SEND`. Kết hợp với `SimpMessagingTemplate` của Spring Boot, em có thể đẩy tin nhắn tới đích danh từng user qua destination `/user/queue/messages` một cách bảo mật và cực kỳ gọn gàng."*

---

### Câu 11: Tại sao trong Spring Security em lại tắt CSRF (`http.csrf(AbstractHttpConfigurer::disable)`)? Tắt như vậy có bị hack không?
- **Trả lời:** *"Dạ hoàn toàn không bị hack ạ. CSRF là lỗ hổng khai thác cơ chế tự động đính kèm Cookie (`JSESSIONID`) của trình duyệt khi người dùng bị lừa bấm vào link độc hại. Nhưng hệ thống của em sử dụng kiến trúc **Stateless REST API với JWT**. Phía máy chủ không lưu Session Cookie nào cả; JWT được lưu ở `localStorage` của trình duyệt và chỉ gửi lên server khi code React chủ động gán vào Header `Authorization: Bearer <token>`. Vì trình duyệt không bao giờ tự ý gửi Bearer Header chéo domain, ứng dụng miễn nhiễm tự nhiên với CSRF. Do đó việc tắt CSRF là chuẩn thiết kế quốc tế cho các hệ thống RESTful JWT."*

---

### Câu 12: Làm thế nào em xác thực người dùng trong kết nối WebSocket STOMP?
- **Trả lời:** *"Dạ khi client thiết lập kết nối WebSocket:
  1. Trình duyệt bắt đầu bằng một HTTP Handshake tới `/ws` (kênh này được permitAll).
  2. Ngay sau khi kết nối TCP mở, thư viện `@stomp/stompjs` gửi frame `CONNECT` có đính kèm Native Header: `Authorization: Bearer <token>`.
  3. Phía Backend, em đăng ký `JwtChannelInterceptor` trong `configureClientInboundChannel(WebSocketConfig)`. Interceptor này chặn frame `CONNECT`, bóc tách JWT Token, xác thực chữ ký và trích xuất `userId`, sau đó gán `StompPrincipal` vào Header phiên WebSocket. Các frame tin nhắn sau đó được server nhận diện tự động mà không cần truyền lại token."*

---

### Câu 13: Trình bày luồng kiểm tra dữ liệu (Validation) từ DTO Backend đến hiển thị lỗi trên React?
- **Trả lời:** *"Dạ quy trình khép kín gồm 4 bước:
  1. Tại Backend DTO: Em khai báo các annotation Jakarta Bean Validation như `@NotBlank`, `@Email`, `@Size(min=6)`.
  2. Tại Controller: Em đặt annotation `@Valid @RequestBody UserRegisterRequest request`. Nếu dữ liệu vi phạm, Spring tự động ném ra `MethodArgumentNotValidException`.
  3. Tại `GlobalExceptionHandler`: Hàm bắt lỗi trích xuất danh sách các trường vi phạm và thông điệp lỗi tương ứng vào một Map `fieldErrors` và trả về mã HTTP 400 Bad Request.
  4. Tại Frontend: Hàm `getApiError(err)` trong `apiError.ts` bóc tách `fieldErrors` này vào state của Component, giúp hiển thị dòng thông báo đỏ ngay dưới đúng ô nhập liệu của người dùng."*

---

### Câu 14: Tại sao em lại dùng Pessimistic Lock (`findByIdForUpdate`) khi Admin Khóa Shop (`banShop`)?
- **Trả lời:** *"Dạ thưa thầy/cô, để chống **tranh chấp đồng thời (Concurrency Race Condition)**: Nếu cùng một thời điểm có 2 Admin cùng mở trang quản trị và thao tác trên cùng 1 Shop (ví dụ Admin A bấm Khóa shop vi phạm, cùng lúc Admin B bấm Đổi thông tin shop), việc sử dụng câu lệnh SQL `SELECT ... FOR UPDATE` sẽ khóa dòng dữ liệu của Shop đó trong MySQL. Thao tác của Admin thứ hai buộc phải xếp hàng chờ cho đến khi giao dịch của Admin thứ nhất hoàn tất và Commit, đảm bảo trạng thái của Shop không bao giờ bị ghi đè sai lệch."*

---

### Câu 15: Khi Admin cưỡng chế đổi trạng thái đơn hàng (Forced Transition), em xử lý audit log thế nào?
- **Trả lời:** *"Dạ trong `AdminOrderService.update`:
  1. Kiểm tra quyền Admin qua `@PreAuthorize("hasRole('ADMIN')")`.
  2. Kiểm tra `expectedStatus` để chắc chắn trạng thái đơn hàng trên màn hình Admin không bị cũ so với Database.
  3. Kiểm tra tính hợp lệ của luồng chuyển trạng thái (ví dụ đơn đã DELIVERED thì không thể chuyển về CONFIRMED).
  4. Kiểm tra điều kiện thanh toán (Đơn VNPay chưa thanh toán thành công thì không được phép chuyển sang SHIPPED hay DELIVERED).
  5. Sau khi cập nhật đơn, em lưu ngay một bản ghi vào bảng `order_admin_status_history` ghi rõ: Admin ID, trạng thái cũ, trạng thái mới, lý do can thiệp và thời gian thực hiện để phục vụ công tác kiểm toán nội bộ."*

---

### Câu 16: Cơ chế hoàn tiền tự động VNPay hoạt động ra sao và tại sao cần `Idempotency-Key`?
- **Trả lời:** *"Dạ khi hủy đơn đã thanh toán VNPay:
  1. Client gửi kèm header `Idempotency-Key` (chuỗi UUID duy nhất).
  2. `RefundReservationService` kiểm tra nếu key này đã tồn tại thì trả về kết quả cũ ngay lập tức, ngăn chặn việc Admin bấm đúp (double click) dẫn đến hệ thống gọi lệnh hoàn tiền 2 lần sang ngân hàng gây thất thoát tiền bạc.
  3. Kiểm tra số tiền hoàn không vượt quá số tiền thanh toán ban đầu.
  4. Tạo bản ghi `refund_attempts` trạng thái `REQUESTED` và gửi request có chữ ký HMAC sang API Hoàn tiền của VNPay. Khi VNPay phản hồi mã `00`, hệ thống chốt trạng thái `CONFIRMED`."*

---

### Câu 17: Thuật toán gợi ý sản phẩm (Recommendation System) trong dự án của em hoạt động theo nguyên lý gì?
- **Trả lời:** *"Dạ em xây dựng thuật toán **Gợi ý hỗn hợp đa tầng (Multi-tier Hybrid Recommendation)** trong `ProductServiceImpl`:
  - **Tầng 1 (Cá nhân hóa):** Dựa vào lịch sử các đơn hàng gần đây của khách, lấy ra danh sách danh mục họ quan tâm và tìm các sản phẩm cùng danh mục mà họ chưa từng mua.
  - **Tầng 2 (Bán chạy / Trending Fallback):** Nếu khách hàng là người dùng mới chưa có lịch sử mua sắm (Cold Start), hệ thống tự động bù đắp bằng các sản phẩm có lượt bán cao nhất trên toàn sàn.
  - **Tầng 3 (Sản phẩm mới nhất Fallback):** Bổ sung các sản phẩm mới lên sàn để kích cầu.
  - Đặc biệt, ở mọi tầng truy vấn, hệ thống luôn chủ động loại bỏ các sản phẩm thuộc về gian hàng đang bị khóa (`bannedShopIds`)."*

---

### Câu 18: Làm thế nào em thiết kế lưu trữ các biến thể sản phẩm động (như size, màu sắc, dung lượng) mà không cần tạo hàng chục bảng con trong Database?
- **Trả lời:** *"Dạ em sử dụng tính năng **JSON Column** của MySQL kết hợp với annotation `@JdbcTypeCode(SqlTypes.JSON)` của Hibernate 6 trên entity `ProductVariant`.
  Thuộc tính `private Map<String, Object> attributes;` cho phép mỗi biến thể lưu các cặp key-value tùy ý (ví dụ áo thì lưu `{ "Màu": "Đen", "Size": "L" }`, điện thoại thì lưu `{ "Bộ nhớ": "128GB" }`). Cách thiết kế này vừa giữ được tốc độ truy vấn nhanh chóng chỉ trong 1 bảng `product_variants`, vừa loại bỏ hoàn toàn việc phải chạy migrate thêm cột DB mỗi khi shop bán một mặt hàng mới."*

---

### Câu 19: Tại sao trong phương thức `VendorAnalyticsService` hoặc `AdminStatisticsController`, em không viết query JOIN trực tiếp giữa các bảng `orders`, `users` và `shops`?
- **Trả lời:** *"Dạ vì hệ thống của em tuân thủ nghiêm ngặt nguyên lý **Modular Monolith**:
  Module `statistics` không được phép chọc trực tiếp vào Repository hay Entity của module `order`, `identity` hay `shop`. Nếu JOIN trực tiếp qua database, ranh giới module sẽ bị phá vỡ, mã nguồn bị thắt nút phụ thuộc chặt chẽ (tight coupling). Em cho module thống kê giao tiếp qua các **Internal Service Interfaces** (`OrderInternalService`, `ShopInternalService`, `IdentityInternalService`). Nhờ vậy, nếu sau này tách module Order thành một Microservice riêng có Database độc lập, module thống kê vẫn hoạt động bình thường mà không cần sửa một dòng code nào."*

---

### Câu 20: Khi upload ảnh sản phẩm lên Cloudinary, luồng xử lý diễn ra như thế nào và em bảo mật thông tin API Key/Secret ra sao?
- **Trả lời:** *"Dạ quy trình xử lý như sau:
  1. Người dùng chọn ảnh trên giao diện React, file được gửi dạng `multipart/form-data` lên endpoint `/api/v1/media/upload`.
  2. Phía Backend, `CloudinaryMediaService` nhận `MultipartFile`, kiểm tra định dạng và dung lượng cho phép.
  3. Service gọi SDK `cloudinary.uploader().upload(file.getBytes(), ...)` để tải ảnh trực tiếp lên máy chủ đám mây của Cloudinary.
  4. Cloudinary trả về đường dẫn ảnh bảo mật HTTPS (`https://res.cloudinary.com/...`). Backend trả URL này về cho Frontend lưu vào form sản phẩm.
  5. Về bảo mật: Toàn bộ `cloud_name`, `api_key` và `api_secret` được đặt trong file `application.yml` đọc qua biến môi trường (`@Value`), tuyệt đối không hard-code vào code hay đẩy lên Git công khai."*
