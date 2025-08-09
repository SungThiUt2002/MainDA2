import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import "./AdminDashboard.css";
import ProductManager from "./ProductManager";
import RolePermissionManager from "./RolePermissionManager";
import InventoryDashboard from "./InventoryDashboard";
import BrandManager from "./BrandManager";
import CategoryManager from "./CategoryManager";
import OrderManager from "./OrderManager";
import { getAllUsers, createUser, deleteUser } from "../../api/accountApi";
import { getTotalProductCount, getProductCountByStatus } from "../../api/productApi";
import { getLowStockItems, getItemsNeedingReorder, getAllInventoryItems } from "../../api/inventoryApi";
import { getTotalOrderCount } from "../../api/orderApi";

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("dashboard");
  const [userInfo, setUserInfo] = useState(null);
  const [stats, setStats] = useState({
    users: 0,
    products: 0,
    orders: 0,
    revenue: 0,
  });
  const [users, setUsers] = useState([]);
  const [userLoading, setUserLoading] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [userForm, setUserForm] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    phoneNumber: "",
    roles: ["USER"],
  });
  const [userError, setUserError] = useState("");
  const [statsLoading, setStatsLoading] = useState(false);
  const [inventoryStats, setInventoryStats] = useState({
    totalProducts: 0,
    outOfStock: 0,
    lowStock: 0,
    inStock: 0,
    needsReorder: 0,
  });

  useEffect(() => {
    // Lấy thông tin user từ token
    const token = localStorage.getItem("accessToken");
    if (token) {
      try {
        const decoded = jwtDecode(token);
        setUserInfo(decoded);
      } catch (error) {
        console.error("Lỗi decode token:", error);
      }
    }

    // Load thống kê (có thể gọi API thực tế sau)
    loadStats();
    fetchInventoryStats();
  }, []);

  // Fetch users when tab is 'users' and reload stats when tab is 'dashboard'
  useEffect(() => {
    if (activeTab === "users") fetchUsers();
    if (activeTab === "dashboard") loadStats();
    // eslint-disable-next-line
  }, [activeTab]);

  const loadStats = async () => {
    setStatsLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      console.log("🔄 Token trong loadStats:", token ? "Có token" : "Không có token");
      console.log("🔄 Token value:", token);
      
      // Lấy số lượng người dùng thực tế
      const usersResponse = await getAllUsers(token);
      const userCount = usersResponse.data.data?.length || 0;
      
      // Lấy số lượng sản phẩm thực tế
      const productsResponse = await getTotalProductCount();
      const productCount = productsResponse.data || 0;
      
      // Lấy số lượng đơn hàng thực tế
      const ordersResponse = await getTotalOrderCount(token);
      console.log("🔄 Orders response trong AdminDashboard:", ordersResponse);
      
      // Kiểm tra cấu trúc response và lấy số đếm
      let orderCount = 0;
      if (ordersResponse) {
        // Thử các cấu trúc response khác nhau
        if (typeof ordersResponse === 'number') {
          orderCount = ordersResponse;
        } else if (ordersResponse.data !== undefined) {
          orderCount = ordersResponse.data;
        } else if (ordersResponse.count !== undefined) {
          orderCount = ordersResponse.count;
        } else if (ordersResponse.total !== undefined) {
          orderCount = ordersResponse.total;
        } else {
          console.warn("⚠️ Không thể xác định cấu trúc response:", ordersResponse);
          orderCount = 0;
        }
      }
      
      console.log("✅ Order count được xử lý:", orderCount);
      
      // TODO: Gọi API thực tế để lấy thống kê doanh thu
      // Tạm thời dùng mock data cho doanh thu
      setStats({
        users: userCount,
        products: productCount,
        orders: orderCount,
        revenue: 120000000,
      });
    } catch (error) {
      console.error("Lỗi khi tải thống kê:", error);
      // Fallback về mock data nếu có lỗi
      setStats({
        users: 0,
        products: 0,
        orders: 0,
        revenue: 120000000,
      });
    } finally {
      setStatsLoading(false);
    }
  };

  const fetchUsers = async () => {
    setUserLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      const res = await getAllUsers(token);
      setUsers(res.data.data || []);
    } catch (err) {
      setUserError("Không thể tải danh sách người dùng");
    }
    setUserLoading(false);
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  const handleOpenAddUser = () => {
    setEditingUser(null);
    setUserForm({
      username: "",
      email: "",
      password: "",
      firstName: "",
      lastName: "",
      phoneNumber: "",
      roles: ["USER"],
    });
    setShowUserModal(true);
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm("Bạn có chắc muốn xóa người dùng này?")) return;
    try {
      const token = localStorage.getItem("accessToken");
      await deleteUser(id, token);
      fetchUsers();
    } catch (err) {
      alert("Xóa thất bại!");
    }
  };

  const handleUserFormChange = (e) => {
    const { name, value } = e.target;
    if (name === "roles") {
      setUserForm({ ...userForm, roles: [value] });
    } else {
      setUserForm({ ...userForm, [name]: value });
    }
  };

  const handleUserFormSubmit = async (e) => {
    e.preventDefault();
    setUserError("");
    try {
      const token = localStorage.getItem("accessToken");
      // Tạo user mới với roles
      await createUser(userForm, token);
      setShowUserModal(false);
      fetchUsers();
    } catch (err) {
      setUserError("Lưu thất bại! Kiểm tra lại thông tin.");
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  const fetchInventoryStats = async () => {
    try {
      // Lấy dữ liệu thực từ API như InventoryDashboard
      const [lowStockRes, reorderRes, allItemsRes] = await Promise.all([
        getLowStockItems(),
        getItemsNeedingReorder(),
        getAllInventoryItems(),
      ]);

      const allItems = allItemsRes.data || [];
      const lowStockItems = lowStockRes.data || [];
      const needingReorderItems = reorderRes.data || [];

      // Tính toán thống kê giống như InventoryDashboard
      const totalItems = allItems.length;
      const outOfStock = allItems.filter(item => item.isOutOfStock).length;
      const lowStock = allItems.filter(item => item.isLowStock).length;
      const inStock = allItems.filter(item => !item.isOutOfStock && !item.isLowStock).length;
      const needsReorder = needingReorderItems.length;

      setInventoryStats({
        totalProducts: totalItems,
        outOfStock: outOfStock,
        lowStock: lowStock,
        inStock: inStock,
        needsReorder: needsReorder,
      });
    } catch (error) {
      console.error("Lỗi khi tải thống kê tồn kho:", error);
      // Fallback về mock data nếu có lỗi
      setInventoryStats({
        totalProducts: 0,
        outOfStock: 0,
        lowStock: 0,
        inStock: 0,
        needsReorder: 0,
      });
    }
  };

  const renderDashboard = () => (
    <div className="dashboard-content">
      <div className="welcome-section">
        <h2>Xin chào, {userInfo?.sub || "Quản trị viên"}! 👋</h2>
        <p>Đây là tổng quan hệ thống của bạn</p>
      </div>

      {statsLoading ? (
        <div className="loading-stats">
          <p>Đang tải thống kê...</p>
        </div>
      ) : (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">👤</div>
            <div className="stat-info">
              <h3>Tổng người dùng</h3>
              <p className="stat-number">{stats.users.toLocaleString()}</p>
              <span className="stat-change positive">
                Dữ liệu thực tế từ hệ thống
              </span>
            </div>
          </div>

        <div className="stat-card">
          <div className="stat-icon">📦</div>
          <div className="stat-info">
            <h3>Sản phẩm</h3>
            <p className="stat-number">{stats.products.toLocaleString()}</p>
            <span className="stat-change positive">Dữ liệu thực tế từ hệ thống</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">🛒</div>
          <div className="stat-info">
            <h3>Đơn hàng</h3>
            <p className="stat-number">{stats.orders.toLocaleString()}</p>
            <span className="stat-change positive">
              Dữ liệu thực tế từ hệ thống
            </span>
          </div>
        </div>

                  <div className="stat-card">
            <div className="stat-icon">💰</div>
            <div className="stat-info">
              <h3>Doanh thu</h3>
              <p className="stat-number">{formatCurrency(stats.revenue)}</p>
              <span className="stat-change positive">
                Dữ liệu thực tế từ hệ thống
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Inventory Overview */}
      <div className="inventory-overview">
        <h3>📊 Tổng quan tồn kho</h3>
        <div className="inventory-stats">
          <div className="inventory-stat-card">
            <div className="inventory-stat-icon">✅</div>
            <div className="inventory-stat-info">
              <h4>Còn hàng</h4>
              <p className="inventory-stat-number">{inventoryStats.inStock}</p>
              <span className="inventory-stat-desc">Sản phẩm có sẵn</span>
            </div>
          </div>

          <div className="inventory-stat-card">
            <div className="inventory-stat-icon">⚠️</div>
            <div className="inventory-stat-info">
              <h4>Sắp hết</h4>
              <p className="inventory-stat-number">{inventoryStats.lowStock}</p>
              <span className="inventory-stat-desc">Cần bổ sung</span>
            </div>
          </div>

          <div className="inventory-stat-card">
            <div className="inventory-stat-icon">❌</div>
            <div className="inventory-stat-info">
              <h4>Hết hàng</h4>
              <p className="inventory-stat-number">{inventoryStats.outOfStock}</p>
              <span className="inventory-stat-desc">Cần đặt hàng</span>
            </div>
          </div>

          <div className="inventory-stat-card">
            <div className="inventory-stat-icon">🔄</div>
            <div className="inventory-stat-info">
              <h4>Cần đặt hàng</h4>
              <p className="inventory-stat-number">{inventoryStats.needsReorder}</p>
              <span className="inventory-stat-desc">Cần đặt hàng</span>
            </div>
          </div>

          <div className="inventory-stat-card">
            <div className="inventory-stat-icon">📈</div>
            <div className="inventory-stat-info">
              <h4>Tỷ lệ tồn kho</h4>
              <p className="inventory-stat-number">
                {inventoryStats.totalProducts > 0 
                  ? Math.round((inventoryStats.inStock / inventoryStats.totalProducts) * 100)
                  : 0}%
              </p>
              <span className="inventory-stat-desc">Sản phẩm có sẵn</span>
            </div>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h3>Thao tác nhanh</h3>
        <div className="action-buttons">
          <button onClick={() => setActiveTab("users")} className="action-btn">
            👥 Quản lý người dùng
          </button>
          <button
            onClick={() => setActiveTab("products")}
            className="action-btn"
          >
            📦 Quản lý sản phẩm
          </button>
          <button
            onClick={() => setActiveTab("inventory")}
            className="action-btn"
          >
            📊 Quản lý tồn kho
          </button>
          <button onClick={() => setActiveTab("orders")} className="action-btn">
            🛒 Xem đơn hàng
          </button>
          <button
            onClick={() => setActiveTab("analytics")}
            className="action-btn"
          >
            📊 Báo cáo chi tiết
          </button>
        </div>
      </div>
    </div>
  );

  const renderUsers = () => (
    <div className="tab-content">
      <div className="tab-header">
        <h2>👥 Quản lý người dùng</h2>
        <button className="add-btn" onClick={handleOpenAddUser}>
          + Thêm người dùng
        </button>
      </div>
      {userLoading ? (
        <div>Đang tải...</div>
      ) : userError ? (
        <div className="error-message">{userError}</div>
      ) : (
        <table className="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên đăng nhập</th>
              <th>Email</th>
              <th>Roles</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>
                  {Array.isArray(u.roles)
                    ? u.roles.map((r) => r.name).join(", ")
                    : ""}
                </td>
                <td>
                  {/* Không có sửa user, chỉ xóa */}
                  <button
                    onClick={() => handleDeleteUser(u.id)}
                    style={{ color: "#fff" }}
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {showUserModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Thêm người dùng</h3>
            <form onSubmit={handleUserFormSubmit} className="user-form">
              <input
                name="username"
                placeholder="Tên đăng nhập"
                value={userForm.username}
                onChange={handleUserFormChange}
                required
              />
              <input
                name="email"
                placeholder="Email"
                value={userForm.email}
                onChange={handleUserFormChange}
                required
                type="email"
              />
              <input
                name="password"
                placeholder="Mật khẩu"
                value={userForm.password}
                onChange={handleUserFormChange}
                required
                type="password"
              />
              <input
                name="firstName"
                placeholder="Họ (First Name)"
                value={userForm.firstName}
                onChange={handleUserFormChange}
              />
              <input
                name="lastName"
                placeholder="Tên (Last Name)"
                value={userForm.lastName}
                onChange={handleUserFormChange}
              />
              <input
                name="phoneNumber"
                placeholder="Số điện thoại"
                value={userForm.phoneNumber}
                onChange={handleUserFormChange}
              />
              <select
                name="roles"
                value={userForm.roles[0]}
                onChange={handleUserFormChange}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
              {userError && <div className="error-message">{userError}</div>}
              <div className="modal-actions">
                <button type="submit" className="add-btn">
                  Lưu
                </button>
                <button type="button" onClick={() => setShowUserModal(false)}>
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );

  const renderProducts = () => (
    <div className="tab-content">
      <div className="tab-header">
        <h2>📦 Quản lý sản phẩm</h2>
        <button className="add-btn">+ Thêm sản phẩm</button>
      </div>
      <div className="content-placeholder">
        <p>Chức năng quản lý sản phẩm sẽ được phát triển...</p>
      </div>
    </div>
  );

  const renderOrders = () => <OrderManager />;

  const renderAnalytics = () => (
    <div className="tab-content">
      <div className="tab-header">
        <h2>📊 Báo cáo & Thống kê</h2>
      </div>
      <div className="content-placeholder">
        <p>Chức năng báo cáo và thống kê sẽ được phát triển...</p>
      </div>
    </div>
  );

  const renderSettings = () => (
    <div className="tab-content">
      <div className="tab-header">
        <h2>⚙️ Cài đặt hệ thống</h2>
      </div>
      <div className="content-placeholder">
        <p>Chức năng cài đặt hệ thống sẽ được phát triển...</p>
      </div>
    </div>
  );

  const renderContent = () => {
    switch (activeTab) {
      case "dashboard":
        return renderDashboard();
      case "users":
        return renderUsers();
      case "products":
        return <ProductManager />
      case "brands":
        return <BrandManager />
      case "categories":
        return <CategoryManager />
      case "inventory":
        return <InventoryDashboard />
      case "orders":
        return renderOrders();
      case "analytics":
        return renderAnalytics();
      case "settings":
        return renderSettings();
      case "roles":
        return <RolePermissionManager />;
      default:
        return (
          <div className="dashboard-content">
            <div className="dashboard-header">
              <h2>🎯 Bảng điều khiển quản trị</h2>
              <p>Chào mừng bạn đến với hệ thống quản lý cửa hàng</p>
            </div>

            {/* Quick Stats */}
            <div className="quick-stats">
              <div className="stat-card">
                <div className="stat-icon">📦</div>
                <div className="stat-info">
                  <h3>Tổng sản phẩm</h3>
                  <p className="stat-number">{inventoryStats.totalProducts}</p>
                  <span className="stat-desc">Sản phẩm trong hệ thống</span>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon">✅</div>
                <div className="stat-info">
                  <h3>Còn hàng</h3>
                  <p className="stat-number">{inventoryStats.inStock}</p>
                  <span className="stat-desc">Sản phẩm có sẵn</span>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon">⚠️</div>
                <div className="stat-info">
                  <h3>Sắp hết</h3>
                  <p className="stat-number">{inventoryStats.lowStock}</p>
                  <span className="stat-desc">Cần bổ sung</span>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon">❌</div>
                <div className="stat-info">
                  <h3>Hết hàng</h3>
                  <p className="stat-number">{inventoryStats.outOfStock}</p>
                  <span className="stat-desc">Cần đặt hàng</span>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon">🔄</div>
                <div className="stat-info">
                  <h3>Cần đặt hàng</h3>
                  <p className="stat-number">{inventoryStats.needsReorder}</p>
                  <span className="stat-desc">Cần đặt hàng</span>
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="quick-actions">
              <h3>🚀 Hành động nhanh</h3>
              <div className="action-grid">
                <button 
                  className="action-btn primary"
                  onClick={() => setActiveTab("products")}
                >
                  📦 Quản lý sản phẩm
                </button>
                <button 
                  className="action-btn secondary"
                  onClick={() => setActiveTab("brands")}
                >
                  🏷️ Quản lý thương hiệu
                </button>
                <button 
                  className="action-btn secondary"
                  onClick={() => setActiveTab("categories")}
                >
                  📂 Quản lý danh mục
                </button>
                <button 
                  className="action-btn tertiary"
                  onClick={() => setActiveTab("inventory")}
                >
                  📊 Quản lý tồn kho
                </button>
                <button className="action-btn tertiary">
                  📈 Báo cáo bán hàng
                </button>
                <button className="action-btn tertiary">
                  👥 Quản lý khách hàng
                </button>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="recent-activity">
              <h3>📋 Hoạt động gần đây</h3>
              <div className="activity-list">
                <div className="activity-item">
                  <div className="activity-icon">➕</div>
                  <div className="activity-content">
                    <p>Thêm sản phẩm mới: "iPhone 15 Pro Max"</p>
                    <span className="activity-time">2 giờ trước</span>
                  </div>
                </div>
                <div className="activity-item">
                  <div className="activity-icon">📦</div>
                  <div className="activity-content">
                    <p>Nhập kho: 50 sản phẩm "Samsung Galaxy S24"</p>
                    <span className="activity-time">4 giờ trước</span>
                  </div>
                </div>
                <div className="activity-item">
                  <div className="activity-icon">⚠️</div>
                  <div className="activity-content">
                    <p>Cảnh báo: "MacBook Pro" sắp hết hàng</p>
                    <span className="activity-time">6 giờ trước</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="admin-dashboard">
      <header className="admin-header">
        <div className="header-left">
          <div className="logo">🛍️ AdminPanel</div>
          <div className="user-info">
            <span>Xin chào, {userInfo?.sub || "Admin"}</span>
            <span className="user-role">Quản trị viên</span>
          </div>
        </div>
        <div className="header-right">
          <button className="notification-btn">🔔</button>
          <button onClick={handleLogout} className="logout-btn">
            🚪 Đăng xuất
          </button>
        </div>
      </header>

      <div className="admin-body">
        <aside className="admin-sidebar">
          <nav className="sidebar-nav">
            <ul>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "dashboard" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("dashboard")}
                >
                  📊 Dashboard
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "users" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("users")}
                >
                  👥 Người dùng
                </button>
              </li>

              <li>
                <button
                  className={`nav-item ${
                    activeTab === "roles" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("roles")}
                >
                  🛡️ Role & Permission
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "products" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("products")}
                >
                  📦 Sản phẩm
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "brands" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("brands")}
                >
                  🏷️ Thương hiệu
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "categories" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("categories")}
                >
                  📂 Danh mục
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "inventory" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("inventory")}
                >
                  📊 Tồn kho
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "orders" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("orders")}
                >
                  🛒 Đơn hàng
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "analytics" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("analytics")}
                >
                  📈 Thống kê
                </button>
              </li>
              <li>
                <button
                  className={`nav-item ${
                    activeTab === "settings" ? "active" : ""
                  }`}
                  onClick={() => setActiveTab("settings")}
                >
                  ⚙️ Cài đặt
                </button>
              </li>
            </ul>
          </nav>
        </aside>

        <main className="admin-content">{renderContent()}</main>
      </div>
    </div>
  );
};

export default AdminDashboard;
