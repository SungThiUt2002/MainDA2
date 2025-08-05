##
#Write-Host "=== START ALL SERVICES ===" -ForegroundColor Cyan
#
#Write-Host "`n1. CREATE NETWORK..." -ForegroundColor Yellow
#docker network create cart-network | Out-Null
#
#Write-Host "`n2. START DATABASE SERVICES..." -ForegroundColor Yellow
#docker-compose -f ../docker-compose-db.yml up -d
#
#Write-Host "`n3. WAITING DATABASE START..." -ForegroundColor Yellow
#Start-Sleep -Seconds 15
#
###--------------
#
#Write-Host "`n4. START UP SECURITY SERVICES..." -ForegroundColor Yellow
#docker-compose -f ../docker-compose-security.yml up -d
#
#Write-Host "`n5. START UP MONITORING SERVICES..." -ForegroundColor Yellow
#docker-compose -f ../docker-compose-monitoring.yml up -d
#
#Write-Host "`n6. START UP CART SERVICE..." -ForegroundColor Yellow
#docker-compose -f ../docker-compose-app.yml up -d
#
#Write-Host "`n7. WAITING SERVICES START UP..." -ForegroundColor Yellow
#Start-Sleep -Seconds 30
#
##----------
#
#Write-Host "`n8. CHECK STATUS..." -ForegroundColor Yellow
#docker ps
##
#Write-Host "`n9. TESTING HEALTH CHECK..." -ForegroundColor Yellow
#
#try
#{
#	$response=Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 10
#	if ($response.StatusCode -eq 200)
#	{
#		Write-Host "Health check successful" -ForegroundColor Green
#		Write-Host "Response: $($response.Content)" -ForegroundColor Gray
#
#	}
#	else
#	{
#		Write-Host "Health check response of status: $($response.StatusCode)" -ForegroundColor Yellow
#	}
#}
#catch
#{
#	Write-Host "Health check failed: $_" -ForegroundColor Yellow
#	Write-Host "The service may not have started yet, try again after 30 seconds..." -ForegroundColor Yellow
#}
#
#Write-Host "`n ==COMPLETE==" -ForegroundColor Cyan
#Write-Host "All services have been started!" -ForegroundColor Green
#Write-Host "Cart Service: http://localhost:8081" -ForegroundColor Cyan
#Write-Host "Health Check: http://localhost:8081/actuator/health" -ForegroundColor Cyan
#Write-Host "Prometheus: http://localhost:9090" -ForegroundColor Cyan
#Write-Host "Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor Cyan
#Write-Host "SonarQube: http://localhost:9000 (admin/admin)" -ForegroundColor Cyan
#Write-Host "OWASP ZAP: http://localhost:8080" -ForegroundColor Cyan
#Write-Host "PgAdmin: http://localhost:5050 (sungthiut2002@gmail.com/stu123123)" -ForegroundColor Cyan
