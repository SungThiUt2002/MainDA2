import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getMyInfo, changePassword } from "../../api/userApi";
import "../../styles/Header.css";
import "./ProfilePage.css";

const ProfilePage = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("profile");
  const [changePasswordData, setChangePasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    fetchUserInfo();
  }, [navigate]);

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      const response = await getMyInfo();
      setUserInfo(response.data);
    } catch (err) {
      console.error("❌ Lỗi lấy thông tin người dùng:", err);
      setError("Không thể tải thông tin người dùng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    
    if (changePasswordData.newPassword !== changePasswordData.confirmPassword) {
      alert("Mật khẩu mới không khớp!");
      return;
    }

    if (changePasswordData.newPassword.length < 6) {
      alert("Mật khẩu mới phải có ít nhất 6 ký tự!");
      return;
    }

    try {
      setIsChangingPassword(true);
      await changePassword({
        currentPassword: changePasswordData.currentPassword,
        newPassword: changePasswordData.newPassword
      });
      
      alert("✅ Đổi mật khẩu thành công!");
      setChangePasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      });
    } catch (err) {
      console.error("❌ Lỗi đổi mật khẩu:", err);
      alert("❌ Đổi mật khẩu thất bại: " + (err.response?.data?.message || err.message));
    } finally {
      setIsChangingPassword(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    alert("Đăng xuất thành công!");
    navigate("/");
  };

  if (loading) {
    return (
      <div className="profile-page-wrapper">
        <div className="loading">
          <div className="spinner"></div>
          <p>Đang tải thông tin tài khoản...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profile-page-wrapper">
        <div className="error-message">
          <p>{error}</p>
          <button className="btn btn-primary" onClick={fetchUserInfo}>
            Thử lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page-wrapper">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="logo" onClick={() => navigate("/")}>
            <h1>🛍️ TechShop</h1>
          </div>
          
          <nav className="nav">
            <a href="/" className="nav-link">Trang chủ</a>
            <a href="/#products" className="nav-link">Sản phẩm</a>
            <a href="/#categories" className="nav-link">Danh mục</a>
            <a href="/#about" className="nav-link">Giới thiệu</a>
            <a href="/#contact" className="nav-link">Liên hệ</a>
          </nav>

          <div className="header-actions">
            <button className="btn btn-cart" onClick={() => navigate("/cart")}>
              🛒 Giỏ hàng
            </button>
            <button className="btn btn-logout" onClick={handleLogout}>
              🚪 Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <div className="profile-page">
        <div className="container">
          <div className="profile-content">
            {/* Sidebar Navigation */}
            <div className="sidebar">
              <div className="sidebar-section">
                {/* User Profile Header */}
                <div className="user-profile-header">
                  <div className="user-avatar">
                    <div className="avatar-circle">
                      {userInfo?.firstName?.charAt(0)?.toUpperCase() || "U"}
                    </div>
                  </div>
                  <div className="user-info">
                    <div className="username-section">
                      <span className="username">{userInfo?.username || "User"}</span>
                      <button className="edit-profile-btn">
                        <span className="edit-icon">✏️</span>
                        <span className="edit-text">Sửa Hồ Sơ</span>
                      </button>
                    </div>
                  </div>
                </div>

                {/* Navigation Menu */}
                <ul className="sidebar-menu">
                  <li className="sidebar-item">
                    <button className="sidebar-btn">
                      <span className="icon">🔔</span>
                      <span className="text">Thông Báo</span>
                    </button>
                  </li>
                  <li className={`sidebar-item ${activeTab === "profile" ? "active" : ""}`}>
                    <button 
                      className="sidebar-btn"
                      onClick={() => setActiveTab("profile")}
                    >
                      <span className="icon">👤</span>
                      <span className="text">Tài Khoản Của Tôi</span>
                    </button>
                  </li>
                  <li className={`sidebar-item ${activeTab === "orders" ? "active" : ""}`}>
                    <button 
                      className="sidebar-btn"
                      onClick={() => setActiveTab("orders")}
                    >
                      <span className="icon">📋</span>
                      <span className="text">Đơn Mua</span>
                    </button>
                  </li>
                </ul>
              </div>
            </div>

            {/* Main Content */}
            <div className="main-content">
              {activeTab === "profile" && (
                <>
                  <div className="content-header">
                    <h2 className="content-title">Tài Khoản Của Tôi</h2>
                  </div>
                  <div className="content-body">
                    <div className="profile-info">
                      <div className="info-section">
                        <h3 className="section-title">Thông tin cá nhân</h3>
                        <div className="info-grid">
                          <div className="info-item">
                            <label className="info-label">Tên đăng nhập</label>
                            <span className="info-value">{userInfo?.username}</span>
                          </div>
                          <div className="info-item">
                            <label className="info-label">Email</label>
                            <span className="info-value">{userInfo?.email}</span>
                          </div>
                          <div className="info-item">
                            <label className="info-label">Họ và tên</label>
                            <span className="info-value">{userInfo?.firstName} {userInfo?.lastName}</span>
                          </div>
                          <div className="info-item">
                            <label className="info-label">Số điện thoại</label>
                            <span className="info-value">{userInfo?.phoneNumber || "Chưa cập nhật"}</span>
                          </div>
                          <div className="info-item">
                            <label className="info-label">Vai trò</label>
                            <span className="info-value">{userInfo?.roles?.join(", ") || "USER"}</span>
                          </div>
                          <div className="info-item">
                            <label className="info-label">Ngày tạo</label>
                            <span className="info-value">{new Date(userInfo?.createdAt).toLocaleDateString('vi-VN')}</span>
                          </div>
                        </div>
                      </div>

                      <div className="info-section">
                        <h3 className="section-title">Bảo mật</h3>
                        <form onSubmit={handlePasswordChange} className="password-form">
                          <div className="form-group">
                            <label htmlFor="currentPassword" className="form-label">Mật khẩu hiện tại *</label>
                            <input
                              type="password"
                              id="currentPassword"
                              className="form-input"
                              value={changePasswordData.currentPassword}
                              onChange={(e) => setChangePasswordData(prev => ({
                                ...prev,
                                currentPassword: e.target.value
                              }))}
                              required
                            />
                          </div>

                          <div className="form-group">
                            <label htmlFor="newPassword" className="form-label">Mật khẩu mới *</label>
                            <input
                              type="password"
                              id="newPassword"
                              className="form-input"
                              value={changePasswordData.newPassword}
                              onChange={(e) => setChangePasswordData(prev => ({
                                ...prev,
                                newPassword: e.target.value
                              }))}
                              required
                            />
                          </div>

                          <div className="form-group">
                            <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu mới *</label>
                            <input
                              type="password"
                              id="confirmPassword"
                              className="form-input"
                              value={changePasswordData.confirmPassword}
                              onChange={(e) => setChangePasswordData(prev => ({
                                ...prev,
                                confirmPassword: e.target.value
                              }))}
                              required
                            />
                          </div>

                          <button 
                            type="submit" 
                            className="btn btn-primary"
                            disabled={isChangingPassword}
                          >
                            {isChangingPassword ? "⏳ Đang xử lý..." : "🔒 Đổi mật khẩu"}
                          </button>
                        </form>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {activeTab === "orders" && (
                <>
                  <div className="content-header">
                    <h2 className="content-title">Đơn Mua</h2>
                  </div>
                  <div className="content-body">
                    {/* Order Status Tabs */}
                    <div className="order-tabs">
                      <button className="order-tab active">Tất cả</button>
                      <button className="order-tab">Chờ thanh toán</button>
                      <button className="order-tab">Vận chuyển</button>
                      <button className="order-tab">Chờ giao hàng</button>
                      <button className="order-tab">Hoàn thành</button>
                      <button className="order-tab">Đã hủy</button>
                      <button className="order-tab">Trả hàng/Hoàn tiền</button>
                    </div>

                    {/* Search Bar */}
                    <div className="search-section">
                      <div className="search-bar">
                        <span className="search-icon">🔍</span>
                        <input 
                          type="text" 
                          placeholder="Bạn có thể tìm kiếm theo tên Shop, ID đơn hàng hoặc Tên Sản phẩm"
                          className="search-input"
                        />
                      </div>
                    </div>

                    {/* Empty Orders */}
                    <div className="empty-orders">
                      <div className="empty-icon">📋</div>
                      <h4>Chưa có đơn hàng</h4>
                      <p>Bạn chưa có đơn hàng nào. Hãy mua sắm để có đơn hàng đầu tiên!</p>
                      <button className="btn btn-primary" onClick={() => navigate("/")}>
                        Mua sắm ngay
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Chat Button */}
      <div className="chat-button">
        <button className="chat-btn">
          <span className="chat-icon">💬</span>
          <span className="chat-text">Chat</span>
        </button>
      </div>
    </div>
  );
};

export default ProfilePage; 