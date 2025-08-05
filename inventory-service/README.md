# Inventory Service

Microservice quản lý kho hàng cho hệ thống e-commerce.

## 🚀 **TÍNH NĂNG CHÍNH**

- **Quản lý kho hàng**: Theo dõi tổng số lượng, đã đặt, đã bán, có sẵn
- **Đặt hàng**: Reserve/Release stock cho cart và orders
- **Bán hàng**: Xác nhận bán và cập nhật số lượng
- **Trả hàng**: Xử lý return và hoàn trả stock
- **Cảnh báo**: Low stock alerts tự động
- **Event-driven**: Tích hợp với Kafka cho async communication
- **Circuit Breaker**: Xử lý lỗi khi gọi external services

## 🏗️ **KIẾN TRÚC**

```
inventory-service/
├── src/main/java/com/stu/inventory_service/
│   ├── InventoryServiceApplication.java     # Main application
│   ├── controller/                          # REST API endpoints
│   ├── service/                             # Business logic
│   ├── repository/                          # Data access
│   ├── entity/                              # Database entities
│   ├── dto/                                 # Data transfer objects
│   ├── event/                               # Event classes & publishers
│   ├── client/                              # External service clients
│   ├── config/                              # Configuration classes
│   └── exception/                           # Custom exceptions
├── src/main/resources/
│   └── application.yml                      # Application config
├── database_schema.sql                      # Database schema
├── docker-compose.yml                       # Docker services
├── Dockerfile                               # Container config
└── pom.xml                                  # Maven dependencies
```

## 🛠️ **CÔNG NGHỆ SỬ DỤNG**

- **Spring Boot 3.2**: Framework chính
- **Spring Data JPA**: Database access
- **PostgreSQL**: Database
- **Apache Kafka**: Event streaming
- **Resilience4j**: Circuit breaker pattern
- **Docker**: Containerization
- **Maven**: Build tool

## 📋 **YÊU CẦU HỆ THỐNG**

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 14+
- Apache Kafka 3.0+

## 🚀 **CÁCH CHẠY**

### **Bước 1: Khởi động Infrastructure**
```bash
# Chạy database và Kafka
docker-compose up -d
```

### **Bước 2: Tạo Database**
```bash
# Kết nối PostgreSQL
docker exec -it inventory-postgres psql -U inventory_user -d inventory_service_db

# Hoặc chạy schema file
psql -h localhost -p 5432 -U inventory_user -d inventory_service_db -f database_schema.sql
```

### **Bước 3: Build và Chạy Application**
```bash
# Build project
mvn clean install

# Chạy application
mvn spring-boot:run
```

### **Bước 4: Kiểm tra Health**
```bash
curl http://localhost:9007/api/inventory/health
```

## 📊 **API ENDPOINTS**

### **Inventory Management**
- `POST /api/inventory/items` - Tạo inventory item
- `GET /api/inventory/items/{id}` - Lấy inventory item
- `GET /api/inventory/items/product/{productVariantId}` - Lấy theo product
- `GET /api/inventory/items` - Lấy tất cả items

### **Stock Operations**
- `POST /api/inventory/items/{productVariantId}/reserve` - Đặt hàng
- `POST /api/inventory/items/{productVariantId}/release` - Hủy đặt hàng
- `POST /api/inventory/items/{productVariantId}/confirm-sale` - Xác nhận bán
- `POST /api/inventory/items/{productVariantId}/return` - Trả hàng
- `POST /api/inventory/items/{productVariantId}/adjust` - Điều chỉnh stock

### **Stock Queries**
- `GET /api/inventory/items/{productVariantId}/available?quantity=5` - Kiểm tra có sẵn
- `GET /api/inventory/items/{productVariantId}/quantity` - Lấy số lượng có sẵn

### **Reports**
- `GET /api/inventory/reports/low-stock` - Items sắp hết hàng
- `GET /api/inventory/reports/out-of-stock` - Items hết hàng
- `GET /api/inventory/reports/overstock` - Items quá nhiều

## 🔧 **CẤU HÌNH**

### **Application Properties**
```yaml
server:
  port: 9007

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_service_db
    username: inventory_user
    password: inventory_pass
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

kafka:
  bootstrap-servers: localhost:9092
  topic:
    stock-reserved: inventory.stock.reserved
    stock-updated: inventory.stock.updated
    low-stock-alert: inventory.low-stock.alert

resilience4j:
  circuitbreaker:
    instances:
      product-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

### **Environment Variables**
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=inventory_service_db
DB_USER=inventory_user
DB_PASS=inventory_pass

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service URLs
PRODUCT_SERVICE_URL=http://localhost:9001
```

## 📈 **MONITORING**

### **Health Checks**
- Application health: `GET /api/inventory/health`
- Database connectivity
- Kafka connectivity

### **Metrics**
- Stock levels
- Transaction rates
- Error rates
- Response times

### **Logs**
```bash
# View application logs
docker logs inventory-service

# View Kafka logs
docker logs inventory-kafka
```

## 🔄 **EVENT FLOW**

### **Stock Reserved Event**
```
Cart Service → Inventory Service → Reserve Stock → Publish Event → Order Service
```

### **Stock Updated Event**
```
Inventory Service → Update Stock → Publish Event → Product Service (update availability)
```

### **Low Stock Alert Event**
```
Inventory Service → Check Threshold → Publish Alert → Notification Service
```

## 🧪 **TESTING**

### **Unit Tests**
```bash
mvn test
```

### **Integration Tests**
```bash
mvn test -Dtest=*IntegrationTest
```

### **API Tests**
```bash
# Test reserve stock
curl -X POST http://localhost:9007/api/inventory/items/1/reserve \
  -H "Content-Type: application/json" \
  -d '{"quantity": 2, "reference": "cart-123", "userId": 1}'

# Test check availability
curl "http://localhost:9007/api/inventory/items/1/available?quantity=5"
```

## 🐛 **TROUBLESHOOTING**

### **Common Issues**

1. **Database Connection Failed**
   ```bash
   # Check if PostgreSQL is running
   docker ps | grep postgres
   
   # Check database logs
   docker logs inventory-postgres
   ```

2. **Kafka Connection Failed**
   ```bash
   # Check if Kafka is running
   docker ps | grep kafka
   
   # Check Kafka logs
   docker logs inventory-kafka
   ```

3. **Application Won't Start**
   ```bash
   # Check application logs
   docker logs inventory-service
   
   # Check port availability
   netstat -an | grep 9007
   ```

### **Debug Mode**
```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.stu=DEBUG"
```

## 📚 **TÀI LIỆU THAM KHẢO**

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Apache Kafka](https://kafka.apache.org/documentation/)
- [Resilience4j](https://resilience4j.readme.io/)
- [PostgreSQL](https://www.postgresql.org/docs/)

## 🤝 **CONTRIBUTING**

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 **LICENSE**

This project is licensed under the MIT License. 