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
  const [filteredOrders, setFilteredOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("all");
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderDetail, setShowOrderDetail] = useState(false);
  const [confirmingOrder, setConfirmingOrder] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortOrder, setSortOrder] = useState("desc");

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

  // Filter and sort orders
  useEffect(() => {
    let filtered = [...orders];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(order => 
        order.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (order.receiverName && order.receiverName.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (order.receiverPhone && order.receiverPhone.includes(searchTerm)) ||
        (order.items && order.items.some(item => 
          item.productName.toLowerCase().includes(searchTerm.toLowerCase())
        ))
      );
    }

    // Sort orders
    filtered.sort((a, b) => {
      let aValue = a[sortBy];
      let bValue = b[sortBy];

      if (sortBy === "createdAt" || sortBy === "updatedAt") {
        aValue = new Date(aValue);
        bValue = new Date(bValue);
      }

      if (sortOrder === "asc") {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    setFilteredOrders(filtered);
  }, [orders, searchTerm, sortBy, sortOrder]);

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
        <div className="header-content">
          <h2>🛒 Quản lý đơn hàng</h2>
          <div className="order-stats">
            <span className="stat-item">
              <strong>{filteredOrders.length}</strong> đơn hàng
            </span>
            {selectedFilter !== "all" && (
              <span className="stat-item">
                <strong>{orders.length}</strong> tổng cộng
              </span>
            )}
          </div>
        </div>
        
        <div className="search-and-filters">
          <div className="search-container">
            <input
              type="text"
              placeholder="🔍 Tìm kiếm theo mã đơn, tên khách hàng, sản phẩm..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
          
          <div className="sort-container">
            <select 
              value={sortBy} 
              onChange={(e) => setSortBy(e.target.value)}
              className="sort-select"
            >
              <option value="createdAt">Sắp xếp theo ngày tạo</option>
              <option value="totalAmount">Sắp xếp theo tổng tiền</option>
              <option value="status">Sắp xếp theo trạng thái</option>
            </select>
            <button
              className={`sort-order-btn ${sortOrder === "desc" ? "desc" : "asc"}`}
              onClick={() => setSortOrder(sortOrder === "desc" ? "asc" : "desc")}
            >
              {sortOrder === "desc" ? "↓" : "↑"}
            </button>
          </div>
        </div>

        <div className="order-filters">
          <button
            className={`filter-btn ${selectedFilter === "all" ? "active" : ""}`}
            onClick={() => handleFilterChange("all")}
          >
            📋 Tất cả
          </button>
          <button
            className={`filter-btn ${selectedFilter === "pending" ? "active" : ""}`}
            onClick={() => handleFilterChange("pending")}
          >
            ⏳ Chờ xác nhận
          </button>
          <button
            className={`filter-btn ${selectedFilter === "confirmed" ? "active" : ""}`}
            onClick={() => handleFilterChange("confirmed")}
          >
            ✅ Đã xác nhận
          </button>
          <button
            className={`filter-btn ${selectedFilter === "delivering" ? "active" : ""}`}
            onClick={() => handleFilterChange("delivering")}
          >
            🚚 Đang giao
          </button>
          <button
            className={`filter-btn ${selectedFilter === "delivered" ? "active" : ""}`}
            onClick={() => handleFilterChange("delivered")}
          >
            🎉 Đã giao
          </button>
          <button
            className={`filter-btn ${selectedFilter === "cancelled" ? "active" : ""}`}
            onClick={() => handleFilterChange("cancelled")}
          >
            ❌ Đã hủy
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
          {filteredOrders.length === 0 ? (
            <div className="no-orders">
              <div className="no-orders-icon">📦</div>
              <h3>Không có đơn hàng nào</h3>
              <p>
                {searchTerm 
                  ? `Không tìm thấy đơn hàng nào với từ khóa "${searchTerm}"`
                  : "Chưa có đơn hàng nào trong danh sách"
                }
              </p>
              {searchTerm && (
                <button 
                  className="clear-search-btn"
                  onClick={() => setSearchTerm("")}
                >
                  Xóa bộ lọc tìm kiếm
                </button>
              )}
            </div>
          ) : (
            <div className="orders-grid">
              {filteredOrders.map((order) => (
                <div key={order.id} className="order-card">
                  <div className="order-card-header">
                    <div className="order-id-section">
                      <span className="order-id">#{order.id}</span>
                      <span className="order-date">{formatDate(order.createdAt)}</span>
                    </div>
                    <div className="order-status">
                      {getStatusBadge(order.status)}
                    </div>
                  </div>

                  <div className="order-card-body">
                    <div className="customer-section">
                      <div className="customer-avatar">👤</div>
                      <div className="customer-info">
                        <h4>{order.receiverName || "Chưa cập nhật"}</h4>
                        <p>{order.receiverPhone || "Chưa cập nhật"}</p>
                      </div>
                    </div>

                    <div className="order-items-section">
                      <h5>📦 Sản phẩm ({order.items?.length || 0})</h5>
                      <div className="items-preview">
                        {order.items?.slice(0, 3).map((item, index) => (
                          <div key={index} className="item-preview">
                            <span className="item-name">{item.productName}</span>
                            <span className="item-quantity">x{item.quantity}</span>
                          </div>
                        ))}
                        {order.items?.length > 3 && (
                          <div className="more-items">
                            +{order.items.length - 3} sản phẩm khác
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="order-summary">
                      <div className="summary-row">
                        <span>Phương thức thanh toán:</span>
                        <span>{order.paymentMethod ? getPaymentMethodLabel(order.paymentMethod) : "Chưa chọn"}</span>
                      </div>
                      <div className="summary-row total-row">
                        <span>Tổng tiền:</span>
                        <span className="total-amount">{formatCurrency(order.totalAmount)}</span>
                      </div>
                    </div>
                  </div>

                  <div className="order-card-footer">
                    <button
                      className="action-btn view-btn"
                      onClick={() => handleViewOrder(order.id)}
                    >
                      👁️ Chi tiết
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
                  </div>
                </div>
              ))}
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