# Stops the Placement Portal started by start.cmd (also frees the port if something else of ours is holding it).
param([int]$Port = 8080)
$root = Split-Path $PSScriptRoot -Parent
$pidFile = Join-Path $root ".app.pid"
$stopped = $false

if (Test-Path $pidFile) {
    $id = [int](Get-Content $pidFile)
    if (Get-Process -Id $id -ErrorAction SilentlyContinue) {
        & taskkill /PID $id /T /F | Out-Null
        Write-Host "[ OK ] Stopped PID $id" -ForegroundColor Green
        $stopped = $true
    }
    Remove-Item $pidFile -Force
}

# fallback: a java process of this project still listening on the port (for example started from an IDE)
$conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn) {
    $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$($conn.OwningProcess)"
    if ($proc -and $proc.CommandLine -match "placement") {
        & taskkill /PID $conn.OwningProcess /T /F | Out-Null
        Write-Host "[ OK ] Stopped application on port $Port (PID $($conn.OwningProcess))" -ForegroundColor Green
        $stopped = $true
    } elseif ($proc) {
        Write-Host "[WARN] Port $Port is used by another program (PID $($conn.OwningProcess), $($proc.Name)); not touching it." -ForegroundColor Yellow
    }
}
if (-not $stopped) { Write-Host "[INFO] Nothing to stop: the application is not running." -ForegroundColor Cyan }
