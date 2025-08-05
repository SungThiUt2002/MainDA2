#Write-Host "=== KIỂM TRA MÔI TRƯỜNG HIỆN TẠI ===" -ForegroundColor Cyan
#
#Write-Host "`n1. THÔNG TIN HỆ THỐNG:" -ForegroundColor Yellow
#Write-Host "OS: $env:OS" -ForegroundColor White
#Write-Host "Current Directory: $(Get-Location)" -ForegroundColor White
#
#Write-Host "`n2. DOCKER:" -ForegroundColor Yellow
#try {
#    $dockerVersion = docker --version 2>$null
#    if ($dockerVersion) {
#        Write-Host "✓ $dockerVersion" -ForegroundColor Green
#    } else {
#        Write-Host "✗ Docker chưa cài đặt hoặc không hoạt động" -ForegroundColor Red
#    }
#} catch {
#    Write-Host "✗ Docker chưa cài đặt hoặc không hoạt động" -ForegroundColor Red
#}
#
#try {
#    $dockerComposeVersion = docker-compose --version 2>$null
#    if ($dockerComposeVersion) {
#        Write-Host "✓ $dockerComposeVersion" -ForegroundColor Green
#    } else {
#        Write-Host "✗ Docker Compose chưa cài đặt" -ForegroundColor Red
#    }
#} catch {
#    Write-Host "✗ Docker Compose chưa cài đặt" -ForegroundColor Red
#}
#
#Write-Host "`n3. GIT:" -ForegroundColor Yellow
#try {
#    $gitVersion = git --version 2>$null
#    if ($gitVersion) {
#        Write-Host "✓ $gitVersion" -ForegroundColor Green
#    } else {
#        Write-Host "✗ Git chưa cài đặt" -ForegroundColor Red
#    }
#} catch {
#    Write-Host "✗ Git chưa cài đặt" -ForegroundColor Red
#}
#
#Write-Host "`n4. JAVA:" -ForegroundColor Yellow
#try {
#    $javaVersion = java -version 2>&1
#    if ($javaVersion -like "*version*") {
#        Write-Host "✓ Java đã cài đặt" -ForegroundColor Green
#        Write-Host "  $($javaVersion[0])" -ForegroundColor Gray
#    } else {
#        Write-Host "✗ Java chưa cài đặt" -ForegroundColor Red
#    }
#} catch {
#    Write-Host "✗ Java chưa cài đặt" -ForegroundColor Red
#}
#
#Write-Host "`n5. CONTAINERS ĐANG CHẠY:" -ForegroundColor Yellow
#try {
#    $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>$null
#    if ($containers -and $containers.Length -gt 1) {
#        Write-Host "✓ Có containers đang chạy:" -ForegroundColor Green
#        $containers | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
#    } else {
#        Write-Host "ℹ Không có containers nào đang chạy" -ForegroundColor Yellow
#    }
#} catch {
#    Write-Host "ℹ Không thể kiểm tra containers" -ForegroundColor Yellow
#}
#
#Write-Host "`n6. DOCKER IMAGES:" -ForegroundColor Yellow
#try {
#    $images = docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" 2>$null
#    if ($images -and $images.Length -gt 1) {
#        Write-Host "✓ Có Docker images:" -ForegroundColor Green
#        $images | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
#    } else {
#        Write-Host "ℹ Không có Docker images nào" -ForegroundColor Yellow
#    }
#} catch {
#    Write-Host "ℹ Không thể kiểm tra Docker images" -ForegroundColor Yellow
#}
#
#Write-Host "`n7. THƯ MỤC HIỆN TẠI:" -ForegroundColor Yellow
#$currentFiles = Get-ChildItem -Name
#if ($currentFiles) {
#    Write-Host "✓ Files trong thư mục hiện tại:" -ForegroundColor Green
#    $currentFiles | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
#} else {
#    Write-Host "ℹ Thư mục trống" -ForegroundColor Yellow
#}
#
#Write-Host "`n8. KIỂM TRA CÁC FILE QUAN TRỌNG:" -ForegroundColor Yellow
#$importantFiles = @("pom.xml", "Dockerfile", "docker-compose-app.yml", "src/main/java/com/stu/cart_service/CartServiceApplication.java")
#foreach ($file in $importantFiles) {
#    if (Test-Path $file) {
#        Write-Host "✓ $file" -ForegroundColor Green
#    } else {
#        Write-Host "✗ $file" -ForegroundColor Red
#    }
#}
#
#Write-Host "`n=== KẾT QUẢ KIỂM TRA ===" -ForegroundColor Cyan