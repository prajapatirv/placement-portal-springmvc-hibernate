# Starts the Placement Portal and waits until it is healthy.
#   start.cmd              -> Supabase (reads config\supabase.properties)
#   start.cmd local        -> in-memory H2, no account needed
#   start.cmd demo         -> Supabase + N+1 demo output in the log
# Options: -Port 8080  -TimeoutSec 180  -Rebuild
param(
    [ValidateSet("supabase", "local", "demo")] [string]$Mode = "supabase",
    [int]$Port = 8080,
    [int]$TimeoutSec = 180,
    [switch]$Rebuild
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$pidFile = Join-Path $root ".app.pid"
$logDir = Join-Path $root "logs"
$log = Join-Path $logDir "app.log"
$errLog = Join-Path $logDir "app.err.log"
New-Item -ItemType Directory -Force $logDir | Out-Null

function Ok($m)   { Write-Host "[ OK ] $m" -ForegroundColor Green }
function Info($m) { Write-Host "[INFO] $m" -ForegroundColor Cyan }
function Fail($m) { Write-Host "[FAIL] $m" -ForegroundColor Red; exit 1 }

# ---- 1. Java 21+ ----
function Find-Java {
    $cands = @()
    if ($env:JAVA_HOME) { $cands += (Join-Path $env:JAVA_HOME "bin\java.exe") }
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { $cands += $cmd.Source }
    $cands += (Get-ChildItem "$env:USERPROFILE\.jdks\*\bin\java.exe" -ErrorAction SilentlyContinue | ForEach-Object FullName)
    foreach ($c in $cands) {
        if (-not (Test-Path $c)) { continue }
        $v = (& cmd /c "`"$c`" -version 2>&1" | Select-Object -First 1)
        $m = [regex]::Match($v, '"(\d+)')
        if ($m.Success -and [int]$m.Groups[1].Value -ge 21) { return @{ Path = $c; Version = $v } }
    }
    return $null
}
$java = Find-Java
if (-not $java) { Fail "JDK 21 or newer not found. Run prerequisites\setup-windows.ps1 or see prerequisites\README.md" }
Ok "Java: $($java.Version.Trim())"
$env:JAVA_HOME = Split-Path (Split-Path $java.Path -Parent) -Parent

# ---- 2. already running? ----
if (Test-Path $pidFile) {
    $old = [int](Get-Content $pidFile)
    if (Get-Process -Id $old -ErrorAction SilentlyContinue) { Fail "Already running (PID $old). Run stop.cmd first, or open http://localhost:$Port" }
    Remove-Item $pidFile -Force
}
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
    Fail "Port $Port is already in use by another program. Run stop.cmd, or start with -Port 8081"
}

# ---- 3. database settings check ----
$profileArg = @()
if ($Mode -eq "local") {
    $profileArg = @("--spring.profiles.active=local")
    Info "Mode: local (in-memory H2, no Supabase needed)"
} else {
    $cfg = Join-Path $root "config\supabase.properties"
    if (-not (Test-Path $cfg)) { Fail "config\supabase.properties not found. Copy config\supabase.properties.example to config\supabase.properties and fill in the password." }
    $props = @{}
    Get-Content $cfg | ForEach-Object { if ($_ -match '^\s*([^#=\s][^=]*?)\s*=\s*(.*)$') { $props[$matches[1]] = $matches[2].Trim() } }
    foreach ($k in "supabase.host", "supabase.user", "supabase.password") { if (-not $props[$k]) { Fail "$k is missing in config\supabase.properties" } }
    if ($props["supabase.password"] -match "YOUR-DB-PASSWORD|YOUR-PASSWORD") { Fail "Set supabase.password in config\supabase.properties (Supabase > Project Settings > Database > Reset password)." }
    $dbHost = $props["supabase.host"]; $dbPort = if ($props["supabase.port"]) { [int]$props["supabase.port"] } else { 5432 }
    Info "Mode: Supabase ($dbHost`:$dbPort, user $($props['supabase.user']))"
    $t = Test-NetConnection $dbHost -Port $dbPort -WarningAction SilentlyContinue
    if (-not $t.TcpTestSucceeded) {
        Fail "Cannot reach $dbHost`:$dbPort. The direct host is IPv6 only: on an IPv4-only network switch to the Session pooler (see config\supabase.properties.example), or run: start.cmd local"
    }
    Ok "Database host reachable ($($t.RemoteAddress))"
    if ($Mode -eq "demo") { $profileArg = @("--demo.n-plus-one=true") }
}

# ---- 4. build when needed ----
$jar = Get-ChildItem (Join-Path $root "target") -Filter "placement-portal-*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
$stale = $true
if ($jar -and -not $Rebuild) {
    $newest = Get-ChildItem (Join-Path $root "src"), (Join-Path $root "pom.xml") -Recurse -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    $stale = $newest.LastWriteTime -gt $jar.LastWriteTime
}
if ($stale) {
    Info "Building (first run downloads dependencies, this can take a few minutes) ..."
    & cmd /c "`"$root\mvnw.cmd`" -B -q -DskipTests package > `"$logDir\build.log`" 2>&1"
    if ($LASTEXITCODE -ne 0) { Get-Content "$logDir\build.log" -Tail 25; Fail "Build failed, see logs\build.log" }
    $jar = Get-ChildItem (Join-Path $root "target") -Filter "placement-portal-*.jar" | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
    Ok "Build done"
} else { Ok "Jar is up to date" }

# ---- 5. start ----
$argList = @("-jar", "`"$($jar.FullName)`"", "--server.port=$Port") + $profileArg
$p = Start-Process $java.Path -ArgumentList $argList -WorkingDirectory $root -PassThru -WindowStyle Hidden -RedirectStandardOutput $log -RedirectStandardError $errLog
$p.Id | Set-Content $pidFile
Info "Started PID $($p.Id); waiting for health (up to $TimeoutSec s) ..."

# ---- 6. health check ----
$base = "http://localhost:$Port"
$deadline = (Get-Date).AddSeconds($TimeoutSec)
$healthy = $false
while ((Get-Date) -lt $deadline) {
    if ($p.HasExited) { break }
    try {
        $h = Invoke-RestMethod "$base/actuator/health" -TimeoutSec 3
        if ($h.status -eq "UP") { $healthy = $true; break }
    } catch { }
    Start-Sleep -Seconds 2
}
if (-not $healthy) {
    Write-Host "`n--- last log lines ---" -ForegroundColor Yellow
    Get-Content $log -Tail 30 -ErrorAction SilentlyContinue
    Get-Content $errLog -Tail 10 -ErrorAction SilentlyContinue
    if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force }
    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    Fail "Application did not become healthy. Common causes are in README.md (Troubleshooting). Full log: logs\app.log"
}
Ok "Health: UP"
$h.components.PSObject.Properties | ForEach-Object { Ok ("  {0,-12} {1}" -f $_.Name, $_.Value.status) }

# ---- 7. smoke checks ----
$checks = @("/", "/jobs", "/companies", "/students", "/applications", "/api/jobs")
$bad = 0
foreach ($c in $checks) {
    try { $r = Invoke-WebRequest "$base$c" -UseBasicParsing -TimeoutSec 20; Ok ("{0,-14} HTTP {1}" -f $c, $r.StatusCode) }
    catch { $bad++; Write-Host ("[FAIL] {0,-14} {1}" -f $c, $_.Exception.Message) -ForegroundColor Red }
}
$jobs = (Invoke-RestMethod "$base/api/jobs").Count
Ok "Open jobs returned by API: $jobs (seed data expects 11)"

Write-Host ""
if ($bad -eq 0) { Write-Host "Placement Portal is running ($Mode mode)" -ForegroundColor Green } else { Write-Host "Running, but $bad page(s) failed. See logs\app.log" -ForegroundColor Yellow }
Write-Host "  App        $base"
Write-Host "  Jobs       $base/jobs"
Write-Host "  N+1 demo   $base/applications?slow=true   (fast: $base/applications)"
Write-Host "  JSON       $base/api/jobs"
Write-Host "  Health     $base/actuator/health"
Write-Host "  Log        $log"
Write-Host "  Stop       stop.cmd"
