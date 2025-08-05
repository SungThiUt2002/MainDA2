import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { getUserOrders } from "../../api/orderApi";
import "./OrderHistoryPage.css";

const OrderHistoryPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setError("Chưa đăng nhập. Vui lòng đăng nhập lại.");
        navigate("/login");
        return;
      }

      // Lấy tất cả đơn hàng của user
      const userOrders = await getUserOrders(token);
      setOrders(userOrders || []);
    } catch (err) {
      console.error("❌ Lỗi lấy lịch sử đơn hàng:", err);
      setError("Không thể tải lịch sử đơn hàng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const getStatusText = (status) => {
    switch (status) {
      case 'PENDING':
        return 'Chờ xác nhận';
      case 'CONFIRMED':
        return 'Đã xác nhận';
      case 'SHIPPING':
        return 'Đang giao hàng';
      case 'DELIVERED':
        return 'Đã giao hàng';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status;
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING':
        return 'status-pending';
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'SHIPPING':
        return 'status-shipping';
      case 'DELIVERED':
        return 'status-delivered';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return 'status-default';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="order-history-page">
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
            <button className="btn btn-orders active" onClick={() => navigate("/orders")}>
              📋 Đơn hàng
            </button>
            <button className="btn btn-login" onClick={() => navigate("/login")}>
              Đăng nhập
            </button>
            <button className="btn btn-register" onClick={() => navigate("/register")}>
              Đăng ký
            </button>
          </div>
        </div>
      </header>

      {/* Breadcrumb */}
      <nav className="breadcrumb">
        <span onClick={() => navigate("/")} className="breadcrumb-link">Trang chủ</span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">Lịch sử đơn hàng</span>
      </nav>

      <div className="container">
        <div className="order-history-content">
          <h2 className="page-title">📋 Lịch sử đơn hàng</h2>

          {loading && (
            <div className="loading">
              <div className="spinner"></div>
              <p>Đang tải lịch sử đơn hàng...</p>
            </div>
          )}

          {error && (
            <div className="error-message">
              <p>{error}</p>
              <button className="btn btn-primary" onClick={fetchOrders}>
                Thử lại
              </button>
            </div>
          )}

          {!loading && !error && orders.length === 0 && (
            <div className="empty-orders">
              <div className="empty-orders-icon">📦</div>
              <h3>Chưa có đơn hàng nào</h3>
              <p>Bạn chưa có đơn hàng nào trong lịch sử</p>
              <button className="btn btn-primary" onClick={() => navigate("/")}>
                Bắt đầu mua sắm
              </button>
            </div>
          )}

          {!loading && !error && orders.length > 0 && (
            <div className="orders-list">
              {orders.map((order) => (
                <div key={order.id} className="order-card">
                  <div className="order-header">
                    <div className="order-info">
                      <h3>Đơn hàng #{order.id}</h3>
                      <p className="order-date">
                        Đặt hàng: {formatDate(order.createdAt)}
                      </p>
                    </div>
                    <div className="order-status">
                      <span className={`status-badge ${getStatusClass(order.status)}`}>
                        {getStatusText(order.status)}
                      </span>
                    </div>
                  </div>

                  <div className="order-details">
                    <div className="order-summary">
                      <div className="summary-item">
                        <span className="label">Tổng tiền:</span>
                        <span className="value">{order.totalAmount?.toLocaleString('vi-VN')} ₫</span>
                      </div>
                      <div className="summary-item">
                        <span className="label">Số sản phẩm:</span>
                        <span className="value">{order.items?.length || 0}</span>
                      </div>
                      {order.shippingAddress && (
                        <div className="summary-item">
                          <span className="label">Địa chỉ giao hàng:</span>
                          <span className="value">{order.shippingAddress}</span>
                        </div>
                      )}
                      {order.phoneNumber && (
                        <div className="summary-item">
                          <span className="label">Số điện thoại:</span>
                          <span className="value">{order.phoneNumber}</span>
                        </div>
                      )}
                    </div>

                    {order.items && order.items.length > 0 && (
                      <div className="order-items">
                        <h4>Sản phẩm đã đặt:</h4>
                        <div className="items-grid">
                          {order.items.map((item, index) => (
                            <div key={index} className="order-item">
                              <div className="item-details">
                                <h5>{item.productName}</h5>
                                <p>Số lượng: {item.quantity}</p>
                                <p>Giá: {item.price?.toLocaleString('vi-VN')} ₫</p>
                                <p>Tổng: {(item.price * item.quantity)?.toLocaleString('vi-VN')} ₫</p>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {order.note && (
                      <div className="order-note">
                        <h4>Ghi chú:</h4>
                        <p>{order.note}</p>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default OrderHistoryPage; 