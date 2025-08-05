-- Inventory Service Database Schema
-- PostgreSQL

-- Create database
CREATE DATABASE inventory_service_db;

-- Connect to database
\c inventory_service_db;

-- Create tables
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    total_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    sold_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 10,
    reorder_point INTEGER NOT NULL DEFAULT 5,
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_stock_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sale_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- RESERVE, RELEASE, SALE, RETURN, ADJUSTMENT
    quantity INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    reference VARCHAR(255),
    user_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

CREATE TABLE inventory_alerts (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL, -- LOW_STOCK, OUT_OF_STOCK, OVERSTOCK
    severity VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH
    message TEXT NOT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

-- Create indexes
CREATE INDEX idx_inventory_items_product_variant_id ON inventory_items(product_variant_id);
CREATE INDEX idx_inventory_items_sku ON inventory_items(sku);
CREATE INDEX idx_inventory_items_is_active ON inventory_items(is_active);
CREATE INDEX idx_inventory_items_is_available ON inventory_items(is_available);
CREATE INDEX idx_inventory_items_available_quantity ON inventory_items(available_quantity);

CREATE INDEX idx_inventory_transactions_item_id ON inventory_transactions(inventory_item_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);

CREATE INDEX idx_inventory_alerts_item_id ON inventory_alerts(inventory_item_id);
CREATE INDEX idx_inventory_alerts_type ON inventory_alerts(alert_type);
CREATE INDEX idx_inventory_alerts_is_resolved ON inventory_alerts(is_resolved);

-- Create constraints
ALTER TABLE inventory_items 
ADD CONSTRAINT chk_quantities_non_negative 
CHECK (total_quantity >= 0 AND reserved_quantity >= 0 AND sold_quantity >= 0 AND available_quantity >= 0);

ALTER TABLE inventory_items 
ADD CONSTRAINT chk_available_quantity_calculation 
CHECK (available_quantity = total_quantity - reserved_quantity - sold_quantity);

ALTER TABLE inventory_transactions 
ADD CONSTRAINT chk_quantity_positive 
CHECK (quantity > 0);

-- Create trigger to update available_quantity automatically
CREATE OR REPLACE FUNCTION update_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = NEW.total_quantity - NEW.reserved_quantity - NEW.sold_quantity;
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_available_quantity
    BEFORE UPDATE ON inventory_items
    FOR EACH ROW
    EXECUTE FUNCTION update_available_quantity();

-- Create trigger to log transactions
CREATE OR REPLACE FUNCTION log_inventory_transaction()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        -- Log quantity changes
        IF OLD.total_quantity != NEW.total_quantity THEN
            INSERT INTO inventory_transactions (
                inventory_item_id, transaction_type, quantity, 
                previous_quantity, new_quantity, reference, user_id
            ) VALUES (
                NEW.id, 'ADJUSTMENT', 
                ABS(NEW.total_quantity - OLD.total_quantity),
                OLD.total_quantity, NEW.total_quantity,
                'System adjustment', NULL
            );
        END IF;
        
        IF OLD.reserved_quantity != NEW.reserved_quantity THEN
            INSERT INTO inventory_transactions (
                inventory_item_id, transaction_type, quantity, 
                previous_quantity, new_quantity, reference, user_id
            ) VALUES (
                NEW.id, 
                CASE 
                    WHEN NEW.reserved_quantity > OLD.reserved_quantity THEN 'RESERVE'
                    ELSE 'RELEASE'
                END,
                ABS(NEW.reserved_quantity - OLD.reserved_quantity),
                OLD.reserved_quantity, NEW.reserved_quantity,
                'System transaction', NULL
            );
        END IF;
        
        IF OLD.sold_quantity != NEW.sold_quantity THEN
            INSERT INTO inventory_transactions (
                inventory_item_id, transaction_type, quantity, 
                previous_quantity, new_quantity, reference, user_id
            ) VALUES (
                NEW.id, 
                CASE 
                    WHEN NEW.sold_quantity > OLD.sold_quantity THEN 'SALE'
                    ELSE 'RETURN'
                END,
                ABS(NEW.sold_quantity - OLD.sold_quantity),
                OLD.sold_quantity, NEW.sold_quantity,
                'System transaction', NULL
            );
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_inventory_transaction
    AFTER UPDATE ON inventory_items
    FOR EACH ROW
    EXECUTE FUNCTION log_inventory_transaction();

-- Create trigger to generate low stock alerts
CREATE OR REPLACE FUNCTION check_low_stock_alert()
RETURNS TRIGGER AS $$
BEGIN
    -- Check for low stock alert
    IF NEW.available_quantity <= NEW.low_stock_threshold AND NEW.available_quantity > 0 THEN
        INSERT INTO inventory_alerts (
            inventory_item_id, alert_type, severity, message
        ) VALUES (
            NEW.id, 'LOW_STOCK', 'MEDIUM',
            'Low stock alert: Available quantity (' || NEW.available_quantity || ') is at or below threshold (' || NEW.low_stock_threshold || ')'
        );
    END IF;
    
    -- Check for out of stock alert
    IF NEW.available_quantity = 0 THEN
        INSERT INTO inventory_alerts (
            inventory_item_id, alert_type, severity, message
        ) VALUES (
            NEW.id, 'OUT_OF_STOCK', 'HIGH',
            'Out of stock alert: No available quantity for SKU ' || NEW.sku
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_low_stock_alert
    AFTER UPDATE ON inventory_items
    FOR EACH ROW
    EXECUTE FUNCTION check_low_stock_alert();

-- Insert sample data
INSERT INTO inventory_items (
    product_variant_id, sku, total_quantity, reserved_quantity, 
    sold_quantity, available_quantity, low_stock_threshold, reorder_point
) VALUES 
(1, 'TSHIRT-BLUE-M', 100, 0, 0, 100, 10, 5),
(2, 'TSHIRT-BLUE-L', 75, 0, 0, 75, 10, 5),
(3, 'JEANS-BLUE-32', 50, 0, 0, 50, 8, 3),
(4, 'JEANS-BLUE-34', 30, 0, 0, 30, 8, 3),
(5, 'SHOES-NIKE-42', 25, 0, 0, 25, 5, 2);

-- Create views for reporting
CREATE VIEW inventory_summary AS
SELECT 
    COUNT(*) as total_items,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_items,
    COUNT(CASE WHEN is_available = true THEN 1 END) as available_items,
    COUNT(CASE WHEN available_quantity = 0 THEN 1 END) as out_of_stock_items,
    COUNT(CASE WHEN available_quantity <= low_stock_threshold THEN 1 END) as low_stock_items,
    SUM(total_quantity) as total_stock,
    SUM(available_quantity) as available_stock,
    SUM(reserved_quantity) as reserved_stock,
    SUM(sold_quantity) as sold_stock
FROM inventory_items;

CREATE VIEW low_stock_report AS
SELECT 
    i.id,
    i.product_variant_id,
    i.sku,
    i.available_quantity,
    i.low_stock_threshold,
    i.reorder_point,
    i.last_stock_update
FROM inventory_items i
WHERE i.available_quantity <= i.low_stock_threshold 
AND i.is_active = true
ORDER BY i.available_quantity ASC;

-- Grant permissions (adjust as needed)
GRANT ALL PRIVILEGES ON DATABASE inventory_service_db TO inventory_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO inventory_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO inventory_user; 