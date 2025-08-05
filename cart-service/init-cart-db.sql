-- Tạo database cho cart-service
CREATE DATABASE IF NOT EXISTS cart_service_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cart_service_db;

-- Bảng carts: lưu thông tin giỏ hàng của từng user
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Khóa chính',
    user_id BIGINT NOT NULL COMMENT 'ID user (tham chiếu account-service)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái: ACTIVE, CHECKED_OUT, ABANDONED...',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng cart_items: lưu các sản phẩm trong giỏ hàng
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Khóa chính',
    cart_id BIGINT NOT NULL COMMENT 'FK carts(id)',
    product_variant_id BIGINT NOT NULL COMMENT 'ID biến thể sản phẩm (product-service)',
    sku VARCHAR(100) NOT NULL COMMENT 'SKU sản phẩm',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Số lượng',
    price DECIMAL(12,2) NOT NULL COMMENT 'Giá tại thời điểm thêm vào cart',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái: ACTIVE, REMOVED...',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian thêm vào cart',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
    UNIQUE KEY uq_cart_product (cart_id, product_variant_id),
    INDEX idx_cart_id (cart_id),
    CONSTRAINT fk_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ghi chú:
-- - Không dùng foreign key sang user hay product_variant để đảm bảo tách biệt microservice
-- - Khi cần lấy thông tin user hoặc sản phẩm, sẽ gọi API sang service tương ứng 