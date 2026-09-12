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
3. [Phần 3: Bộ 20 Câu Hỏi Vấn Đáp Thực Chiến & Mẫu Trả Lời Ghi Điểm Tuyệt Đối](#phần-3-bộ-20-câu-hỏi-vấn-đáp-thực-chiến--mẫu-trả-lời-ghi-điểm-tuyệt-đối)

---

# PHẦN 1: GIẢI MÃ CÚ PHÁP FRONTEND QUA MÃ NGUỒN THỰC TẾ

## 1.1. Cấu hình Mạng & Token Interceptor (`apiClient.ts`)

*Vị trí file:* [frontend/src/api/apiClient.ts](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/api/apiClient.ts)

### 📌 Đoạn mã nguồn thực tế:
```typescript
import axios from 'axios';

// 1. Tạo một Axios instance dùng chung cho toàn bộ ứng dụng
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. REQUEST INTERCEPTOR: Chạy trước khi bất kỳ request nào bay ra mạng
apiClient.interceptors.request.use(
  (config) => {
    // Lấy token JWT từ LocalStorage của trình duyệt
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      // Tự động gắn Bearer Token vào Header Authorization
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. RESPONSE INTERCEPTOR: Chạy ngay khi nhận được phản hồi từ Backend
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Nếu Backend trả về 401 Unauthorized (Token hết hạn hoặc không hợp lệ)
    if (error.response?.status === 401) {
      localStorage.removeItem('token'); // Xóa token cũ
      // Tự động chuyển hướng về trang đăng nhập
      if (!error.config?.url?.startsWith('/api/v1/auth/')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`import.meta.env.VITE_API_URL || 'http://localhost:8080'`:**
   - `import.meta.env`: Cú pháp của công cụ Vite để đọc biến môi trường từ file `.env`.
   - `||` (Toán tử OR): Nếu không tìm thấy biến môi trường, tự động fallback về URL mặc định `http://localhost:8080`.
2. **`apiClient.interceptors.request.use(...)` (Interceptor là gì?):**
   - Giống như một **"trạm kiểm soát vé"**. Mỗi khi một component (như `CartPage`, `OrderPage`) gọi `apiClient.get(...)`, interceptor này tự động chèn thêm `Authorization: Bearer <jwt>` vào Header. Lợi ích: Lập trình viên không cần viết thủ công việc lấy token ở hàng trăm hàm gọi API.
3. **`apiClient.interceptors.response.use(...)`:**
   - Xử lý lỗi tập trung (Global Error Handler). Nếu token JWT hết hạn (sau 24h), backend trả về mã `401 Unauthorized`, interceptor lập tức xóa token khỏi máy khách và điều hướng người dùng về trang `/login`.

> **Hội đồng sẽ hỏi:** *"Tại sao em không dùng hàm `fetch()` mặc định của trình duyệt mà lại dùng thư viện `axios`?"*  
> **Trả lời:** *"Dạ thưa thầy/cô, `axios` cung cấp tính năng **Interceptors** cực kỳ mạnh mẽ giúp em tự động đính kèm JWT token vào mọi request và tự động bắt lỗi 401 tập trung tại 1 nơi duy nhất. Ngoài ra, `axios` tự động parse dữ liệu JSON và có cơ chế thiết lập timeout, giúp code ngắn gọn và dễ bảo trì hơn `fetch` thuần rất nhiều."*

---

## 1.2. Quản lý Đăng nhập Toàn cục với Context & Hook (`AuthContext.tsx`)

*Vị trí file:* [frontend/src/contexts/AuthContext.tsx](file:///c:/Users/Dell/IdeaProjects/ecommerce/frontend/src/contexts/AuthContext.tsx)

### 📌 Đoạn mã nguồn thực tế:
```tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { decodeUser } from './authSession';

// 1. Định nghĩa kiểu dữ liệu (Interface) cho vùng nhớ Context
interface AuthContextType {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isVendor: boolean;
  user: UserPayload | null;
  login: (token: string) => void;
  logout: () => void;
}

// 2. Tạo Context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// 3. Provider bao bọc toàn bộ ứng dụng
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // useState khởi tạo user bằng cách giải mã token trong LocalStorage
  const [user, setUser] = useState(() => decodeUser(localStorage.getItem('token')));

  const login = (token: string) => {
    localStorage.setItem('token', token);
    setUser(decodeUser(token)); // Cập nhật state -> Giao diện tự đổi sang Đã đăng nhập
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null); // Xóa user -> Giao diện tự đổi về Chưa đăng nhập
  };

  const isAuthenticated = !!user;
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const isVendor = user?.roles?.includes('ROLE_VENDOR') ?? false;

  return (
    <AuthContext.Provider value={{ isAuthenticated, isAdmin, isVendor, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// 4. Custom Hook để các Component khác sử dụng nhanh
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
```

### 💡 Giải thích từng cú pháp & Bản chất kỹ thuật:
1. **`createContext` & `useContext`:**
   - Giải quyết bài toán **Prop Drilling** (truyền dữ liệu qua quá nhiều cấp component trung gian). Bất kỳ trang nào trong web chỉ cần gọi `const { isVendor, logout } = useAuth();` là có thể lấy dữ liệu ngay lập tức.
2. **`useState(() => decodeUser(...))` (Lazy Initialization):**
   - Thay vì truyền trực tiếp giá trị vào `useState(decodeUser(...))`, ta truyền một hàm callback `() => ...`. Cú pháp này giúp thao tác giải mã JWT chỉ chạy **đúng 1 lần duy nhất** khi web vừa tải, tránh việc giải mã lại mỗi lần component render, giúp tối ưu hiệu năng.
3. **Toán tử `!!user`:**
   - Ép kiểu đối tượng thành Boolean. Nếu `user` là một object có dữ liệu $\rightarrow$ `true`; nếu `user` là `null` hoặc `undefined` $\rightarrow$ `false`.
4. **Toán tử Optional Chaining `?.` và Nullish Coalescing `??`:**
   - `user?.roles?.includes('ROLE_ADMIN') ?? false`: Nếu `user` hoặc `roles` bị `null`, code không bị văng lỗi crash màn hình, mà trả về giá trị mặc định là `false`.

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
import SockJS from 'sockjs-client';

export class WebSocketService {
  private client: Client | null = null;

  connect(token: string, onMessageReceived: (message: any) => void) {
    // 1. Khởi tạo kết nối qua SockJS dự phòng nếu trình duyệt không hỗ trợ WebSocket thuần
    const socket = new SockJS('http://localhost:8080/ws');
    
    // 2. Cấu hình STOMP Client
    this.client = new Client({
      webSocketFactory: () => socket,
      // Gửi JWT Token trong Header STOMP CONNECT frame
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      onConnect: () => {
        console.log('STOMP Connected successfully!');
        // 3. Đăng ký kênh nhận tin nhắn riêng của User hiện tại
        this.client?.subscribe('/user/queue/messages', (frame) => {
          const message = JSON.parse(frame.body);
          onMessageReceived(message); // Gọi callback truyền tin nhắn vào React state
        });
      },
      onStompError: (frame) => {
        console.error('Broker error: ' + frame.headers['message']);
      }
    });

    this.client.activate(); // Kích hoạt kết nối
  }

  // 4. Gửi tin nhắn lên server qua kênh /app/chat.send
  sendMessage(roomId: number, content: string) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ roomId, content })
      });
    }
  }
}
```

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

## 2.7. Gửi Tin nhắn Realtime qua STOMP (`ChatServiceImpl.java`)

*Vị trí file:* [ChatServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/chat/service/ChatServiceImpl.java#L90-L123)

### 📌 Đoạn mã nguồn thực tế:
```java
@Override
@Transactional
public void sendMessage(ChatMessageRequest request, Long senderId) {
    ChatRoom room = chatRoomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ChatRoomNotFoundException("Phòng chat không tồn tại"));

    // 1. Kiểm tra quyền truy cập (Người gửi phải là Buyer hoặc Chủ shop của phòng này)
    validateAccess(room, senderId);

    // 2. Lưu tin nhắn vào Database
    ChatMessage message = ChatMessage.builder()
            .roomId(room.getId())
            .senderId(senderId)
            .content(request.getContent())
            .build();
    message = chatMessageRepository.save(message);

    // Cập nhật thời gian phòng chat có tin nhắn mới
    room.setUpdatedAt(LocalDateTime.now());
    chatRoomRepository.save(room);

    // 3. Xác định người nhận tin nhắn là ai
    Long recipientId = getRecipientId(room, senderId);
    ChatMessageResponse response = mapToChatMessageResponse(message);

    // 4. BẮN TIN NHẮN REALTIME QUA STOMP:
    // Đẩy tin nhắn tới người nhận qua kênh riêng /user/{recipientId}/queue/messages
    messagingTemplate.convertAndSendToUser(
            recipientId.toString(),
            "/queue/messages",
            response
    );
    // Đẩy lại cho chính người gửi để cập nhật tin nhắn trên đa thiết bị
    if (!recipientId.equals(senderId)) {
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/messages",
                response
        );
    }
}
```

---

# PHẦN 3: BỘ 20 CÂU HỎI VẤN ĐÁP THỰC CHIẾN & MẪU TRẢ LỜI GHI ĐIỂM TUYỆT ĐỐI

### Câu 1: Em hãy giải thích luồng hoạt động từ lúc người dùng gõ URL đến khi nhìn thấy sản phẩm trên màn hình?
- **Trả lời:**
  1. Trình duyệt tải ứng dụng React SPA từ Vite Server.
  2. Router kích hoạt component `ProductListPage.tsx`. Hook `useEffect` chạy hàm `fetchProducts()`.
  3. `productApi.getAllProducts()` dùng `apiClient` (Axios) gửi request HTTP GET tới `http://localhost:8080/api/v1/products`.
  4. Phía Backend: `JwtAuthenticationFilter` kiểm tra request (cho qua vì API public). `ProductController.getAllProducts()` nhận request, gọi `ProductServiceImpl.getAllProducts()`.
  5. `ProductRepository` chạy truy vấn Hibernate xuống MySQL kèm `@EntityGraph` để lấy sản phẩm và danh mục chỉ trong 1 câu lệnh SQL.
  6. Controller đóng gói kết quả vào `ApiResponse<Page<ProductResponse>>` gửi về dạng JSON (HTTP 200).
  7. Axios nhận JSON, hàm setter `setProducts(data)` được gọi $\rightarrow$ React re-render và hiển thị các Card sản phẩm lên màn hình.

---

### Câu 2: Tại sao em lại chia hệ thống thành Modular Monolith thay vì Microservices hay Monolith truyền thống?
- **Trả lời:** *"Dạ thưa thầy/cô:
  - Nếu làm **Monolith truyền thống (Spaghetti code)**: Các module gọi entity, repository của nhau bừa bãi, sửa 1 module sẽ làm hỏng toàn bộ hệ thống, cực kỳ khó mở rộng.
  - Nếu làm **Microservices**: Quá cồng kềnh đối với quy mô đồ án, tốn kém tài nguyên máy chủ, gặp vấn đề trễ mạng (Network Latency) và đau đầu với Distributed Transactions (Saga pattern).
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
  1. `AdminRefundController` gọi `RefundService.processRefund(orderId)`.
  2. Hệ thống tạo bản ghi trong bảng `refund_attempts` với `idempotency_key` duy nhất để chống Admin double-click hoàn tiền 2 lần.
  3. `VnPayRefundGateway` gửi request có chữ ký HMAC sang cổng API hoàn tiền của VNPay.
  4. Khi VNPay phản hồi mã `00` thành công, hệ thống cập nhật bản ghi `refund_attempts` thành `SUCCESS` và cập nhật đơn hàng hoàn tất."*

---

### Câu 10: WebSocket STOMP khác gì so với Raw WebSocket?
- **Trả lời:** *"Dạ Raw WebSocket chỉ là một đường truyền byte thô hai chiều, không có định dạng bản tin hay cơ chế định tuyến người nhận. STOMP là giao thức tầng ứng dụng chạy trên WebSocket, có sẵn các lệnh chuẩn như `CONNECT`, `SUBSCRIBE`, `SEND`. Kết hợp với `SimpMessagingTemplate` của Spring Boot, em có thể đẩy tin nhắn tới đích danh từng user qua destination `/user/queue/messages` một cách bảo mật và cực kỳ gọn gàng."*
