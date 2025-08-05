# Stock Dashboard - Hướng dẫn sử dụng

## Tổng quan
`StockDashboard` là một component React được thiết kế để hiển thị thống kê tồn kho một cách trực quan và dễ hiểu. Dashboard này cung cấp các thông tin quan trọng về tình trạng tồn kho của cửa hàng.

## Các tính năng chính

### 1. Thống kê tổng quan
- **Còn hàng**: Số lượng sản phẩm có sẵn để bán
- **Sắp hết**: Sản phẩm có số lượng dưới ngưỡng cảnh báo
- **Hết hàng**: Sản phẩm không còn trong kho
- **Cần đặt hàng**: Sản phẩm cần được đặt hàng bổ sung

### 2. Tỷ lệ thống kê
- Tỷ lệ còn hàng (%)
- Tỷ lệ cần bổ sung (%)

### 3. Danh sách chi tiết
- Sản phẩm sắp hết hàng
- Sản phẩm cần đặt hàng
- Bảng tất cả sản phẩm trong kho

## Cách sử dụng

### Import component
```jsx
import StockDashboard from './StockDashboard';
```

### Sử dụng trong component
```jsx
function AdminPage() {
  return (
    <div>
      <StockDashboard />
    </div>
  );
}
```

### Sử dụng với routing
```jsx
import { Routes, Route } from 'react-router-dom';
import StockDashboard from './StockDashboard';

function App() {
  return (
    <Routes>
      <Route path="/admin/stock" element={<StockDashboard />} />
    </Routes>
  );
}
```

## API Requirements

Dashboard này sử dụng các API sau từ `inventoryApi.js`:

- `getLowStockItems()`: Lấy danh sách sản phẩm sắp hết hàng
- `getItemsNeedingReorder()`: Lấy danh sách sản phẩm cần đặt hàng
- `getAllInventoryItems()`: Lấy tất cả sản phẩm trong kho

## Cấu trúc dữ liệu

Dashboard mong đợi dữ liệu có cấu trúc như sau:

```javascript
{
  id: number,
  productId: number,
  productName: string,
  availableQuantity: number,
  soldQuantity: number,
  lowStockThreshold: number,
  reorderPoint: number,
  isLowStock: boolean,
  isOutOfStock: boolean,
  needsReorder: boolean
}
```

## Responsive Design

Dashboard được thiết kế responsive và hoạt động tốt trên:
- Desktop (>= 1024px)
- Tablet (768px - 1023px)
- Mobile (< 768px)

## Customization

### Thay đổi màu sắc
Các màu sắc có thể được tùy chỉnh trong file `StockDashboard.css`:

```css
.stat-card.in-stock {
  border-left-color: #059669; /* Màu xanh cho còn hàng */
}

.stat-card.low-stock {
  border-left-color: #f59e0b; /* Màu vàng cho sắp hết */
}

.stat-card.out-of-stock {
  border-left-color: #dc2626; /* Màu đỏ cho hết hàng */
}
```

### Thay đổi layout
Grid layout có thể được điều chỉnh:

```css
.stats-grid {
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}
```

## Troubleshooting

### Lỗi thường gặp

1. **Không hiển thị dữ liệu**
   - Kiểm tra kết nối API
   - Kiểm tra console để xem lỗi
   - Đảm bảo API trả về đúng format dữ liệu

2. **Lỗi CORS**
   - Kiểm tra cấu hình backend
   - Đảm bảo API endpoint đúng

3. **Layout bị vỡ**
   - Kiểm tra CSS đã được import
   - Kiểm tra responsive breakpoints

## Performance

- Sử dụng `useEffect` để fetch dữ liệu một lần khi component mount
- Sử dụng `Promise.all` để fetch nhiều API cùng lúc
- Có loading state để tránh hiển thị dữ liệu rỗng

## Future Enhancements

- Thêm biểu đồ thống kê
- Thêm filter và search
- Thêm export dữ liệu
- Thêm real-time updates
- Thêm notifications cho sản phẩm sắp hết hàng 