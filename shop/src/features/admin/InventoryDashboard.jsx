import React, { useState, useEffect } from "react";
import { getLowStockItems, getItemsNeedingReorder, getAllInventoryItems, importStock } from "../../api/inventoryApi";
import "./InventoryDashboard.css";

const InventoryDashboard = () => {
  const [lowStockItems, setLowStockItems] = useState([]);
  const [needingReorderItems, setNeedingReorderItems] = useState([]);
  const [allInventoryItems, setAllInventoryItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showImportModal, setShowImportModal] = useState(false);
  const [importForm, setImportForm] = useState({
    productId: "",
    productName: "",
    quantity: "",
    notes: ""
  });
  const [importLoading, setImportLoading] = useState(false);
  const [importError, setImportError] = useState("");
  const [importSuccess, setImportSuccess] = useState("");
  const [stats, setStats] = useState({
    totalItems: 0,
    outOfStock: 0,
    lowStock: 0,
    inStock: 0,
    totalValue: 0,
  });

  useEffect(() => {
    fetchInventoryData();
  }, []);

  const fetchInventoryData = async () => {
    try {
      setLoading(true);
      const [lowStockRes, reorderRes, allItemsRes] = await Promise.all([
        getLowStockItems(),
        getItemsNeedingReorder(),
        getAllInventoryItems(),
      ]);

      setLowStockItems(lowStockRes.data || []);
      setNeedingReorderItems(reorderRes.data || []);
      setAllInventoryItems(allItemsRes.data || []);

      // Tính toán thống kê
      const totalItems = allItemsRes.data.length;
      const outOfStock = allItemsRes.data.filter(item => item.isOutOfStock).length;
      const lowStock = allItemsRes.data.filter(item => item.isLowStock).length;
      const inStock = allItemsRes.data.filter(item => !item.isOutOfStock && !item.isLowStock).length;

      setStats({
        totalItems,
        outOfStock,
        lowStock,
        inStock,
        totalValue: 0, // TODO: Tính tổng giá trị tồn kho
      });
    } catch (error) {
      console.error("Lỗi khi tải dữ liệu tồn kho:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleImportStock = async (e) => {
    e.preventDefault();
    
    if (!importForm.productId || !importForm.quantity) {
      setImportError("Vui lòng chọn sản phẩm và nhập số lượng");
      return;
    }

    try {
      setImportLoading(true);
      setImportError("");
      setImportSuccess("");

      // Debug token trước khi lấy
      debugToken();

      // Thử lấy token từ nhiều nguồn khác nhau
      let token = localStorage.getItem("accessToken");
      if (!token) {
        token = sessionStorage.getItem("accessToken");
      }
      
      console.log('Token from storage:', token ? `Token length: ${token.length}` : 'No token found');
      console.log('Token preview:', token ? token.substring(0, 20) + '...' : 'No token');
      
      // Debug: Hiển thị tất cả keys trong localStorage
      console.log('All localStorage keys:', Object.keys(localStorage));
      console.log('All sessionStorage keys:', Object.keys(sessionStorage));
      
      if (!token) {
        setImportError("Vui lòng đăng nhập để thực hiện nhập kho");
        return;
      }

      const stockData = {
        quantity: parseInt(importForm.quantity),
        reason: importForm.notes || "Nhập kho từ Admin"
      };

      console.log('Sending import stock request:', {
        productId: importForm.productId,
        stockData,
        hasToken: !!token,
        tokenLength: token.length,
        tokenStart: token.substring(0, 20)
      });

      await importStock(parseInt(importForm.productId), stockData, token);
      
      setImportSuccess("Nhập kho thành công!");
      setImportForm({
        productId: "",
        productName: "",
        quantity: "",
        notes: ""
      });
      
      // Refresh data sau khi nhập kho
      setTimeout(() => {
        fetchInventoryData();
        setShowImportModal(false);
        setImportSuccess("");
      }, 1500);

    } catch (error) {
      console.error("Lỗi khi nhập kho:", error);
      console.error("Error details:", {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        statusText: error.response?.statusText
      });
      
      // Hiển thị lỗi chi tiết hơn
      let errorMessage = "Có lỗi xảy ra khi nhập kho";
      if (error.response?.status === 401) {
        errorMessage = "Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.";
      } else if (error.response?.status === 403) {
        errorMessage = "Bạn không có quyền thực hiện thao tác này.";
      } else if (error.response?.status === 404) {
        errorMessage = "Sản phẩm không tồn tại trong hệ thống.";
      } else if (error.response?.status === 500) {
        errorMessage = "Lỗi server. Vui lòng thử lại sau.";
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      
      setImportError(errorMessage);
    } finally {
      setImportLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setImportForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleProductSelect = (e) => {
    const selectedProductId = e.target.value;
    const selectedProduct = allInventoryItems.find(item => item.productId.toString() === selectedProductId);
    
    setImportForm(prev => ({
      ...prev,
      productId: selectedProductId,
      productName: selectedProduct ? selectedProduct.productName : ""
    }));
  };

  const debugToken = () => {
    const token = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");
    
    console.log('=== TOKEN DEBUG ===');
    console.log('Token exists:', !!token);
    if (token) {
      console.log('Token length:', token.length);
      console.log('Token preview:', token.substring(0, 50) + '...');
      console.log('Token format:', token.split('.').length === 3 ? 'Valid JWT format' : 'Invalid format');
      
      // Decode JWT token để kiểm tra
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('Token payload:', payload);
        console.log('Token expires at:', new Date(payload.exp * 1000));
        console.log('Token is expired:', Date.now() > payload.exp * 1000);
      } catch (error) {
        console.log('Cannot decode token:', error.message);
      }
    }
    console.log('localStorage keys:', Object.keys(localStorage));
    console.log('sessionStorage keys:', Object.keys(sessionStorage));
    console.log('==================');
  };

  const getStockStatusColor = (item) => {
    if (item.isOutOfStock) return "#dc2626";
    if (item.isLowStock) return "#f59e0b";
    if (item.needsReorder) return "#d97706";
    return "#059669";
  };

  const getStockStatusText = (item) => {
    if (item.isOutOfStock) return "Hết hàng";
    if (item.isLowStock) return "Sắp hết";
    if (item.needsReorder) return "Cần đặt hàng";
    return "Còn hàng";
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  if (loading) {
    return <div className="inventory-dashboard">Đang tải...</div>;
  }

  return (
    <div className="inventory-dashboard">
      <div className="inventory-header">
        <h2>📦 Quản lý tồn kho</h2>
        <div className="header-actions">
          <button className="import-btn" onClick={() => setShowImportModal(true)}>
            📥 Nhập kho
          </button>
        <button className="refresh-btn" onClick={fetchInventoryData}>
          🔄 Làm mới
        </button>
      </div>
      </div>

      {/* Import Stock Modal */}
      {showImportModal && (
        <div className="modal-overlay">
          <div className="import-modal">
            <div className="modal-header">
              <h3>📥 Nhập kho</h3>
              <button 
                className="close-btn" 
                onClick={() => {
                  setShowImportModal(false);
                  setImportError("");
                  setImportSuccess("");
                }}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleImportStock} className="import-form">
              <div className="form-group">
                <label htmlFor="productId">Chọn sản phẩm *</label>
                <select
                  id="productId"
                  name="productId"
                  value={importForm.productId}
                  onChange={handleProductSelect}
                  required
                  className="product-select"
                >
                  <option value="">-- Chọn sản phẩm --</option>
                  {allInventoryItems.map((item) => (
                    <option key={item.productId} value={item.productId}>
                      {item.productName} (ID: {item.productId}) - Còn: {item.availableQuantity}
                    </option>
                  ))}
                </select>
                {importForm.productName && (
                  <div className="selected-product-info">
                    <strong>Đã chọn:</strong> {importForm.productName}
                  </div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="quantity">Số lượng nhập *</label>
                <input
                  type="number"
                  id="quantity"
                  name="quantity"
                  value={importForm.quantity}
                  onChange={handleInputChange}
                  placeholder="Nhập số lượng"
                  min="1"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="notes">Ghi chú</label>
                <textarea
                  id="notes"
                  name="notes"
                  value={importForm.notes}
                  onChange={handleInputChange}
                  placeholder="Ghi chú về lô hàng nhập (tùy chọn)"
                  rows="3"
                />
              </div>

              {importError && (
                <div className="error-message">
                  ❌ {importError}
                </div>
              )}

              {importSuccess && (
                <div className="success-message">
                  ✅ {importSuccess}
                </div>
              )}

              <div className="modal-actions">
                <button 
                  type="button" 
                  className="debug-btn"
                  onClick={debugToken}
                  disabled={importLoading}
                >
                  🔍 Debug Token
                </button>
                <button 
                  type="button" 
                  className="cancel-btn"
                  onClick={() => {
                    setShowImportModal(false);
                    setImportError("");
                    setImportSuccess("");
                  }}
                  disabled={importLoading}
                >
                  Hủy
                </button>
                <button 
                  type="submit" 
                  className="submit-btn"
                  disabled={importLoading}
                >
                  {importLoading ? "Đang nhập..." : "Nhập kho"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📦</div>
          <div className="stat-info">
            <h3>Tổng sản phẩm</h3>
            <p className="stat-number">{stats.totalItems}</p>
            <span className="stat-desc">Sản phẩm trong kho</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ backgroundColor: "#059669" }}>✅</div>
          <div className="stat-info">
            <h3>Còn hàng</h3>
            <p className="stat-number">{stats.inStock}</p>
            <span className="stat-desc">Sản phẩm có sẵn</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ backgroundColor: "#f59e0b" }}>⚠️</div>
          <div className="stat-info">
            <h3>Sắp hết hàng</h3>
            <p className="stat-number">{stats.lowStock}</p>
            <span className="stat-desc">Cần bổ sung</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ backgroundColor: "#dc2626" }}>❌</div>
          <div className="stat-info">
            <h3>Hết hàng</h3>
            <p className="stat-number">{stats.outOfStock}</p>
            <span className="stat-desc">Cần nhập hàng</span>
          </div>
        </div>
      </div>

      {/* Low Stock Items */}
      <div className="inventory-section">
        <h3>⚠️ Sản phẩm sắp hết hàng ({lowStockItems.length})</h3>
        {lowStockItems.length > 0 ? (
          <div className="items-grid">
            {lowStockItems.slice(0, 6).map((item) => (
              <div key={item.id} className="item-card low-stock">
                <div className="item-header">
                  <h4>{item.productName}</h4>
                  <span 
                    className="status-badge"
                    style={{ 
                      backgroundColor: getStockStatusColor(item) + '20',
                      color: getStockStatusColor(item)
                    }}
                  >
                    {getStockStatusText(item)}
                  </span>
                </div>
                <div className="item-details">
                  <div className="stock-info">
                    <span>Còn lại: <strong>{item.availableQuantity}</strong></span>
                    <span>Đã bán: {item.soldQuantity}</span>
                    <span>Ngưỡng: {item.lowStockThreshold}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="no-items">Không có sản phẩm nào sắp hết hàng</p>
        )}
      </div>

      {/* Items Needing Reorder */}
      <div className="inventory-section">
        <h3>🔄 Sản phẩm cần đặt hàng ({needingReorderItems.length})</h3>
        {needingReorderItems.length > 0 ? (
          <div className="items-grid">
            {needingReorderItems.slice(0, 6).map((item) => (
              <div key={item.id} className="item-card needs-reorder">
                <div className="item-header">
                  <h4>{item.productName}</h4>
                  <span 
                    className="status-badge"
                    style={{ 
                      backgroundColor: getStockStatusColor(item) + '20',
                      color: getStockStatusColor(item)
                    }}
                  >
                    {getStockStatusText(item)}
                  </span>
                </div>
                <div className="item-details">
                  <div className="stock-info">
                    <span>Còn lại: <strong>{item.availableQuantity}</strong></span>
                    <span>Điểm đặt hàng: {item.reorderPoint}</span>
                    <span>Đã bán: {item.soldQuantity}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="no-items">Không có sản phẩm nào cần đặt hàng</p>
        )}
      </div>

      {/* All Inventory Items Table */}
      <div className="inventory-section">
        <h3>📋 Tất cả sản phẩm trong kho</h3>
        <div className="table-container">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Tồn kho</th>
                <th>Đã bán</th>
                <th>Trạng thái</th>
                <th>Ngưỡng cảnh báo</th>
              </tr>
            </thead>
            <tbody>
              {allInventoryItems.slice(0, 10).map((item) => (
                <tr key={item.id}>
                  <td>
                    <div className="product-info">
                      <strong>{item.productName}</strong>
                      <small>ID: {item.productId}</small>
                    </div>
                  </td>
                  <td>
                    <span className="quantity">{item.availableQuantity}</span>
                  </td>
                  <td>
                    <span className="sold">{item.soldQuantity}</span>
                  </td>
                  <td>
                    <span 
                      className="status-badge"
                      style={{ 
                        backgroundColor: getStockStatusColor(item) + '20',
                        color: getStockStatusColor(item)
                      }}
                    >
                      {getStockStatusText(item)}
                    </span>
                  </td>
                  <td>
                    <span className="threshold">{item.lowStockThreshold}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {allInventoryItems.length > 10 && (
          <p className="show-more">Hiển thị 10 sản phẩm đầu tiên. Tổng cộng {allInventoryItems.length} sản phẩm.</p>
        )}
      </div>
    </div>
  );
};

export default InventoryDashboard; 