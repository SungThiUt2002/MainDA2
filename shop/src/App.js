// app.js

import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginForm from "./features/auth/LoginForm";
import RegisterForm from "./features/auth/RegisterForm";
import PrivateRoute from "./routes/PrivateRoute";

import AdminDashboard from "./features/admin/AdminDashboard";
import CartPage from "./features/cart/CartPage";
import CheckoutPage from "./features/checkout/CheckoutPage";
import OrderHistoryPage from "./features/orders/OrderHistoryPage";
import GlobalLoader from "./components/GlobalLoader";
import ProductDetailPage from "./features/products/ProductDetailPage";
// import UserManagement from "./features/User/UserManagement";
import HomePage from "./features/home/HomePage";
import CategoryProducts from "./features/categories/CategoryProducts"; // ✅ Thêm trang danh mục
import ProfilePage from "./features/profile/ProfilePage"; // ✅ Thêm trang tài khoản


const Unauthorized = () => <h2>Không có quyền truy cập</h2>;

function App() {
  return (
    <>
      <GlobalLoader />
      <Router>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/register" element={<RegisterForm />} />

          <Route element={<PrivateRoute allowedRoles={["ADMIN"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            {/* <Route path="/admin/users" element={<UserManagement />} /> */}
          </Route>

          <Route element={<PrivateRoute allowedRoles={["USER"]} />}>
            <Route path="/cart" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/orders" element={<OrderHistoryPage />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>

          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/category/:categoryId" element={<CategoryProducts />} />

          <Route path="/unauthorized" element={<Unauthorized />} />
        </Routes>
      </Router>
    </>
  );
}

export default App;
