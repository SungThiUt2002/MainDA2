# 📦 Inventory Service Entities

## 🗂️ **Các Entity đã tạo:**

### 1. **InventoryItem** - Quản lý tồn kho chính
**File:** `src/main/java/com/stu/inventory_service/entity/InventoryItem.java`

**Chức năng:**
- Lưu trữ thông tin tồn kho của từng product variant
- Quản lý số lượng: total, reserved, sold, available
- Cấu hình ngưỡng cảnh báo và điểm đặt hàng
- Tự động cập nhật available_quantity

**Business Methods:**
```java
// Cập nhật số lượng có sẵn
inventoryItem.updateAvailableQuantity();

// Kiểm tra trạng thái
boolean isLowStock = inventoryItem.isLowStock();
boolean isOutOfStock = inventoryItem.isOutOfStock();
boolean needsReorder = inventoryItem.needsReorder();
```

### 2. **InventoryTransaction** - Lịch sử giao dịch
**File:** `src/main/java/com/stu/inventory_service/entity/InventoryTransaction.java`

**Chức năng:**
- Ghi lại tất cả thay đổi về số lượng tồn kho
- Audit trail hoàn chỉnh
- Liên kết với InventoryItem

**Transaction Types:**
- `RESERVE` - Đặt hàng
- `RELEASE` - Hủy đặt hàng  
- `SALE` - Bán hàng
- `RETURN` - Trả hàng
- `ADJUSTMENT` - Điều chỉnh tồn kho

### 3. **InventoryAlert** - Hệ thống cảnh báo
**File:** `src/main/java/com/stu/inventory_service/entity/InventoryAlert.java`

**Chức năng:**
- Tự động tạo cảnh báo khi có vấn đề về tồn kho
- Quản lý mức độ nghiêm trọng
- Tracking trạng thái xử lý

**Alert Types:**
- `LOW_STOCK` - Hàng sắp hết
- `OUT_OF_STOCK` - Hết hàng
- `OVERSTOCK` - Quá nhiều hàng tồn kho

**Severity Levels:**
- `LOW` - Cảnh báo nhẹ
- `MEDIUM` - Cảnh báo trung bình
- `HIGH` - Cảnh báo cao (ưu tiên xử lý)

## 🔗 **Mối quan hệ giữa các Entity:**

```
InventoryItem (1) ←→ (N) InventoryTransaction
InventoryItem (1) ←→ (N) InventoryAlert
```

## ⚙️ **Cấu hình Database:**

**File:** `src/main/resources/application.properties`

```properties
# Development: Tự động tạo/xóa bảng
spring.jpa.hibernate.ddl-auto=create-drop

# Production: Chỉ validate schema
spring.jpa.hibernate.ddl-auto=validate
```

## 🚀 **Cách sử dụng:**

### 1. **Khởi tạo InventoryItem:**
```java
InventoryItem item = new InventoryItem();
item.setProductVariantId(1L);
item.setSku("TSHIRT-BLUE-M");
item.setTotalQuantity(100);
item.setLowStockThreshold(10);
item.setReorderPoint(5);
item.updateAvailableQuantity();
```

### 2. **Tạo Transaction:**
```java
InventoryTransaction transaction = new InventoryTransaction(
    inventoryItem,
    TransactionType.SALE,
    5,           // quantity
    15,          // previous_quantity
    20,          // new_quantity
    "ORDER-123", // reference
    1001L,       // user_id
    "Customer order"
);
```

### 3. **Tạo Alert:**
```java
InventoryAlert alert = new InventoryAlert(
    inventoryItem,
    AlertType.LOW_STOCK,
    Severity.MEDIUM,
    "Low stock alert: Available quantity (10) is at or below threshold (10)"
);
```

## 📊 **Database Schema tương ứng:**

Các entity này sẽ tự động tạo các bảng:
- `inventory_items`
- `inventory_transactions` 
- `inventory_alerts`

Với đầy đủ constraints, indexes và relationships như trong `database_schema.sql`.

## 🎯 **Lợi ích:**

1. **Code-First Development:** Entity classes = documentation sống
2. **Type Safety:** Enum cho transaction types và alert types
3. **Business Logic:** Methods tích hợp trong entities
4. **Audit Trail:** Tự động tracking mọi thay đổi
5. **Microservice Ready:** Không phụ thuộc database khác 