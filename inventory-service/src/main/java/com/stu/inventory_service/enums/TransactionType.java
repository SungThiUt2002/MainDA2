package com.stu.inventory_service.enums;

public enum TransactionType {
    CREATE, // tạo sản phẩm từ product service
    UPDATE, // update thông tin từ produc service
    DELETE, // xóa sản phẩm từ product service
    IMPORT_STOCK, // nhập kho
    RESERVE,    // Đặt hàng
    RELEASE,    // Hủy đặt hàng
    SALE,       // Bán hàng
    RETURN,     // Trả hàng
    ADJUSTMENT ,// Điều chỉnh tồn kho
    EXPORT // Giao hàng thành công

}
