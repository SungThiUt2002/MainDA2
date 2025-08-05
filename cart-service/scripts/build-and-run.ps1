#Write-Host "=== BUILD VÀ RUN CART SERVICE ===" -ForegroundColor Cyan
#
## Kiểm tra thư mục hiện tại
#$currentDir = Get-Location
#Write-Host "Thư mục hiện tại: $currentDir" -ForegroundColor Yellow
#
## Chuyển về thư mục gốc của project
#if ($currentDir.Path -like "*scripts*") {
#    Set-Location ..
#    Write-Host "Đã chuyển về thư mục gốc: $(Get-Location)" -ForegroundColor Green
#}
#
## Bước 1: Kiểm tra pom.xml
#if (-not (Test-Path "pom.xml")) {
#    Write-Host "✗ Không tìm thấy pom.xml" -ForegroundColor Red
#    exit 1
#}
#
## Bước 2: Clean và build project
#Write-Host "`n1. CLEANING PROJECT..." -ForegroundColor Yellow
#try {
#    ./mvnw clean
#    Write-Host "✓ Clean thành công" -ForegroundColor Green
#} catch {
#    Write-Host "✗ Clean thất bại: $_" -ForegroundColor Red
#    exit 1
#}
#
#Write-Host "`n2. BUILDING PROJECT..." -ForegroundColor Yellow
#try {
#    ./mvnw package -DskipTests
#    Write-Host "✓ Build thành công" -ForegroundColor Green
#} catch {
#    Write-Host "✗ Build thất bại: $_" -ForegroundColor Red
#    exit 1
#}
#
## Bước 3: Kiểm tra JAR file
#$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -like "*cart-service*" } | Select-Object -First 1
#if (-not $jarFile) {
#    Write-Host "✗ Không tìm thấy JAR file" -ForegroundColor Red
#    exit 1
#}
#
#Write-Host "✓ Tìm thấy JAR: $($jarFile.Name)" -ForegroundColor Green
#
## Bước 4: Build Docker image
#Write-Host "`n3. BUILDING DOCKER IMAGE..." -ForegroundColor Yellow
#try {
#    docker build -t cart-service .
#    Write-Host "✓ Docker build thành công" -ForegroundColor Green
#} catch {
#    Write-Host "✗ Docker build thất bại: $_" -ForegroundColor Red
#    exit 1
#}
#
## Bước 5: Kiểm tra Docker Compose
#if (-not (Test-Path "docker-compose-app.yml")) {
#    Write-Host "✗ Không tìm thấy docker-compose-app.yml" -ForegroundColor Red
#    exit 1
#}
#
## Bước 6: Chạy services
#Write-Host "`n4. STARTING SERVICES..." -ForegroundColor Yellow
#try {
#    docker-compose -f docker-compose-app.yml up -d
#    Write-Host "✓ Services đã khởi động" -ForegroundColor Green
#} catch {
#    Write-Host "✗ Khởi động services thất bại: $_" -ForegroundColor Red
#    exit 1
#}
#
## Bước 7: Kiểm tra trạng thái
#Write-Host "`n5. KIỂM TRA TRẠNG THÁI..." -ForegroundColor Yellow
#Start-Sleep -Seconds 10
#
#try {
#    $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
#    Write-Host "Containers đang chạy:" -ForegroundColor Green
#    $containers | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
#} catch {
#    Write-Host "Không thể kiểm tra containers" -ForegroundColor Yellow
#}
#
## Bước 8: Test health check
#Write-Host "`n6. TESTING HEALTH CHECK..." -ForegroundColor Yellow
#try {
#    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 10
#    if ($response.StatusCode -eq 200) {
#        Write-Host "✓ Health check thành công" -ForegroundColor Green
#        Write-Host "Response: $($response.Content)" -ForegroundColor Gray
#    } else {
#        Write-Host "⚠ Health check trả về status: $($response.StatusCode)" -ForegroundColor Yellow
#    }
#} catch {
#    Write-Host "⚠ Health check thất bại: $_" -ForegroundColor Yellow
#    Write-Host "Có thể service chưa khởi động xong, thử lại sau 30 giây..." -ForegroundColor Yellow
#}
#
#Write-Host "`n=== HOÀN THÀNH ===" -ForegroundColor Cyan
#Write-Host "Cart Service đã được build và khởi động!" -ForegroundColor Green
#Write-Host "URL: http://localhost:8081" -ForegroundColor Cyan
#Write-Host "Health Check: http://localhost:8081/actuator/health" -ForegroundColor Cyan