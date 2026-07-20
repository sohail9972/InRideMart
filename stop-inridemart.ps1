$ErrorActionPreference = "SilentlyContinue"

$project = $PSScriptRoot

Write-Host "Stopping InRideMart..." -ForegroundColor Cyan

# --------------------------------------------------------
# Stop Spring Boot Services
# --------------------------------------------------------

$servicePatterns = @(
    "services/auth-service",
    "services/customer-service",
    "services/catalog-service",
    "services/cart-service",
    "services/order-service",
    "services/ai-service",
    "services/payment-service"
)

Get-CimInstance Win32_Process |
Where-Object {
    $_.Name -eq "java.exe" -and (
        $servicePatterns | Where-Object {
            $_.CommandLine -match [regex]::Escape($_)
        }
    )
} |
ForEach-Object {
    Write-Host "Stopping Java Process PID $($_.ProcessId)" -ForegroundColor Yellow
    Stop-Process -Id $_.ProcessId -Force
}

# --------------------------------------------------------
# Stop Next.js
# --------------------------------------------------------

Get-CimInstance Win32_Process |
Where-Object {
    $_.Name -eq "node.exe" -and
    $_.CommandLine -match "next"
} |
ForEach-Object {
    Write-Host "Stopping Frontend PID $($_.ProcessId)" -ForegroundColor Yellow
    Stop-Process -Id $_.ProcessId -Force
}

# --------------------------------------------------------
# Stop Docker Containers
# --------------------------------------------------------

Set-Location $project

Write-Host "Stopping Docker Containers..." -ForegroundColor Cyan

docker compose down

# --------------------------------------------------------
# Cleanup Ports (Optional)
# --------------------------------------------------------

$ports = 8081,8082,8083,8084,8085,8086,8087,3000

foreach ($port in $ports) {

    $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue

    foreach ($connection in $connections) {

        try {

            Stop-Process -Id $connection.OwningProcess -Force

            Write-Host "Freed Port $port (PID $($connection.OwningProcess))" -ForegroundColor DarkYellow

        }
        catch { }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "All InRideMart services have been stopped." -ForegroundColor Green
Write-Host "Docker infrastructure stopped." -ForegroundColor Green
Write-Host "Ports 8081-8087 and 3000 cleaned." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green