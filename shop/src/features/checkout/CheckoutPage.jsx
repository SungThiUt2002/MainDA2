// 📁 src/features/checkout/CheckoutPage.jsx
import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { updateOrderInfo, confirmOrder } from "../../api/orderApi";
import "../../styles/Header.css";
import "./CheckoutPage.css";

const CheckoutPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Thông tin đơn hàng từ cart checkout
  const [orderData, setOrderData] = useState(null);
  
  // Form data
  const [formData, setFormData] = useState({
    receiverName: "",
    receiverPhone: "",
    province: "",
    district: "",
    ward: "",
    streetAddress: "",
    note: "",
    paymentMethod: "COD" // COD hoặc ONLINE
  });

  useEffect(() => {
    // Lấy thông tin đơn hàng từ location state (sau khi checkout cart)
    if (location.state?.orderData) {
      setOrderData(location.state.orderData);
    } else {
      // Nếu không có order data, redirect về cart
      alert("Không có thông tin đơn hàng. Vui lòng thực hiện checkout từ giỏ hàng.");
      navigate("/cart");
    }
  }, [location.state, navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!orderData) {
      alert("Không có thông tin đơn hàng!");
      return;
    }

    // Validate form
    if (!formData.receiverName || !formData.receiverPhone || !formData.streetAddress) {
      alert("Vui lòng điền đầy đủ thông tin giao hàng!");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        throw new Error("Vui lòng đăng nhập để tiếp tục!");
      }

      console.log("📝 Cập nhật thông tin đơn hàng...");
      
      // Cập nhật thông tin đơn hàng
      const orderInfo = {
        addressId: 1, // Tạm thời hardcode, sau này sẽ lấy từ profile service
        receiverName: formData.receiverName,
        receiverPhone: formData.receiverPhone,
        province: formData.province,
        district: formData.district,
        ward: formData.ward,
        streetAddress: formData.streetAddress,
        note: formData.note,
        paymentMethod: formData.paymentMethod
      };

      const updatedOrder = await updateOrderInfo(orderData.id, orderInfo, token);
      console.log("✅ Cập nhật thông tin đơn hàng thành công:", updatedOrder);

      // Xác nhận đơn hàng
      console.log("✅ Xác nhận đơn hàng...");
      await confirmOrder(orderData.id, token);
      
      // Hiển thị thông báo thành công
      alert("Đặt hàng thành công! Đơn hàng của bạn đã được tạo.");
      
      // Redirect về trang chủ
      navigate("/");
      
    } catch (error) {
      console.error("❌ Lỗi tạo đơn hàng:", error);
      const errorMessage = error.response?.data?.message || error.message || "Lỗi khi tạo đơn hàng";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const calculateTotal = () => {
    if (!orderData?.items) return 0;
    return orderData.items.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  if (!orderData) {
    return (
      <div className="checkout-page-wrapper">
        <div className="loading">
          <div className="spinner"></div>
          <p>Đang tải thông tin đơn hàng...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page-wrapper">
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
        <span onClick={() => navigate("/cart")} className="breadcrumb-link">Giỏ hàng</span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">Thanh toán</span>
      </nav>

      <div className="checkout-page">
        <div className="container">
          <h2 className="checkout-title">💳 Thanh toán đơn hàng</h2>

          {error && (
            <div className="error-message">
              <p>{error}</p>
            </div>
          )}

          <div className="checkout-content">
            {/* Form thông tin giao hàng */}
            <div className="checkout-form-section">
              <h3>📦 Thông tin giao hàng</h3>
              <form onSubmit={handleSubmit} className="checkout-form">
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="receiverName">Họ và tên người nhận *</label>
                    <input
                      type="text"
                      id="receiverName"
                      name="receiverName"
                      value={formData.receiverName}
                      onChange={handleInputChange}
                      required
                      placeholder="Nhập họ và tên"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="receiverPhone">Số điện thoại *</label>
                    <input
                      type="tel"
                      id="receiverPhone"
                      name="receiverPhone"
                      value={formData.receiverPhone}
                      onChange={handleInputChange}
                      required
                      placeholder="Nhập số điện thoại"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="province">Tỉnh/Thành phố</label>
                    <input
                      type="text"
                      id="province"
                      name="province"
                      value={formData.province}
                      onChange={handleInputChange}
                      placeholder="Nhập tỉnh/thành phố"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="district">Quận/Huyện</label>
                    <input
                      type="text"
                      id="district"
                      name="district"
                      value={formData.district}
                      onChange={handleInputChange}
                      placeholder="Nhập quận/huyện"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="ward">Phường/Xã</label>
                    <input
                      type="text"
                      id="ward"
                      name="ward"
                      value={formData.ward}
                      onChange={handleInputChange}
                      placeholder="Nhập phường/xã"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="streetAddress">Địa chỉ cụ thể *</label>
                    <input
                      type="text"
                      id="streetAddress"
                      name="streetAddress"
                      value={formData.streetAddress}
                      onChange={handleInputChange}
                      required
                      placeholder="Số nhà, tên đường, thôn/xóm..."
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="note">Ghi chú</label>
                  <textarea
                    id="note"
                    name="note"
                    value={formData.note}
                    onChange={handleInputChange}
                    placeholder="Ghi chú cho đơn hàng (không bắt buộc)"
                    rows="3"
                  />
                </div>

                <div className="form-group">
                  <label>Phương thức thanh toán</label>
                  <div className="payment-methods">
                    <label className="payment-method">
                      <input
                        type="radio"
                        name="paymentMethod"
                        value="COD"
                        checked={formData.paymentMethod === "COD"}
                        onChange={handleInputChange}
                      />
                      <span className="payment-label">
                        💰 Thanh toán khi nhận hàng (COD)
                      </span>
                    </label>
                    <label className="payment-method">
                      <input
                        type="radio"
                        name="paymentMethod"
                        value="ONLINE"
                        checked={formData.paymentMethod === "ONLINE"}
                        onChange={handleInputChange}
                      />
                      <span className="payment-label">
                        💳 Thanh toán online
                      </span>
                    </label>
                  </div>
                </div>

                <div className="form-actions">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => navigate("/cart")}
                    disabled={loading}
                  >
                    Quay lại giỏ hàng
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? "Đang xử lý..." : "Đặt hàng"}
                  </button>
                </div>
              </form>
            </div>

            {/* Tóm tắt đơn hàng */}
            <div className="order-summary">
              <h3>📋 Tóm tắt đơn hàng</h3>
              <div className="order-items">
                {orderData.items?.map((item, index) => (
                  <div key={index} className="order-item">
                    <div className="item-info">
                      <h4>{item.productName}</h4>
                      <p>Số lượng: {item.quantity}</p>
                    </div>
                    <div className="item-price">
                      {(item.price * item.quantity).toLocaleString('vi-VN')} ₫
                    </div>
                  </div>
                ))}
              </div>
              
              <div className="order-total">
                <div className="total-row">
                  <span>Tổng tiền hàng:</span>
                  <span>{calculateTotal().toLocaleString('vi-VN')} ₫</span>
                </div>
                <div className="total-row">
                  <span>Phí vận chuyển:</span>
                  <span>Miễn phí</span>
                </div>
                <div className="total-row final">
                  <span>Tổng cộng:</span>
                  <span>{calculateTotal().toLocaleString('vi-VN')} ₫</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage; 