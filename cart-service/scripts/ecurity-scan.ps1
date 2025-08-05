Write-Host "Running Trivy vulnerability scan..." -ForegroundColor Green

# Scan với Trivy
docker run --rm -v ${PWD}:/app aquasec/trivy:latest fs --security-checks vuln /app

Write-Host "Running ZAP security scan..." -ForegroundColor Green

# Scan với ZAP (nếu app đang chạy)
docker run --rm -v ${PWD}:/zap/wrk/:rw -t owasp/zap2docker-stable zap-baseline.py -t http://cart-service:8081 -J zap-report.json -m 5

Write-Host "Security scan completed!" -ForegroundColor Green

# Mục đích: Script để chạy security scanning