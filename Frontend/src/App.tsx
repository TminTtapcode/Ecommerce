import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { AdminLayout } from './components/layout/AdminLayout';
import { PublicLayout } from './components/layout/PublicLayout';
import { ProductListPage } from './pages/public/ProductListPage';
import { ProductDetailPage } from './pages/public/ProductDetailPage';
import { AdminProductListPage } from './pages/admin/AdminProductListPage';
import { AdminProductFormPage } from './pages/admin/AdminProductFormPage';
import { CartProvider } from './contexts/CartContext';
import { CartPage } from './pages/public/CartPage';
import { CheckoutPage } from './pages/public/CheckoutPage';
import { OrderHistoryPage } from './pages/public/OrderHistoryPage';
import { OrderDetailPage } from './pages/public/OrderDetailPage';
import { PaymentResultPage } from './pages/public/PaymentResultPage';
import { VendorOrderListPage } from './pages/vendor/VendorOrderListPage';

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<Navigate to="/products" replace />} />
          
          <Route element={<PublicLayout />}>
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/cart" element={<ProtectedRoute requireAdmin={false}><CartPage /></ProtectedRoute>} />
            <Route path="/checkout" element={<ProtectedRoute requireAdmin={false}><CheckoutPage /></ProtectedRoute>} />
            <Route path="/payment-result" element={<ProtectedRoute requireAdmin={false}><PaymentResultPage /></ProtectedRoute>} />
            <Route path="/orders" element={<ProtectedRoute requireAdmin={false}><OrderHistoryPage /></ProtectedRoute>} />
            <Route path="/orders/:id" element={<ProtectedRoute requireAdmin={false}><OrderDetailPage /></ProtectedRoute>} />
          </Route>
          
          {/* Vendor Routes */}
          <Route element={<PublicLayout />}>
            <Route path="/vendor/orders" element={<ProtectedRoute requireAdmin={false}><VendorOrderListPage /></ProtectedRoute>} />
            <Route path="/vendor" element={<ProtectedRoute requireAdmin={false}><VendorOrderListPage /></ProtectedRoute>} />
            <Route path="/seller" element={<ProtectedRoute requireAdmin={false}><VendorOrderListPage /></ProtectedRoute>} />
          </Route>
          
          {/* Admin Protected Routes */}
          <Route path="/admin" element={<ProtectedRoute requireAdmin={true}><AdminLayout /></ProtectedRoute>}>
            <Route index element={
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-2xl font-bold mb-4">Tổng quan Dashboard</h2>
                <p>Chào mừng bạn đến với trang quản trị.</p>
              </div>
            } />
            <Route path="products" element={<AdminProductListPage />} />
            <Route path="products/create" element={<AdminProductFormPage />} />
            <Route path="products/:id/edit" element={<AdminProductFormPage />} />
          </Route>
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
