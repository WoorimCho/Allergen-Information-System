# MySQL Connection Diagnostic Script
# Run this in PowerShell to diagnose connection issues

Write-Host "`n=== MySQL Connection Diagnostic ===" -ForegroundColor Cyan
Write-Host "Checking your setup...`n" -ForegroundColor Cyan

# Check 1: Docker is running
Write-Host "[1/7] Checking if Docker is running..." -ForegroundColor Yellow
try {
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Docker is running" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Docker is not running!" -ForegroundColor Red
        Write-Host "  → Please start Docker Desktop" -ForegroundColor Yellow
        exit
    }
} catch {
    Write-Host "  ✗ Docker not found or not running" -ForegroundColor Red
    exit
}

# Check 2: Containers status
Write-Host "`n[2/7] Checking container status..." -ForegroundColor Yellow
$containers = docker-compose ps --format json 2>&1 | ConvertFrom-Json
if ($containers) {
    foreach ($container in $containers) {
        if ($container.Service -eq "mysql") {
            Write-Host "  MySQL Container: $($container.State)" -ForegroundColor $(if ($container.State -eq "running") { "Green" } else { "Red" })
            Write-Host "    Name: $($container.Name)"
            Write-Host "    Status: $($container.Status)"
        }
        if ($container.Service -eq "app") {
            Write-Host "  App Container: $($container.State)" -ForegroundColor $(if ($container.State -eq "running") { "Green" } else { "Red" })
            Write-Host "    Name: $($container.Name)"
            Write-Host "    Status: $($container.Status)"
        }
    }
} else {
    Write-Host "  ℹ No containers running" -ForegroundColor Yellow
    Write-Host "  → Run: docker-compose up -d" -ForegroundColor Cyan
}

# Check 3: MySQL health
Write-Host "`n[3/7] Checking MySQL health..." -ForegroundColor Yellow
$mysqlHealth = docker inspect allergen-mysql --format='{{.State.Health.Status}}' 2>&1
if ($LASTEXITCODE -eq 0) {
    if ($mysqlHealth -eq "healthy") {
        Write-Host "  ✓ MySQL is healthy" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ MySQL status: $mysqlHealth" -ForegroundColor Yellow
        Write-Host "  → Wait a moment and check again" -ForegroundColor Cyan
    }
} else {
    Write-Host "  ✗ MySQL container not found" -ForegroundColor Red
    Write-Host "  → Run: docker-compose up mysql -d" -ForegroundColor Cyan
}

# Check 4: Port 3306 availability
Write-Host "`n[4/7] Checking port 3306..." -ForegroundColor Yellow
$port3306 = netstat -ano | Select-String ":3306"
if ($port3306) {
    Write-Host "  ℹ Port 3306 is in use:" -ForegroundColor Yellow
    $port3306 | ForEach-Object { Write-Host "    $_" }
} else {
    Write-Host "  ℹ Port 3306 is not bound" -ForegroundColor Yellow
}

# Check 5: MySQL connection test
Write-Host "`n[5/7] Testing MySQL connection..." -ForegroundColor Yellow
try {
    $mysqlTest = docker-compose exec -T mysql mysqladmin ping -h localhost -u myuser -psecret 2>&1
    if ($mysqlTest -like "*mysqld is alive*") {
        Write-Host "  ✓ MySQL is accessible" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Cannot connect to MySQL" -ForegroundColor Red
        Write-Host "    $mysqlTest" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ✗ Cannot test MySQL connection" -ForegroundColor Red
}

# Check 6: Database exists
Write-Host "`n[6/7] Checking if database exists..." -ForegroundColor Yellow
try {
    $dbCheck = docker-compose exec -T mysql mysql -u myuser -psecret -e "SHOW DATABASES LIKE 'mydatabase';" 2>&1
    if ($dbCheck -like "*mydatabase*") {
        Write-Host "  ✓ Database 'mydatabase' exists" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Database 'mydatabase' not found" -ForegroundColor Red
        Write-Host "  → It should be created automatically" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ℹ Could not check database" -ForegroundColor Yellow
}

# Check 7: Application logs
Write-Host "`n[7/7] Checking application logs (last 5 lines)..." -ForegroundColor Yellow
try {
    $appLogs = docker-compose logs --tail=5 app 2>&1
    if ($appLogs -like "*Started AllergenInformationSystem*") {
        Write-Host "  ✓ Application started successfully!" -ForegroundColor Green
    } elseif ($appLogs -like "*Communications link failure*") {
        Write-Host "  ✗ Connection error detected" -ForegroundColor Red
    } elseif ($appLogs) {
        Write-Host "  ℹ Recent logs:" -ForegroundColor Yellow
        $appLogs.Split("`n") | Select-Object -Last 3 | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "  ℹ App not running or no logs" -ForegroundColor Yellow
}

# Summary and recommendations
Write-Host "`n=== Diagnosis Complete ===" -ForegroundColor Cyan
Write-Host "`nRecommended Actions:" -ForegroundColor Yellow

$mysqlRunning = docker ps --filter "name=allergen-mysql" --filter "status=running" --quiet
$appRunning = docker ps --filter "name=allergen-app" --filter "status=running" --quiet

if (-not $mysqlRunning -and -not $appRunning) {
    Write-Host "  → Start everything:" -ForegroundColor Cyan
    Write-Host "     docker-compose up --build`n" -ForegroundColor White
}
elseif (-not $mysqlRunning) {
    Write-Host "  → Start MySQL:" -ForegroundColor Cyan
    Write-Host "     docker-compose up mysql -d`n" -ForegroundColor White
}
elseif (-not $appRunning) {
    Write-Host "  → Start application:" -ForegroundColor Cyan
    Write-Host "     docker-compose up app --build`n" -ForegroundColor White
}
else {
    Write-Host "  → Everything seems to be running!" -ForegroundColor Green
    Write-Host "  → Check: http://localhost:8080`n" -ForegroundColor Cyan
}

Write-Host "For detailed logs, run:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f app`n" -ForegroundColor White

Write-Host "To restart everything fresh:" -ForegroundColor Yellow
Write-Host "  docker-compose down && docker-compose up --build`n" -ForegroundColor White

