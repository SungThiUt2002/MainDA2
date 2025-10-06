import React, { useState, useEffect, useCallback } from "react";
import { 
  getAllOrders, 
  getPendingConfirmationOrders, 
  getOrdersByStatus, 
  getOrderById, 
  adminConfirmOrder,
  updateShippingStatus,
  getOrderStatuses
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
  const [showStatusUpdateModal, setShowStatusUpdateModal] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(null);
  const [statusUpdateData, setStatusUpdateData] = useState({
    newStatus: "",
    description: ""
  });
  const [orderStatuses, setOrderStatuses] = useState([]);
  const [loadingStatuses, setLoadingStatuses] = useState(false);

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
        case "returned":
          response = await getOrdersByStatus("RETURNED", token);
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

  // Handle update shipping status
  const handleUpdateShippingStatus = async (orderId) => {
    if (!statusUpdateData.newStatus) {
      alert("Vui lòng chọn trạng thái mới!");
      return;
    }

    setUpdatingStatus(orderId);
    try {
      await updateShippingStatus(orderId, statusUpdateData, token);
      alert("✅ Cập nhật trạng thái thành công!");
      setShowStatusUpdateModal(false);
      setStatusUpdateData({ newStatus: "", description: "" });
      fetchOrders(selectedFilter); // Refresh list
    } catch (err) {
      console.error("❌ Lỗi cập nhật trạng thái:", err);
      alert("❌ Cập nhật trạng thái thất bại!");
    } finally {
      setUpdatingStatus(null);
    }
  };

  // Handle open status update modal
  const handleOpenStatusUpdateModal = (order) => {
    setSelectedOrder(order);
    setShowStatusUpdateModal(true);
  };

  // Load order statuses from backend
  const loadOrderStatuses = useCallback(async () => {
    setLoadingStatuses(true);
    try {
      const statuses = await getOrderStatuses(token);
      setOrderStatuses(statuses);
    } catch (err) {
      console.error("❌ Lỗi load danh sách trạng thái:", err);
    } finally {
      setLoadingStatuses(false);
    }
  }, [token]);

  // Get valid next statuses based on current status
  const getValidNextStatuses = (currentStatus) => {
    switch (currentStatus) {
      case "CONFIRMED":
        return ["DELIVERING"];
      case "DELIVERING":
        return ["DELIVERY_SUCCESSFUL", "RETURNED"]; // Có thể trả hàng trong quá trình giao
      case "DELIVERY_SUCCESSFUL":
        return ["RETURNED"]; // Có thể trả hàng sau khi giao thành công
      default:
        return [];
    }
  };

  // Load statuses when component mounts
  useEffect(() => {
    loadOrderStatuses();
  }, [loadOrderStatuses]);

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
      RETURNED: { label: "Đã trả lại", className: "status-returned" },
    };
    
    const config = statusConfig[status] || { label: status, className: "status-unknown" };
    return <span className={`status-badge ${config.className}`}>{config.label}</span>;
  };

  // Get status label
  const getStatusLabel = (status) => {
    const statusConfig = {
      CREATED: "Đã tạo",
      PENDING_CONFIRMATION: "Chờ xác nhận",
      PENDING_PAYMENT: "Chờ thanh toán",
      CONFIRMED: "Đã xác nhận",
      DELIVERING: "Đang giao",
      DELIVERY_SUCCESSFUL: "Giao thành công",
      CANCELLED: "Đã hủy",
      PAYMENT_FAILED: "Thanh toán thất bại",
      RETURNED: "Đã trả lại",
    };
    
    return statusConfig[status] || status;
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
          <button
            className={`filter-btn ${selectedFilter === "returned" ? "active" : ""}`}
            onClick={() => handleFilterChange("returned")}
          >
            Đã trả lại
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
                    <th className="stt-column">STT</th>
                    <th className="customer-column">Tên khách hàng</th>
                    <th className="product-column">Tên sản phẩm</th>
                    <th className="payment-column">Phương thức thanh toán</th>
                    <th className="total-column">Tổng tiền</th>
                    <th className="status-column">Trạng thái</th>
                    <th className="action-column">Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order, index) => (
                    <tr key={order.id} className="order-row">
                      <td className="stt-cell">
                        <span className="stt-number">{index + 1}</span>
                      </td>
                      <td className="customer-cell">
                        <div className="customer-info-card">
                          <div className="customer-avatar">
                            <span className="avatar-text">
                              {(order.receiverName || "K").charAt(0).toUpperCase()}
                            </span>
                          </div>
                          <div className="customer-details">
                            <div className="customer-name">
                              {order.receiverName || "Chưa cập nhật"}
                            </div>
                            <div className="customer-phone">
                              📞 {order.receiverPhone || "Chưa cập nhật"}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="product-cell">
                        <div className="products-list">
                          {order.items?.slice(0, 3).map((item, itemIndex) => (
                            <div key={itemIndex} className="product-item">
                              <div className="product-name">{item.productName}</div>
                              <div className="product-quantity">x{item.quantity}</div>
                            </div>
                          ))}
                          {order.items?.length > 3 && (
                            <div className="more-products">
                              +{order.items.length - 3} sản phẩm khác
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="payment-cell">
                        <div className="payment-method-card">
                          <div className="payment-icon">
                            {order.paymentMethod === "COD" ? "💰" : 
                             order.paymentMethod === "ONLINE" ? "💳" : 
                             order.paymentMethod === "BANK_TRANSFER" ? "🏦" : "❓"}
                          </div>
                          <div className="payment-text">
                            {order.paymentMethod ? getPaymentMethodLabel(order.paymentMethod) : "Chưa chọn"}
                          </div>
                        </div>
                      </td>
                      <td className="total-cell">
                        <div className="total-amount-card">
                          <div className="amount-value">
                            {formatCurrency(order.totalAmount)}
                          </div>
                          <div className="order-date">
                            📅 {new Date(order.createdAt).toLocaleDateString("vi-VN")}
                          </div>
                        </div>
                      </td>
                      <td className="status-cell">
                        {getStatusBadge(order.status)}
                      </td>
                      <td className="action-cell">
                        <div className="action-buttons">
                          <button
                            className="action-btn view-btn"
                            onClick={() => handleViewOrder(order.id)}
                            title="Xem chi tiết đơn hàng"
                          >
                            Xem chi tiết
                          </button>
                          {order.status === "PENDING_CONFIRMATION" && (
                            <button
                              className="action-btn confirm-btn"
                              onClick={() => handleConfirmOrder(order.id)}
                              disabled={confirmingOrder === order.id}
                              title="Xác nhận đơn hàng"
                            >
                              {confirmingOrder === order.id ? "⏳" : "✅"}
                            </button>
                          )}
                          {(order.status === "CONFIRMED" || order.status === "DELIVERING" || order.status === "DELIVERY_SUCCESSFUL") && 
                           getValidNextStatuses(order.status).length > 0 && (
                            <button
                              className="action-btn update-status-btn"
                              onClick={() => handleOpenStatusUpdateModal(order)}
                              title="Cập nhật trạng thái"
                            >
                              📦
                            </button>
                          )}
                        </div>
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

      {/* Status Update Modal */}
      {showStatusUpdateModal && selectedOrder && (
        <div className="modal-overlay">
          <div className="status-update-modal">
            <div className="modal-header">
              <h3>Cập nhật trạng thái đơn hàng #{selectedOrder.id}</h3>
              <button
                className="close-btn"
                onClick={() => {
                  setShowStatusUpdateModal(false);
                  setStatusUpdateData({ newStatus: "", description: "" });
                }}
              >
                ✕
              </button>
            </div>
            
            <div className="status-update-content">
              <div className="current-status">
                <h4>Trạng thái hiện tại: {getStatusBadge(selectedOrder.status)}</h4>
                <p>Đơn hàng #{selectedOrder.id}</p>
              </div>
              
              {selectedOrder.status === "DELIVERY_SUCCESSFUL" ? (
                <div className="return-notice">
                  <p><strong>⚠️ Lưu ý:</strong> Đơn hàng đã giao thành công. Chỉ chuyển sang "Trả lại" khi khách hàng yêu cầu trả lại hàng hoặc phát hiện lỗi sau khi nhận.</p>
                </div>
              ) : selectedOrder.status === "DELIVERING" ? (
                <div className="return-notice">
                  <p><strong>⚠️ Lưu ý:</strong> Đơn hàng đang trong quá trình giao. Có thể chuyển sang "Trả lại" nếu khách hàng từ chối nhận hàng hoặc không tìm thấy khách hàng.</p>
                </div>
              ) : null}
              
              <div className="status-form">
                <div className="form-group">
                  <label>Trạng thái mới:</label>
                  <select
                    value={statusUpdateData.newStatus}
                    onChange={(e) => setStatusUpdateData({
                      ...statusUpdateData,
                      newStatus: e.target.value
                    })}
                    disabled={loadingStatuses}
                  >
                    <option value="">Chọn trạng thái mới</option>
                    {getValidNextStatuses(selectedOrder.status).map((status) => (
                      <option key={status} value={status}>
                        {getStatusLabel(status)}
                      </option>
                    ))}
                  </select>
                  {loadingStatuses && (
                    <small style={{ color: '#6c757d', fontStyle: 'italic' }}>
                      Đang tải danh sách trạng thái...
                    </small>
                  )}
                  {!loadingStatuses && getValidNextStatuses(selectedOrder.status).length === 0 && (
                    <small style={{ color: '#dc3545', fontStyle: 'italic' }}>
                      Không có trạng thái hợp lệ để chuyển đổi từ trạng thái hiện tại
                    </small>
                  )}
                </div>

                <div className="form-group">
                  <label>Ghi chú (tùy chọn):</label>
                  <textarea
                    value={statusUpdateData.description}
                    onChange={(e) => setStatusUpdateData({
                      ...statusUpdateData,
                      description: e.target.value
                    })}
                    placeholder="Nhập ghi chú về trạng thái..."
                    rows="3"
                  />
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button
                className="update-btn"
                onClick={() => handleUpdateShippingStatus(selectedOrder.id)}
                disabled={updatingStatus === selectedOrder.id || !statusUpdateData.newStatus}
              >
                {updatingStatus === selectedOrder.id ? "⏳ Đang cập nhật..." : "✅ Cập nhật trạng thái"}
              </button>
              <button
                className="close-btn secondary"
                onClick={() => {
                  setShowStatusUpdateModal(false);
                  setStatusUpdateData({ newStatus: "", description: "" });
                }}
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderManager; 