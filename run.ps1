# ================================
# RUN ALL SPRING BOOT SERVICES
# Auto set terminal title
# ================================

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$services = @(
    "auth-service",
    "catalog-service",
    "chat-service",
    "chatbot-service",
    "customer_service",
    "inventory-service",
    "notification-service",
    "purchase-service",
    "reporting-service",
    "sales-service"
)

Write-Host "Starting all services..." -ForegroundColor Green

foreach ($s in $services) {

    $servicePath = Join-Path $root $s

    if (Test-Path $servicePath) {

        Write-Host "Starting $s ..." -ForegroundColor Cyan

        Start-Process powershell `
        -ArgumentList "-NoExit", "-Command", "
            `$host.UI.RawUI.WindowTitle = '$s';
            cd '$servicePath';
            mvn spring-boot:run
        "

    } else {
        Write-Host "Not found: $s" -ForegroundColor Red
    }
}

Write-Host "All services launched!" -ForegroundColor Green