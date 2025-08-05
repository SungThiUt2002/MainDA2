import React, { useState, useEffect, useCallback } from "react";
import { 
  getAllOrders, 
  getPendingConfirmationOrders, 
  getOrdersByStatus, 
  getOrderById, 
  adminConfirmOrder 
} from "../../api/orderApi";
import "./OrderManager.css";

const OrderManager = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("all");
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderDetail, setShowOrderDetail] = useState(false);
  const [confirmingOrder, setConfirmingOrder] = useState(null);

  const token = localStorage.getItem("accessToken");

  // Fetch orders based on filter
  const fetchOrders = useCallback(async (filter = "all") => {
    setLoading(true);
    setError("");
    try {
      let response;
      switch (filter) {
        case "pending":
          response = await getPendingConfirmationOrders(token);
          break;
        case "confirmed":
          response = await getOrdersByStatus("CONFIRMED", token);
          break;
        case "delivering":
          response = await getOrdersByStatus("DELIVERING", token);
          break;
        case "delivered":
          response = await getOrdersByStatus("DELIVERY_SUCCESSFUL", token);
          break;
        case "cancelled":
          response = await getOrdersByStatus("CANCELLED", token);
          break;
        default:
          response = await getAllOrders(token);
      }
      setOrders(response || []);
    } catch (err) {
      console.error("❌ Lỗi lấy danh sách đơn hàng:", err);
      setError("Không thể tải danh sách đơn hàng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }, [token]);

  // Load orders when component mounts or filter changes
  useEffect(() => {
    fetchOrders(selectedFilter);
  }, [fetchOrders, selectedFilter]);

  // Handle filter change
  const handleFilterChange = (filter) => {
    setSelectedFilter(filter);
  };

  // Handle view order details
  const handleViewOrder = async (orderId) => {
    try {
      const orderDetail = await getOrderById(orderId, token);
      setSelectedOrder(orderDetail);
      setShowOrderDetail(true);
    } catch (err) {
      console.error("❌ Lỗi lấy chi tiết đơn hàng:", err);
      alert("Không thể lấy chi tiết đơn hàng");
    }
  };

  // Handle confirm order (COD)
  const handleConfirmOrder = async (orderId) => {
    if (!window.confirm("Bạn có chắc muốn xác nhận đơn hàng này?")) return;
    
    setConfirmingOrder(orderId);
    try {
      await adminConfirmOrder(orderId, token);
      alert("✅ Xác nhận đơn hàng thành công!");
      fetchOrders(selectedFilter); // Refresh list
    } catch (err) {
      console.error("❌ Lỗi xác nhận đơn hàng:", err);
      alert("❌ Xác nhận đơn hàng thất bại!");
    } finally {
      setConfirmingOrder(null);
    }
  };

  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  // Format date
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString("vi-VN");
  };

  // Get status badge
  const getStatusBadge = (status) => {
    const statusConfig = {
      CREATED: { label: "Đã tạo", className: "status-created" },
      PENDING_CONFIRMATION: { label: "Chờ xác nhận", className: "status-pending" },
      PENDING_PAYMENT: { label: "Chờ thanh toán", className: "status-pending" },
      CONFIRMED: { label: "Đã xác nhận", className: "status-confirmed" },
      DELIVERING: { label: "Đang giao", className: "status-delivering" },
      DELIVERY_SUCCESSFUL: { label: "Giao thành công", className: "status-delivered" },
      CANCELLED: { label: "Đã hủy", className: "status-cancelled" },
      PAYMENT_FAILED: { label: "Thanh toán thất bại", className: "status-failed" },
    };
    
    const config = statusConfig[status] || { label: status, className: "status-unknown" };
    return <span className={`status-badge ${config.className}`}>{config.label}</span>;
  };

  // Get payment method label
  const getPaymentMethodLabel = (method) => {
    const methodConfig = {
      COD: "Thanh toán khi nhận hàng",
      ONLINE: "Thanh toán online",
      BANK_TRANSFER: "Chuyển khoản ngân hàng",
    };
    return methodConfig[method] || method;
  };

  return (
    <div className="order-manager">
      <div className="order-manager-header">
        <h2>🛒 Quản lý đơn hàng</h2>
        <div className="order-filters">
          <button
            className={`filter-btn ${selectedFilter === "all" ? "active" : ""}`}
            onClick={() => handleFilterChange("all")}
          >
            Tất cả
          </button>
          <button
            className={`filter-btn ${selectedFilter === "pending" ? "active" : ""}`}
            onClick={() => handleFilterChange("pending")}
          >
            Chờ xác nhận
          </button>
          <button
            className={`filter-btn ${selectedFilter === "confirmed" ? "active" : ""}`}
            onClick={() => handleFilterChange("confirmed")}
          >
            Đã xác nhận
          </button>
          <button
            className={`filter-btn ${selectedFilter === "delivering" ? "active" : ""}`}
            onClick={() => handleFilterChange("delivering")}
          >
            Đang giao
          </button>
          <button
            className={`filter-btn ${selectedFilter === "delivered" ? "active" : ""}`}
            onClick={() => handleFilterChange("delivered")}
          >
            Đã giao
          </button>
          <button
            className={`filter-btn ${selectedFilter === "cancelled" ? "active" : ""}`}
            onClick={() => handleFilterChange("cancelled")}
          >
            Đã hủy
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-container">
          <p>Đang tải danh sách đơn hàng...</p>
        </div>
      ) : error ? (
        <div className="error-message">{error}</div>
      ) : (
        <div className="orders-container">
          {orders.length === 0 ? (
            <div className="no-orders">
              <p>Không có đơn hàng nào</p>
            </div>
          ) : (
            <div className="orders-table">
              <table>
                <thead>
                  <tr>
                    <th>Mã đơn hàng</th>
                    <th>Khách hàng</th>
                    <th>Sản phẩm</th>
                    <th>Tổng tiền</th>
                    <th>Phương thức thanh toán</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr key={order.id}>
                      <td className="order-id">{order.id}</td>
                      <td className="customer-info">
                        <div>
                          <strong>{order.receiverName || "Chưa cập nhật"}</strong>
                          <br />
                          <small>{order.receiverPhone || "Chưa cập nhật"}</small>
                        </div>
                      </td>
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
                        <strong>{formatCurrency(order.totalAmount)}</strong>
                      </td>
                      <td className="payment-method">
                        {order.paymentMethod ? getPaymentMethodLabel(order.paymentMethod) : "Chưa chọn"}
                      </td>
                      <td className="order-status">
                        {getStatusBadge(order.status)}
                      </td>
                      <td className="order-date">
                        {formatDate(order.createdAt)}
                      </td>
                      <td className="order-actions">
                        <button
                          className="action-btn view-btn"
                          onClick={() => handleViewOrder(order.id)}
                        >
                          👁️ Xem chi tiết
                        </button>
                        {order.status === "PENDING_CONFIRMATION" && (
                          <button
                            className="action-btn confirm-btn"
                            onClick={() => handleConfirmOrder(order.id)}
                            disabled={confirmingOrder === order.id}
                          >
                            {confirmingOrder === order.id ? "⏳ Đang xác nhận..." : "✅ Xác nhận"}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

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
                    <span>{getStatusBadge(selectedOrder.status)}</span>
                  </div>
                  <div className="detail-item">
                    <label>Tổng tiền:</label>
                    <span className="total-amount">{formatCurrency(selectedOrder.totalAmount)}</span>
                  </div>
                  <div className="detail-item">
                    <label>Phương thức thanh toán:</label>
                    <span>{selectedOrder.paymentMethod ? getPaymentMethodLabel(selectedOrder.paymentMethod) : "Chưa chọn"}</span>
                  </div>
                  <div className="detail-item">
                    <label>Ngày tạo:</label>
                    <span>{formatDate(selectedOrder.createdAt)}</span>
                  </div>
                  <div className="detail-item">
                    <label>Cập nhật lần cuối:</label>
                    <span>{formatDate(selectedOrder.updatedAt)}</span>
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
                          <span>Giá: {formatCurrency(item.productPrice)}</span>
                          <span>Số lượng: {item.quantity}</span>
                          <span>Tổng: {formatCurrency(item.totalPrice)}</span>
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
              {selectedOrder.status === "PENDING_CONFIRMATION" && (
                <button
                  className="confirm-btn large"
                  onClick={() => {
                    handleConfirmOrder(selectedOrder.id);
                    setShowOrderDetail(false);
                  }}
                  disabled={confirmingOrder === selectedOrder.id}
                >
                  {confirmingOrder === selectedOrder.id ? "⏳ Đang xác nhận..." : "✅ Xác nhận đơn hàng"}
                </button>
              )}
              <button
                className="close-btn secondary"
                onClick={() => setShowOrderDetail(false)}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderManager; 