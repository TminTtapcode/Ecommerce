import { NotificationProvider } from './contexts/NotificationContext';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { AdminLayout } from './components/layout/AdminLayout';
import { VendorLayout } from './components/layout/VendorLayout';
import { PublicLayout } from './components/layout/PublicLayout';
import { ProductListPage } from './pages/public/ProductListPage';
import { ProductDetailPage } from './pages/public/ProductDetailPage';
import { ShopProfilePage } from './pages/public/ShopProfilePage';
import { AdminProductListPage } from './pages/admin/AdminProductListPage';
import { AdminProductFormPage } from './pages/admin/AdminProductFormPage';
import { CartProvider } from './contexts/CartContext';
import { ChatProvider } from './contexts/ChatContext';
import { CartPage } from './pages/public/CartPage';
import { CheckoutPage } from './pages/public/CheckoutPage';
import { OrderHistoryPage } from './pages/public/OrderHistoryPage';
import { OrderDetailPage } from './pages/public/OrderDetailPage';
import { PaymentResultPage } from './pages/public/PaymentResultPage';
import { VendorOrderListPage } from './pages/vendor/VendorOrderListPage';
import { VendorProductListPage } from './pages/vendor/VendorProductListPage';
import { VendorProductFormPage } from './pages/vendor/VendorProductFormPage';
import { ChatTestPage } from './pages/public/ChatTestPage';
import { ProfilePage } from './pages/public/ProfilePage';
import { RequireShop } from './components/vendor/RequireShop';
import { AdminShopListPage } from './pages/admin/AdminShopListPage';
import { AdminOrderListPage } from './pages/admin/AdminOrderListPage';
import { AdminOrderDetailPage } from './pages/admin/AdminOrderDetailPage';
import { AdminVoucherListPage } from './pages/admin/AdminVoucherListPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import { VendorVoucherListPage } from './pages/vendor/VendorVoucherListPage';
import { VendorAnalyticsPage } from './pages/vendor/VendorAnalyticsPage';
import { VendorOverviewPage } from './pages/vendor/VendorOverviewPage';
import { VendorPayoutPage } from './pages/vendor/VendorPayoutPage';
import { AdminPayoutListPage } from './pages/admin/AdminPayoutListPage';

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <NotificationProvider>
            <ChatProvider>
              <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<Navigate to="/products" replace />} />

          <Route element={<PublicLayout />}>
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/shops/:id" element={<ShopProfilePage />} />
            <Route path="/cart" element={<ProtectedRoute requireAdmin={false}><CartPage /></ProtectedRoute>} />
            <Route path="/checkout" element={<ProtectedRoute requireAdmin={false}><CheckoutPage /></ProtectedRoute>} />
            <Route path="/payment-result" element={<ProtectedRoute requireAdmin={false}><PaymentResultPage /></ProtectedRoute>} />
            <Route path="/orders" element={<ProtectedRoute requireAdmin={false}><OrderHistoryPage /></ProtectedRoute>} />
            <Route path="/orders/:id" element={<ProtectedRoute requireAdmin={false}><OrderDetailPage /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute requireAdmin={false}><ProfilePage /></ProtectedRoute>} />
            <Route path="/chat-test" element={<ProtectedRoute requireAdmin={false}><ChatTestPage /></ProtectedRoute>} />
          </Route>

          <Route
            path="/vendor"
            element={
              <ProtectedRoute requireAdmin={false}>
                <RequireShop allowBanned>
                  <VendorLayout />
                </RequireShop>
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/vendor/overview" replace />} />
            <Route path="overview" element={<VendorOverviewPage />} />
            <Route path="products" element={<VendorProductListPage />} />
            <Route path="products/new" element={<RequireShop><VendorProductFormPage /></RequireShop>} />
            <Route path="products/:id/edit" element={<VendorProductFormPage />} />
            <Route path="orders" element={<VendorOrderListPage />} />
            <Route path="vouchers" element={<RequireShop><VendorVoucherListPage /></RequireShop>} />
            <Route path="analytics" element={<VendorAnalyticsPage />} />
            <Route path="payouts" element={<VendorPayoutPage />} />
          </Route>
          <Route path="/seller" element={<Navigate to="/vendor/overview" replace />} />

          <Route path="/admin" element={<ProtectedRoute requireAdmin={true}><AdminLayout /></ProtectedRoute>}>
            <Route index element={<AdminDashboardPage />} />
            <Route path="products" element={<AdminProductListPage />} />
            <Route path="products/create" element={<AdminProductFormPage />} />
            <Route path="products/:id/edit" element={<AdminProductFormPage />} />
            <Route path="shop" element={<AdminShopListPage />} />
            <Route path="orders" element={<AdminOrderListPage />} />
            <Route path="orders/:id" element={<AdminOrderDetailPage />} />
            <Route path="vouchers" element={<AdminVoucherListPage />} />
            <Route path="payouts" element={<AdminPayoutListPage />} />
          </Route>
              </Routes>
            </ChatProvider>
          </NotificationProvider>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
