# 📦 Tích hợp Quản lý Tồn kho

## 🎯 Tổng quan

Đã tích hợp thành công tính năng quản lý tồn kho vào frontend, kết nối với backend `inventory-service` để hiển thị thông tin tồn kho của từng sản phẩm.

## ✨ Tính năng đã thêm

### 1. **ProductManager với thông tin tồn kho**
- Hiển thị số lượng tồn kho của từng sản phẩm
- Trạng thái stock (Còn hàng, Sắp hết, Hết hàng, Cần đặt hàng)
- Modal chi tiết sản phẩm với thông tin tồn kho đầy đủ
- Màu sắc phân biệt trạng thái tồn kho

### 2. **InventoryDashboard**
- Dashboard tổng quan về tồn kho
- Thống kê: Tổng sản phẩm, Còn hàng, Sắp hết, Hết hàng
- Danh sách sản phẩm sắp hết hàng
- Danh sách sản phẩm cần đặt hàng
- Bảng tất cả sản phẩm trong kho

### 3. **API Integration**
- Kết nối với `inventory-service` (port 9007)
- Auto-refresh token với axios interceptor
- Error handling cho các API calls

## 🔧 Cấu hình

### Backend Services
```yaml
# inventory-service
Port: 9007
Base URL: http://localhost:9007/api/v1/inventory-items

# product-service  
Port: 9001
Base URL: http://localhost:9001/api/v1/products
```

### Frontend Configuration
```javascript
// shop/src/api/inventoryApi.js
const inventoryApi = createAxiosInstance({
  baseURL: 'http://localhost:9007/api/v1/inventory-items',
});
```

## 📊 Cấu trúc dữ liệu

### InventoryItemResponse
```json
{
  "id": 1,
  "productId": 123,
  "productName": "iPhone 15 Pro",
  "totalQuantity": 100,
  "lockedQuantity": 5,
  "soldQuantity": 45,
  "availableQuantity": 50,
  "lowStockThreshold": 10,
  "reorderPoint": 5,
  "isAvailable": true,
  "isActive": true,
  "isLowStock": false,
  "isOutOfStock": false,
  "needsReorder": false,
  "lastSaleDate": "2024-01-15T10:30:00",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 🎨 UI Components

### 1. ProductManager
- **Cột "Tồn kho"**: Hiển thị số lượng có sẵn và đã bán
- **Cột "Trạng thái"**: Badge với màu sắc phân biệt
- **Modal chi tiết**: Thông tin tồn kho đầy đủ

### 2. InventoryDashboard
- **Stats Cards**: 4 thẻ thống kê chính
- **Items Grid**: Hiển thị sản phẩm theo nhóm
- **Data Table**: Bảng tất cả sản phẩm

### 3. Color Coding
```css
/* Trạng thái tồn kho */
.in-stock: #059669 (Xanh lá)
.low-stock: #f59e0b (Vàng)
.needs-reorder: #d97706 (Cam)
.out-of-stock: #dc2626 (Đỏ)
.unknown: #6b7280 (Xám)
```

## 🚀 Cách sử dụng

### 1. Truy cập Admin Dashboard
```
http://localhost:3000/admin/dashboard
```

### 2. Quản lý sản phẩm với tồn kho
- Click tab "📦 Sản phẩm"
- Xem thông tin tồn kho trong bảng
- Click vào tên sản phẩm để xem chi tiết

### 3. Quản lý tồn kho
- Click tab "📊 Tồn kho"
- Xem tổng quan và thống kê
- Theo dõi sản phẩm sắp hết hàng

## 🔄 API Endpoints

### Inventory Service
```javascript
// Lấy thông tin tồn kho theo productId
GET /api/v1/inventory-items/product-variant/{productId}

// Lấy tất cả inventory items
GET /api/v1/inventory-items

// Lấy sản phẩm sắp hết hàng
GET /api/v1/inventory-items/low-stock

// Lấy sản phẩm cần đặt hàng
GET /api/v1/inventory-items/needing-reorder

// Lấy số lượng có sẵn
GET /api/v1/inventory-items/products/{productId}/available-quantity

// Lấy số lượng đã bán
GET /api/v1/inventory-items/products/{productId}/sold-quantity
```

## 🛠️ Development

### Thêm tính năng mới
1. Tạo API function trong `inventoryApi.js`
2. Tạo component trong `features/admin/`
3. Thêm CSS styles
4. Tích hợp vào AdminDashboard

### Error Handling
```javascript
try {
  const response = await getInventoryByProductId(productId);
  // Handle success
} catch (error) {
  console.error("Lỗi khi lấy thông tin tồn kho:", error);
  // Handle error
}
```

## 📝 Notes

- ✅ Tích hợp hoàn chỉnh với backend inventory-service
- ✅ Responsive design cho mobile
- ✅ Error handling và loading states
- ✅ Auto-refresh token
- ✅ Color coding cho trạng thái tồn kho
- 🔄 TODO: Thêm chức năng nhập kho từ frontend
- 🔄 TODO: Thêm biểu đồ thống kê tồn kho
- 🔄 TODO: Thêm export báo cáo tồn kho

## 🐛 Troubleshooting

### Lỗi kết nối API
1. Kiểm tra inventory-service có đang chạy không
2. Kiểm tra port 9007 có đúng không
3. Kiểm tra CORS configuration

### Không hiển thị thông tin tồn kho
1. Kiểm tra productId có khớp với inventory item không
2. Kiểm tra API response format
3. Kiểm tra console errors

### Token expired
- Hệ thống tự động refresh token
- Nếu vẫn lỗi, logout và login lại 