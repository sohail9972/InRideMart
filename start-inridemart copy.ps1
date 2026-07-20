param(
    [string]$OpenAiApiKey = $env:OPENAI_API_KEY
)

$ErrorActionPreference = "Stop"

# Shared runtime configuration. Every protected service must receive the same JWT secret.
$env:INRIDEMART_DB_PASSWORD = "InRide@123"
$env:JWT_SECRET = "abcdefghijklmnopqrstuvwxyz123456"
$env:OPENAI_API_KEY = $OpenAiApiKey

$project = $PSScriptRoot

function Start-InRideMartProcess {
    param(
        [string]$Name,
        [string]$WorkingDirectory,
        [string]$Command
    )

    Write-Host "Starting $Name..." -ForegroundColor Cyan
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$WorkingDirectory'; $Command"
    )
}

Set-Location $project

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker is not available in PATH. Start Docker Desktop and reopen PowerShell."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven is not available in PATH. Install Maven or add it to PATH."
}

Write-Host "Starting Docker infrastructure..." -ForegroundColor Cyan
docker compose up -d

Write-Host "Waiting for PostgreSQL..." -ForegroundColor Cyan
$deadline = (Get-Date).AddSeconds(60)
do {
    Start-Sleep -Seconds 2
    $postgresReady = docker compose exec -T postgres pg_isready -U inridemart -d inridemart_auth 2>$null
} while ($LASTEXITCODE -ne 0 -and (Get-Date) -lt $deadline)

if ($LASTEXITCODE -ne 0) {
    throw "PostgreSQL did not become ready within 60 seconds. Run 'docker compose logs postgres'."
}

Start-InRideMartProcess "Auth Service" $project "mvn -pl services/auth-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Customer Service" $project "mvn -pl services/customer-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Catalog Service" $project "mvn -pl services/catalog-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Cart Service" $project "mvn -pl services/cart-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Order Service" $project "mvn -pl services/order-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "AI Service" $project "mvn -pl services/ai-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Payment Service" $project "mvn -pl services/payment-service spring-boot:run"
Start-Sleep -Seconds 2
Start-InRideMartProcess "Frontend" (Join-Path $project "frontend") "npm run dev"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "InRideMart is starting..." -ForegroundColor Green
Write-Host "Frontend : http://localhost:3000"
Write-Host "Auth     : http://localhost:8081/swagger-ui.html"
Write-Host "Customer : http://localhost:8082/swagger-ui.html"
Write-Host "Catalog  : http://localhost:8083/swagger-ui.html"
Write-Host "Cart     : http://localhost:8084/swagger-ui.html"
Write-Host "Order    : http://localhost:8085/swagger-ui.html"
Write-Host "AI       : http://localhost:8086/swagger-ui.html"
Write-Host "Payment  : http://localhost:8087/swagger-ui.html"
Write-Host "========================================" -ForegroundColor Green

if ([string]::IsNullOrWhiteSpace($env:OPENAI_API_KEY)) {
    Write-Host "OPENAI_API_KEY is not set. AI Service will use deterministic catalog recommendations." -ForegroundColor Yellow
}
