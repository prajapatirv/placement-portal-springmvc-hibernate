# Installs Temurin JDK 21 for the current user (no admin rights) and sets JAVA_HOME and Path.
# Run: powershell -ExecutionPolicy Bypass -File .\prerequisites\setup-windows.ps1
$dst = Join-Path $env:USERPROFILE ".jdks\temurin-21"
if (Test-Path (Join-Path $dst "bin\java.exe")) {
    Write-Host "JDK already present at $dst"
} else {
    $zip = Join-Path $env:TEMP "temurin21.zip"
    Write-Host "Downloading Temurin 21 (about 200 MB) ..."
    Invoke-WebRequest -Uri "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse" -OutFile $zip
    $tmp = Join-Path $env:TEMP "temurin21-unzip"
    if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
    Expand-Archive $zip -DestinationPath $tmp
    New-Item -ItemType Directory -Force (Split-Path $dst) | Out-Null
    Move-Item (Get-ChildItem $tmp | Select-Object -First 1).FullName $dst
    Remove-Item $zip, $tmp -Recurse -Force
}
[Environment]::SetEnvironmentVariable("JAVA_HOME", $dst, "User")
$path = [Environment]::GetEnvironmentVariable("Path", "User")
if (($path -split ';') -notcontains "$dst\bin") {
    [Environment]::SetEnvironmentVariable("Path", ("$dst\bin;" + $path).TrimEnd(';'), "User")
}
Write-Host "Done. Open a NEW terminal and run: java -version"
