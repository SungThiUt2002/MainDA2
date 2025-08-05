#Write-Host "Stopping all services..." -ForegroundColor Yellow
#
## Stop all services
#docker-compose -f docker-compose-app.yml down
#docker-compose -f docker-compose-monitoring.yml down
#docker-compose -f docker-compose-security.yml down
#docker-compose -f docker-compose-db.yml down
#
#Write-Host "Removing containers..." -ForegroundColor Yellow
#docker container prune -f
#
#Write-Host "Removing volumes..." -ForegroundColor Yellow
#docker volume prune -f
#
#Write-Host "Removing networks..." -ForegroundColor Yellow
#docker network prune -f
#
#Write-Host "Cleanup completed!" -ForegroundColor Green