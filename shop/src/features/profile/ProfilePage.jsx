import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getMyInfo, changePassword, updateProfile, logout } from "../../api/userApi";
import { getUserOrders, getUserOrdersByStatus, cancelOrder } from "../../api/orderApi";
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
  const [passwordValidation, setPasswordValidation] = useState({
    isValid: false,
    message: ""
  });
  const [showPasswords, setShowPasswords] = useState({
    currentPassword: false,
    newPassword: false,
    confirmPassword: false
  });
  
  // Order states
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [activeOrderTab, setActiveOrderTab] = useState("all");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderDetail, setShowOrderDetail] = useState(false);
  
  // Profile update states
  const [updateProfileData, setUpdateProfileData] = useState({
    firstName: "",
    lastName: "",
    phoneNumber: ""
  });
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      navigate("/login");
      return;
    }

    fetchUserInfo();
  }, [navigate]);

  // Load orders when orders tab is active
  useEffect(() => {
    if (activeTab === "orders") {
      fetchOrders(activeOrderTab);
    }
  }, [activeTab, activeOrderTab]);

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

  const validatePassword = (password) => {
    if (password.length < 8) {
      return { isValid: false, message: "Mật khẩu phải có ít nhất 8 ký tự" };
    }
    
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{8,}$/;
    if (!passwordRegex.test(password)) {
      return { isValid: false, message: "Mật khẩu phải chứa chữ hoa, chữ thường, số và ký tự đặc biệt" };
    }
    
    return { isValid: true, message: "Mật khẩu hợp lệ" };
  };

  const togglePasswordVisibility = (field) => {
    setShowPasswords(prev => ({
      ...prev,
      [field]: !prev[field]
    }));
  };

  // Order functions
  const fetchOrders = async (status = null) => {
    try {
      setLoadingOrders(true);
      const token = localStorage.getItem("accessToken");
      let response;
      
      if (status && status !== "all") {
        response = await getUserOrdersByStatus(status, token);
      } else {
        response = await getUserOrders(token);
      }
      
      setOrders(response);
    } catch (err) {
      console.error("❌ Lỗi lấy danh sách đơn hàng:", err);
    } finally {
      setLoadingOrders(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này?")) {
      try {
        const token = localStorage.getItem("accessToken");
        await cancelOrder(orderId, token);
        alert("✅ Hủy đơn hàng thành công!");
        fetchOrders(activeOrderTab); // Refresh danh sách với trạng thái hiện tại
      } catch (err) {
        console.error("❌ Lỗi hủy đơn hàng:", err);
        alert("❌ Hủy đơn hàng thất bại: " + (err.response?.data?.message || err.message));
      }
    }
  };

  const handleContinueOrder = (order) => {
    // Chuyển đến trang checkout để tiếp tục điền thông tin
    navigate("/checkout", { 
      state: { 
        orderData: order,
        isContinuing: true 
      } 
    });
  };

  // Handle view order details
  const handleViewOrder = (order) => {
    setSelectedOrder(order);
    setShowOrderDetail(true);
  };

  const getStatusDisplayName = (status) => {
    const statusMap = {
      'CREATED': 'Tạo mới',
      'CONFIRM_INFORMATION': 'Xác nhận thông tin',
      'CONFIRMED': 'Đã xác nhận',
      'PENDING_CONFIRMATION': 'Chờ xác nhận',
      'PENDING_PAYMENT': 'Chờ thanh toán',
      'DELIVERING': 'Đang giao hàng',
      'DELIVERY_SUCCESSFUL': 'Giao hàng thành công',
      'CANCELLED': 'Đã hủy',
      'RETURNED': 'Hoàn hàng',
      'PAYMENT_FAILED': 'Thanh toán thất bại'
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      'CREATED': '#6c757d',
      'CONFIRM_INFORMATION': '#17a2b8',
      'CONFIRMED': '#28a745',
      'PENDING_CONFIRMATION': '#ffc107',
      'PENDING_PAYMENT': '#fd7e14',
      'DELIVERING': '#007bff',
      'DELIVERY_SUCCESSFUL': '#28a745',
      'CANCELLED': '#dc3545',
      'RETURNED': '#6f42c1',
      'PAYMENT_FAILED': '#dc3545'
    };
    return colorMap[status] || '#6c757d';
  };

  const filteredOrders = orders.filter(order => {
    // Filter by status
    if (activeOrderTab !== "all" && order.status !== activeOrderTab) {
      return false;
    }
    
    // Filter by search term
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      return (
        order.id.toLowerCase().includes(searchLower) ||
        (order.receiverName && order.receiverName.toLowerCase().includes(searchLower)) ||
        order.items.some(item => item.productName.toLowerCase().includes(searchLower))
      );
    }
    
    return true;
  });

  // Thêm function để format địa chỉ giao hàng
  const formatShippingAddress = (order) => {
    const parts = [];
    if (order.streetAddress) parts.push(order.streetAddress);
    if (order.ward) parts.push(order.ward);
    if (order.district) parts.push(order.district);
    if (order.province) parts.push(order.province);
    return parts.join(', ');
  };

  // Thêm function để format phương thức thanh toán
  const formatPaymentMethod = (paymentMethod) => {
    const methodMap = {
      'COD': 'Thanh toán khi nhận hàng',
      'BANK_TRANSFER': 'Chuyển khoản ngân hàng',
      'VNPAY': 'Thanh toán qua VNPAY',
      'MOMO': 'Thanh toán qua MOMO',
      'PAYPAL': 'Thanh toán qua PayPal'
    };
    return methodMap[paymentMethod] || paymentMethod;
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    
    if (changePasswordData.newPassword !== changePasswordData.confirmPassword) {
      alert("Mật khẩu mới không khớp!");
      return;
    }

    if (!passwordValidation.isValid) {
      alert("Mật khẩu mới không hợp lệ: " + passwordValidation.message);
      return;
    }

    try {
      setIsChangingPassword(true);
      
      await changePassword({
        currentPassword: changePasswordData.currentPassword,
        newPassword: changePasswordData.newPassword,
        confirmPassword: changePasswordData.confirmPassword
      });
      
             alert("✅ Đổi mật khẩu thành công! Bạn sẽ được đăng xuất để bảo mật. Vui lòng đăng nhập lại với mật khẩu mới.");
       
       // Tự động logout sau khi đổi mật khẩu thành công
       setTimeout(() => {
         localStorage.removeItem("accessToken");
         localStorage.removeItem("refreshToken");
         navigate("/login");
       }, 2000);
    } catch (err) {
      console.error("❌ Lỗi đổi mật khẩu:", err);
      let errorMessage = "❌ Đổi mật khẩu thất bại!";
      
      if (err.response?.data?.message) {
        errorMessage = "❌ " + err.response.data.message;
      } else if (err.response?.data?.errors) {
        // Xử lý validation errors từ backend
        const validationErrors = err.response.data.errors;
        const errorDetails = Object.values(validationErrors).flat().join(", ");
        errorMessage = "❌ Lỗi validation: " + errorDetails;
      } else if (err.response?.status === 400) {
        errorMessage = "❌ Mật khẩu hiện tại không đúng hoặc mật khẩu mới không hợp lệ!";
      } else if (err.response?.status === 401) {
        errorMessage = "❌ Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
      } else if (err.message) {
        errorMessage = "❌ " + err.message;
      }
      
      alert(errorMessage);
    } finally {
      setIsChangingPassword(false);
    }
  };

  const handleLogout = async () => {
    try {
      // Gọi API logout từ backend
      await logout();
      
      // Xóa token khỏi localStorage
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      
      alert("✅ Đăng xuất thành công!");
      navigate("/");
    } catch (error) {
      console.error("❌ Lỗi đăng xuất:", error);
      
      // Ngay cả khi API logout thất bại, vẫn xóa token và chuyển về trang chủ
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      
      alert("⚠️ Đăng xuất thành công (có lỗi kết nối server)!");
      navigate("/");
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    
    try {
      setIsUpdatingProfile(true);
      
      // Validate required fields
      if (!updateProfileData.firstName?.trim() && !updateProfileData.lastName?.trim() && !updateProfileData.phoneNumber?.trim()) {
        alert("❌ Vui lòng nhập ít nhất một thông tin để cập nhật!");
        return;
      }
      
      // Validate phone number format if provided
      if (updateProfileData.phoneNumber?.trim() && !updateProfileData.phoneNumber.match(/^[+]?[0-9]{10,15}$/)) {
        alert("❌ Số điện thoại không đúng định dạng!");
        return;
      }
      
      await updateProfile(updateProfileData);
      
      // If no exception is thrown, the update was successful
      alert("✅ Cập nhật thông tin thành công!");
      // Refresh user info
      await fetchUserInfo();
      // Reset form
      setUpdateProfileData({
        firstName: "",
        lastName: "",
        phoneNumber: ""
      });
      // Switch back to profile tab
      setActiveTab("profile");
    } catch (err) {
      console.error("❌ Lỗi cập nhật thông tin:", err);
      
      let errorMessage = "❌ Có lỗi xảy ra khi cập nhật thông tin!";
      
      if (err.response?.data?.message) {
        errorMessage = "❌ " + err.response.data.message;
      } else if (err.response?.status === 400) {
        errorMessage = "❌ Dữ liệu không hợp lệ!";
      } else if (err.response?.status === 401) {
        errorMessage = "❌ Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
      } else if (err.message) {
        errorMessage = "❌ " + err.message;
      }
      
      alert(errorMessage);
    } finally {
      setIsUpdatingProfile(false);
    }
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
                      <button 
                        className="edit-profile-btn"
                        onClick={() => setActiveTab("editProfile")}
                      >
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
                  <li className={`sidebar-item ${activeTab === "editProfile" ? "active" : ""}`}>
                    <button 
                      className="sidebar-btn"
                      onClick={() => setActiveTab("editProfile")}
                    >
                      <span className="icon">✏️</span>
                      <span className="text">Sửa Hồ Sơ</span>
                    </button>
                  </li>
                  <li className={`sidebar-item ${activeTab === "changePassword" ? "active" : ""}`}>
                    <button 
                      className="sidebar-btn"
                      onClick={() => setActiveTab("changePassword")}
                    >
                      <span className="icon">🔒</span>
                      <span className="text">Đổi Mật Khẩu</span>
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
                    <button 
                      className="btn btn-secondary"
                      onClick={() => setActiveTab("changePassword")}
                    >
                      🔒 Đổi mật khẩu
                    </button>
                  </div>
                  <div className="content-body">
                    <div className="profile-info">
                      <div className="info-section">
                        <h3 className="section-title">Thông tin cá nhân</h3>
                        <div className="info-list">
                          <div className="info-row">
                            <span className="info-label">Tên đăng nhập:</span>
                            <span className="info-value">{userInfo?.username}</span>
                          </div>
                          <div className="info-row">
                            <span className="info-label">Email:</span>
                            <span className="info-value">{userInfo?.email}</span>
                          </div>
                          <div className="info-row">
                            <span className="info-label">Họ và tên:</span>
                            <span className="info-value">{userInfo?.firstName} {userInfo?.lastName}</span>
                          </div>
                          <div className="info-row">
                            <span className="info-label">Số điện thoại:</span>
                            <span className="info-value">{userInfo?.phoneNumber || "Chưa cập nhật"}</span>
                          </div>
                          <div className="info-row">
                            <span className="info-label">Ngày tạo:</span>
                            <span className="info-value">{new Date(userInfo?.createdAt).toLocaleDateString('vi-VN')}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {activeTab === "changePassword" && (
                <>
                  <div className="content-header">
                    <h2 className="content-title">Đổi Mật Khẩu</h2>
                  </div>
                  <div className="content-body">
                    <div className="profile-info">
                      <div className="info-section">
                        <h3 className="section-title">Bảo mật</h3>
                        <form onSubmit={handlePasswordChange} className="password-form">
                          <div className="form-group">
                            <label htmlFor="currentPassword" className="form-label">Mật khẩu hiện tại *</label>
                            <div className="password-input-container">
                              <input
                                type={showPasswords.currentPassword ? "text" : "password"}
                                id="currentPassword"
                                className="form-input"
                                value={changePasswordData.currentPassword}
                                onChange={(e) => setChangePasswordData(prev => ({
                                  ...prev,
                                  currentPassword: e.target.value
                                }))}
                                required
                              />
                              <button
                                type="button"
                                className="password-toggle-btn"
                                onClick={() => togglePasswordVisibility('currentPassword')}
                              >
                                {showPasswords.currentPassword ? "🙈" : "👁️"}
                              </button>
                            </div>
                          </div>

                          <div className="form-group">
                            <label htmlFor="newPassword" className="form-label">Mật khẩu mới *</label>
                            <div className="password-input-container">
                              <input
                                type={showPasswords.newPassword ? "text" : "password"}
                                id="newPassword"
                                className="form-input"
                                value={changePasswordData.newPassword}
                                onChange={(e) => {
                                  const newPassword = e.target.value;
                                  setChangePasswordData(prev => ({
                                    ...prev,
                                    newPassword: newPassword
                                  }));
                                  setPasswordValidation(validatePassword(newPassword));
                                }}
                                required
                              />
                              <button
                                type="button"
                                className="password-toggle-btn"
                                onClick={() => togglePasswordVisibility('newPassword')}
                              >
                                {showPasswords.newPassword ? "🙈" : "👁️"}
                              </button>
                            </div>
                                                         <small className={`form-help ${passwordValidation.isValid ? 'valid' : changePasswordData.newPassword ? 'invalid' : ''}`}>
                               {changePasswordData.newPassword ? passwordValidation.message : "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (!@#$%^&*()_+-=[]{}|;:,.<>?)"}
                             </small>
                          </div>

                          <div className="form-group">
                            <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu mới *</label>
                            <div className="password-input-container">
                              <input
                                type={showPasswords.confirmPassword ? "text" : "password"}
                                id="confirmPassword"
                                className="form-input"
                                value={changePasswordData.confirmPassword}
                                onChange={(e) => setChangePasswordData(prev => ({
                                  ...prev,
                                  confirmPassword: e.target.value
                                }))}
                                required
                              />
                              <button
                                type="button"
                                className="password-toggle-btn"
                                onClick={() => togglePasswordVisibility('confirmPassword')}
                              >
                                {showPasswords.confirmPassword ? "🙈" : "👁️"}
                              </button>
                            </div>
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

              {activeTab === "editProfile" && (
                <>
                  <div className="content-header">
                    <h2 className="content-title">Sửa Hồ Sơ</h2>
                  </div>
                  <div className="content-body">
                    <div className="profile-info">
                      <div className="info-section">
                        <h3 className="section-title">Cập nhật thông tin cá nhân</h3>
                        <form onSubmit={handleUpdateProfile} className="password-form">
                          <div className="form-group">
                            <label htmlFor="firstName" className="form-label">Họ</label>
                            <input
                              type="text"
                              id="firstName"
                              className="form-input"
                              value={updateProfileData.firstName}
                              onChange={(e) => setUpdateProfileData(prev => ({
                                ...prev,
                                firstName: e.target.value
                              }))}
                              placeholder={userInfo?.firstName || "Nhập họ"}
                            />
                          </div>

                          <div className="form-group">
                            <label htmlFor="lastName" className="form-label">Tên</label>
                            <input
                              type="text"
                              id="lastName"
                              className="form-input"
                              value={updateProfileData.lastName}
                              onChange={(e) => setUpdateProfileData(prev => ({
                                ...prev,
                                lastName: e.target.value
                              }))}
                              placeholder={userInfo?.lastName || "Nhập tên"}
                            />
                          </div>

                          <div className="form-group">
                            <label htmlFor="phoneNumber" className="form-label">Số điện thoại</label>
                            <input
                              type="tel"
                              id="phoneNumber"
                              className="form-input"
                              value={updateProfileData.phoneNumber}
                              onChange={(e) => setUpdateProfileData(prev => ({
                                ...prev,
                                phoneNumber: e.target.value
                              }))}
                              placeholder={userInfo?.phoneNumber || "Nhập số điện thoại"}
                            />
                            <small className="form-help">
                              Định dạng: +84xxxxxxxxx hoặc 0xxxxxxxxx (10-15 số)
                            </small>
                          </div>

                          <button 
                            type="submit" 
                            className="btn btn-primary"
                            disabled={isUpdatingProfile}
                          >
                            {isUpdatingProfile ? "⏳ Đang xử lý..." : "💾 Cập nhật thông tin"}
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
                       <button 
                         className={`order-tab ${activeOrderTab === "all" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("all")}
                       >
                         Tất cả
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "PENDING_PAYMENT" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("PENDING_PAYMENT")}
                       >
                         Chờ thanh toán
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "PENDING_CONFIRMATION" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("PENDING_CONFIRMATION")}
                       >
                         Chờ xác nhận
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "DELIVERING" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("DELIVERING")}
                       >
                         Đang giao hàng
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "DELIVERY_SUCCESSFUL" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("DELIVERY_SUCCESSFUL")}
                       >
                         Hoàn thành
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "CANCELLED" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("CANCELLED")}
                       >
                         Đã hủy
                       </button>
                       <button 
                         className={`order-tab ${activeOrderTab === "RETURNED" ? "active" : ""}`}
                         onClick={() => setActiveOrderTab("RETURNED")}
                       >
                         Hoàn hàng
                       </button>
                     </div>

                     {/* Search Bar */}
                     <div className="search-section">
                       <div className="search-bar">
                         <span className="search-icon">🔍</span>
                         <input 
                           type="text" 
                           placeholder="Tìm kiếm theo ID đơn hàng, tên người nhận hoặc tên sản phẩm"
                           className="search-input"
                           value={searchTerm}
                           onChange={(e) => setSearchTerm(e.target.value)}
                         />
                       </div>
                     </div>

                     {/* Orders List */}
                     {loadingOrders ? (
                       <div className="loading-orders">
                         <div className="spinner"></div>
                         <p>Đang tải danh sách đơn hàng...</p>
                       </div>
                     ) : filteredOrders.length > 0 ? (
                       <div className="orders-table">
                         <table>
                           <thead>
                             <tr>
                               <th>Mã đơn hàng</th>
                               <th>Sản phẩm</th>
                               <th>Tổng tiền</th>
                               <th>Phương thức thanh toán</th>
                               <th>Trạng thái</th>
                               <th>Ngày tạo</th>
                               <th>Hành động</th>
                             </tr>
                           </thead>
                           <tbody>
                             {filteredOrders.map((order) => (
                               <tr key={order.id}>
                                 <td className="order-id">{order.id}</td>
                                 <td className="order-items">
                                   <div className="items-preview">
                                     {order.items?.slice(0, 2).map((item, index) => (
                                       <div key={index} className="item-preview">
                                         {item.productName} x{item.quantity}
                                       </div>
                                     ))}
                                     {order.items?.length > 2 && (
                                       <div className="more-items">+{order.items.length - 2} sản phẩm khác</div>
                                     )}
                                   </div>
                                 </td>
                                 <td className="order-total">
                                   <strong>{new Intl.NumberFormat('vi-VN', {
                                     style: 'currency',
                                     currency: 'VND'
                                   }).format(order.totalAmount)}</strong>
                                 </td>
                                 <td className="payment-method">
                                   {order.paymentMethod ? formatPaymentMethod(order.paymentMethod) : "Chưa chọn"}
                                 </td>
                                 <td className="order-status">
                                   <span
                                     className="status-badge"
                                     style={{ backgroundColor: getStatusColor(order.status) }}
                                   >
                                     {getStatusDisplayName(order.status)}
                                   </span>
                                 </td>
                                 <td className="order-date">
                                   {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                                 </td>
                                 <td className="order-actions">
                                   <button
                                     className="action-btn view-btn"
                                     onClick={() => handleViewOrder(order)}
                                   >
                                     👁️ Xem chi tiết
                                   </button>
                                   {(order.status === 'CREATED' || order.status === 'CONFIRM_INFORMATION') && (
                                     <button
                                       className="action-btn continue-btn"
                                       onClick={() => handleContinueOrder(order)}
                                     >
                                       ✏️ Tiếp tục điền thông tin
                                     </button>
                                   )}
                                   {(order.status === 'PENDING_PAYMENT' || order.status === 'CREATED' || order.status === 'CONFIRM_INFORMATION' || order.status === 'PENDING_CONFIRMATION' || order.status === 'CONFIRMED') && (
          <button
            className="action-btn cancel-btn"
            onClick={() => handleCancelOrder(order.id)}
          >
            ❌ Hủy đơn hàng
          </button>
        )}
                                 </td>
                               </tr>
                             ))}
                           </tbody>
                         </table>
                       </div>
                     ) : (
                       <div className="empty-orders">
                         <div className="empty-icon">📋</div>
                         <h4>Chưa có đơn hàng</h4>
                         <p>Bạn chưa có đơn hàng nào. Hãy mua sắm để có đơn hàng đầu tiên!</p>
                         <button className="btn btn-primary" onClick={() => navigate("/")}>
                           Mua sắm ngay
                         </button>
                       </div>
                     )}
                   </div>
                 </>
               )}
            </div>
          </div>
        </div>
      </div>

      {/* Order Detail Modal */}
      {showOrderDetail && selectedOrder && (
        <div className="modal-overlay">
          <div className="order-detail-modal">
            <div className="modal-header">
              <h3>Chi tiết đơn hàng #{selectedOrder.id}</h3>
              <button
                className="close-btn"
                onClick={() => setShowOrderDetail(false)}
              >
                ✕
              </button>
            </div>
            
            <div className="order-detail-content">
              <div className="detail-section">
                <h4>📋 Thông tin đơn hàng</h4>
                <div className="detail-grid">
                  <div className="detail-item">
                    <label>Mã đơn hàng:</label>
                    <span>{selectedOrder.id}</span>
                  </div>
                  <div className="detail-item">
                    <label>Trạng thái:</label>
                    <span>
                      <span
                        className="status-badge"
                        style={{ backgroundColor: getStatusColor(selectedOrder.status) }}
                      >
                        {getStatusDisplayName(selectedOrder.status)}
                      </span>
                    </span>
                  </div>
                  <div className="detail-item">
                    <label>Tổng tiền:</label>
                    <span className="total-amount">
                      {new Intl.NumberFormat('vi-VN', {
                        style: 'currency',
                        currency: 'VND'
                      }).format(selectedOrder.totalAmount)}
                    </span>
                  </div>
                  <div className="detail-item">
                    <label>Phương thức thanh toán:</label>
                    <span>{selectedOrder.paymentMethod ? formatPaymentMethod(selectedOrder.paymentMethod) : "Chưa chọn"}</span>
                  </div>
                  <div className="detail-item">
                    <label>Ngày tạo:</label>
                    <span>{new Date(selectedOrder.createdAt).toLocaleDateString('vi-VN')}</span>
                  </div>
                  <div className="detail-item">
                    <label>Cập nhật lần cuối:</label>
                    <span>{new Date(selectedOrder.updatedAt).toLocaleDateString('vi-VN')}</span>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <h4>👤 Thông tin khách hàng</h4>
                <div className="detail-grid">
                  <div className="detail-item">
                    <label>Người nhận:</label>
                    <span>{selectedOrder.receiverName || "Chưa cập nhật"}</span>
                  </div>
                  <div className="detail-item">
                    <label>Số điện thoại:</label>
                    <span>{selectedOrder.receiverPhone || "Chưa cập nhật"}</span>
                  </div>
                  <div className="detail-item">
                    <label>Địa chỉ giao hàng:</label>
                    <span>
                      {selectedOrder.streetAddress && selectedOrder.ward && selectedOrder.district && selectedOrder.province
                        ? `${selectedOrder.streetAddress}, ${selectedOrder.ward}, ${selectedOrder.district}, ${selectedOrder.province}`
                        : "Chưa cập nhật"
                      }
                    </span>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <h4>📦 Sản phẩm đặt hàng</h4>
                <div className="order-items-detail">
                  {selectedOrder.items?.map((item, index) => (
                    <div key={index} className="order-item-detail">
                      <div className="item-info">
                        <div className="item-name">{item.productName}</div>
                        <div className="item-details">
                          <span>Giá: {new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                          }).format(item.productPrice)}</span>
                          <span>Số lượng: {item.quantity}</span>
                          <span>Tổng: {new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                          }).format(item.totalPrice)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {selectedOrder.note && (
                <div className="detail-section">
                  <h4>📝 Ghi chú</h4>
                  <p className="order-note">{selectedOrder.note}</p>
                </div>
              )}
            </div>

            <div className="modal-footer">
                      {(selectedOrder.status === 'CREATED' || selectedOrder.status === 'CONFIRM_INFORMATION') && (
                        <button
                          className="btn btn-primary"
                          onClick={() => {
                            handleContinueOrder(selectedOrder);
                            setShowOrderDetail(false);
                          }}
                        >
                          ✏️ Tiếp tục điền thông tin
                        </button>
                      )}
                      {(selectedOrder.status === 'PENDING_PAYMENT' || selectedOrder.status === 'CREATED' || selectedOrder.status === 'CONFIRM_INFORMATION' || selectedOrder.status === 'PENDING_CONFIRMATION' || selectedOrder.status === 'CONFIRMED') && (
          <button
            className="btn btn-danger"
            onClick={() => {
              handleCancelOrder(selectedOrder.id);
              setShowOrderDetail(false);
            }}
          >
            ❌ Hủy đơn hàng
          </button>
        )}
              <button
                className="btn btn-secondary"
                onClick={() => setShowOrderDetail(false)}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

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