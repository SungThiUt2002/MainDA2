# Thay đổi Bảo mật - Hệ thống Role

## Vấn đề bảo mật đã được khắc phục

### Vấn đề cũ:
- Người dùng có thể tự gán role cho mình khi đăng ký
- Có thể tạo tài khoản với role ADMIN ngay từ đầu
- Thiếu kiểm soát quyền trong quá trình tạo user

### Giải pháp mới:

#### 1. Cho phép tự gán role cơ bản
- Người dùng có thể tự gán một số role cơ bản
- Không thể tự gán role quản trị

#### 2. Tạo DTO mới `CreateUserWithRolesRequest`
- Chỉ admin mới được phép sử dụng
- Có field `roles` để admin chỉ định role cho user

#### 3. Thêm validation bảo mật
- Kiểm tra role tồn tại trước khi gán
- Không cho phép tạo user với role ADMIN (ngăn chặn leo thang đặc quyền)
- Chỉ admin mới có quyền tạo user với role cụ thể

#### 4. Endpoint mới cho admin
- `POST /users/admin/create-user-with-roles` - Tạo user với role cụ thể
- Có annotation `@PreAuthorize("hasRole('ADMIN')")` để bảo vệ

### Role người dùng có thể tự gán:
```java
Set<String> selfAssignableRoles = Set.of(
    "PREMIUM_USER",           // User trả phí
    "VERIFIED_USER",          // User đã xác minh email/SĐT
    "POWER_USER",             // User hoạt động tích cực
    "BETA_TESTER",            // User tham gia beta testing
    "NEWSLETTER_SUBSCRIBER"   // User đăng ký newsletter
);
```

## Luồng hoạt động mới:

### Đăng ký với role cơ bản:
```
POST /auth/register
{
  "username": "user123",
  "email": "user@example.com",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "0123456789",
  "roles": ["PREMIUM_USER", "NEWSLETTER_SUBSCRIBER"]
}
```
→ ✅ Gán role `USER` + `PREMIUM_USER` + `NEWSLETTER_SUBSCRIBER`

### Đăng ký với role quản trị (sẽ bị bỏ qua):
```
POST /auth/register
{
  "username": "hacker123",
  "email": "hacker@example.com",
  "password": "Password123!",
  "firstName": "Hacker",
  "lastName": "User",
  "phoneNumber": "0123456789",
  "roles": ["ADMIN", "MODERATOR"]
}
```
→ ⚠️ Chỉ gán role `USER`, bỏ qua `ADMIN` và `MODERATOR`

### Admin tạo user với role quản trị:
```
POST /users/admin/create-user-with-roles
{
  "username": "moderator123",
  "email": "moderator@example.com",
  "password": "Password123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "0987654321",
  "roles": ["MODERATOR", "EDITOR"]
}
```
→ ✅ Gán role `MODERATOR` + `EDITOR` (trừ ADMIN)

## Lợi ích của giải pháp:

### 1. **Linh hoạt cho người dùng:**
- Có thể tự chọn role phù hợp với nhu cầu
- Tăng trải nghiệm người dùng
- Giảm gánh nặng cho admin

### 2. **Bảo mật vẫn được đảm bảo:**
- Không thể tự gán role quản trị
- Log đầy đủ việc tự gán role
- Validation chặt chẽ

### 3. **Phù hợp với thực tế:**
- Theo chuẩn của các platform lớn
- Cân bằng giữa bảo mật và trải nghiệm
- Dễ dàng mở rộng

## Migration cần thiết:

1. **Frontend:** Cập nhật form đăng ký với checkbox role cơ bản
2. **Database:** Tạo các role mới (PREMIUM_USER, VERIFIED_USER, etc.)
3. **Documentation:** Cập nhật API docs với role mới
4. **Testing:** Test các trường hợp tự gán role

## Monitoring và Audit:

```java
// Log khi user tự gán role
log.info("User {} tự gán role: {}", username, roleName);

// Log khi user cố gắng gán role không được phép
log.warn("User {} cố gắng tự gán role không được phép: {}", username, roleName);
``` 