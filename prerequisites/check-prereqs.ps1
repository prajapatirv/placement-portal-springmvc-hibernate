# Checks the tools needed for the Placement Portal. Run from anywhere: .\prerequisites\check-prereqs.ps1
$ok = $true
function Report($name, $good, $detail) {
    if ($good) { Write-Host ("[ OK ] {0,-22} {1}" -f $name, $detail) -ForegroundColor Green }
    else { Write-Host ("[FAIL] {0,-22} {1}" -f $name, $detail) -ForegroundColor Red; $script:ok = $false }
}

# JDK 21 or newer
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    $line = (& cmd /c "java -version 2>&1" | Select-Object -First 1)
    $m = [regex]::Match($line, '"(\d+)')
    $major = if ($m.Success) { [int]$m.Groups[1].Value } else { 0 }
    Report "JDK 21+" ($major -ge 21) $line
} else { Report "JDK 21+" $false "java not found; see prerequisites/README.md section 1" }

Report "JAVA_HOME" ([bool]$env:JAVA_HOME) ($(if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "not set (optional, java on PATH is enough)" }))

$git = Get-Command git -ErrorAction SilentlyContinue
Report "Git" ([bool]$git) ($(if ($git) { (git --version) } else { "not found" }))

$root = Split-Path $PSScriptRoot -Parent
Report "Maven wrapper" (Test-Path (Join-Path $root "mvnw.cmd")) "mvnw.cmd in repo root"

if (Test-Path (Join-Path $root "config\supabase.properties")) { Write-Host ("[ OK ] {0,-22} found" -f "supabase.properties") -ForegroundColor Green }
else { Write-Host ("[INFO] {0,-22} missing: copy config\supabase.properties.example, or run 'start.cmd local' (H2)" -f "supabase.properties") -ForegroundColor Yellow }

$busy = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
Report "Port 8080 free" (-not $busy) ($(if ($busy) { "in use; stop the other app or add --server.port=8081" } else { "free" }))

if ($ok) { Write-Host "`nAll required checks passed." -ForegroundColor Green } else { Write-Host "`nFix the FAIL lines above." -ForegroundColor Red; exit 1 }
