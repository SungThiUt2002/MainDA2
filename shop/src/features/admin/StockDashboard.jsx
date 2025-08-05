import React, { useState, useEffect } from "react";
import { getLowStockItems, getItemsNeedingReorder, getAllInventoryItems } from "../../api/inventoryApi";
import "./StockDashboard.css";

const StockDashboard = () => {
  const [lowStockItems, setLowStockItems] = useState([]);
  const [needingReorderItems, setNeedingReorderItems] = useState([]);
  const [allInventoryItems, setAllInventoryItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    inStock: 0,
    lowStock: 0,
    outOfStock: 0,
    needsReorder: 0,
    totalItems: 0,
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
      const needsReorder = reorderRes.data.length;

      setStats({
        totalItems,
        outOfStock,
        lowStock,
        inStock,
        needsReorder,
      });
    } catch (error) {
      console.error("Lỗi khi tải dữ liệu tồn kho:", error);
    } finally {
      setLoading(false);
    }
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

  if (loading) {
    return <div className="stock-dashboard">Đang tải...</div>;
  }

  return (
    <div className="stock-dashboard">
      <div className="stock-header">
        <h2>📊 Thống kê tồn kho</h2>
        <div className="header-actions">
          <button className="refresh-btn" onClick={fetchInventoryData}>
            🔄 Làm mới
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card in-stock">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <h3>Còn hàng</h3>
            <p className="stat-number">{stats.inStock}</p>
            <span className="stat-desc">Sản phẩm có sẵn</span>
          </div>
        </div>

        <div className="stat-card low-stock">
          <div className="stat-icon">⚠️</div>
          <div className="stat-info">
            <h3>Sắp hết</h3>
            <p className="stat-number">{stats.lowStock}</p>
            <span className="stat-desc">Cần bổ sung</span>
          </div>
        </div>

        <div className="stat-card out-of-stock">
          <div className="stat-icon">❌</div>
          <div className="stat-info">
            <h3>Hết hàng</h3>
            <p className="stat-number">{stats.outOfStock}</p>
            <span className="stat-desc">Cần nhập hàng</span>
          </div>
        </div>

        <div className="stat-card needs-reorder">
          <div className="stat-icon">🔄</div>
          <div className="stat-info">
            <h3>Cần đặt hàng</h3>
            <p className="stat-number">{stats.needsReorder}</p>
            <span className="stat-desc">Cần đặt hàng</span>
          </div>
        </div>
      </div>

      {/* Summary Section */}
      <div className="summary-section">
        <div className="summary-card">
          <h3>📈 Tổng quan</h3>
          <div className="summary-stats">
            <div className="summary-item">
              <span className="label">Tổng sản phẩm:</span>
              <span className="value">{stats.totalItems}</span>
            </div>
            <div className="summary-item">
              <span className="label">Tỷ lệ còn hàng:</span>
              <span className="value">{stats.totalItems > 0 ? Math.round((stats.inStock / stats.totalItems) * 100) : 0}%</span>
            </div>
            <div className="summary-item">
              <span className="label">Tỷ lệ cần bổ sung:</span>
              <span className="value">{stats.totalItems > 0 ? Math.round(((stats.lowStock + stats.outOfStock) / stats.totalItems) * 100) : 0}%</span>
            </div>
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

export default StockDashboard; 