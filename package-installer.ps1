# =====================================================================
# Script dong goi bo cai dat Setup.exe cho QLBH Clone
# Ho tro chay tren may tinh khac ma KHONG CAN cai Java hay Firebird Server
# Tu dong tao 2 Shortcut ngoai Desktop: Ban Quan ly & Ban POS cam ung
# =====================================================================

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $scriptDir) { $scriptDir = (Get-Location).Path }
Set-Location $scriptDir

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   BAT DAU DONG GOI BO CAI DAT SETUP.EXE - QLBH CLONE   " -ForegroundColor Cyan
Write-Host "       (Tich hop ca 2 ban: Quan ly va Ban hang POS)     " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Thiet lap moi truong (Java 22 + WiX Toolset)
$jdkBin = "C:\Program Files\Java\jdk-22\bin"
$wixBin = "C:\Program Files (x86)\WiX Toolset v3.14\bin"

if (Test-Path $jdkBin) {
    $env:Path = "$jdkBin;$env:Path"
}
if (Test-Path $wixBin) {
    $env:Path = "$wixBin;$env:Path"
}

# Kiem tra cong cu jpackage
$jpackageExe = $null
$cmd = Get-Command jpackage.exe -ErrorAction SilentlyContinue
if ($cmd) {
    $jpackageExe = $cmd.Source
} elseif (Test-Path "$jdkBin\jpackage.exe") {
    $jpackageExe = "$jdkBin\jpackage.exe"
}

if (-not $jpackageExe) {
    Write-Error "Khong tim thay cong cu jpackage.exe. Vui long kiem tra duong dan JDK 22!"
    exit 1
}

# Kiem tra WiX Toolset
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    Write-Error "Khong tim thay WiX Toolset (candle.exe / light.exe). Can WiX Toolset v3.14 de tao file .exe!"
    exit 1
}

# 2. Bien dich ma nguon Java bang Maven
Write-Host "`n[1/4] Bien dich du an va tao fat JAR (Maven package)..." -ForegroundColor Yellow
$mvnCmd = "mvn.cmd"
$mvnCheck = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($mvnCheck) {
    $mvnCmd = $mvnCheck.Source
}

& $mvnCmd package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Bien dich Maven that bai!"
    exit 1
}

# 3. Chuan bi thu muc staging dong goi: installer-input-embedded
Write-Host "`n[2/4] Chuan bi tep tin & thu vien nhung (Firebird Embedded, DB Seed)..." -ForegroundColor Yellow
$stagingDir = Join-Path $scriptDir "installer-input-embedded"
$firebirdDir = Join-Path $stagingDir "firebird"
$seedDir = Join-Path $stagingDir "seed"
$packagingDir = Join-Path $scriptDir "packaging"

New-Item -ItemType Directory -Force $stagingDir | Out-Null
New-Item -ItemType Directory -Force $firebirdDir | Out-Null
New-Item -ItemType Directory -Force $seedDir | Out-Null
New-Item -ItemType Directory -Force $packagingDir | Out-Null

# Copy file JAR moi nhat
$jarSource = Join-Path $scriptDir "target\QLBH_Clone-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarSource)) {
    Write-Error "Khong tim thay file JAR dau ra tai: $jarSource"
    exit 1
}
Copy-Item $jarSource -Destination (Join-Path $stagingDir "QLBH_Clone-1.0-SNAPSHOT.jar") -Force

# Dam bao Firebird Embedded runtime day du
$fbSource = Join-Path $scriptDir "packaging\firebird\Firebird-2.5.9.27139-0_x64_embed"
if (Test-Path $fbSource) {
    Copy-Item "$fbSource\*" -Destination $firebirdDir -Recurse -Force
}

# Dam bao CSDL mau (seed)
$dbSource = Join-Path $scriptDir "data\DEMO.FDB"
if (Test-Path $dbSource) {
    Copy-Item $dbSource -Destination (Join-Path $seedDir "DEMO.FDB") -Force
}

# Chuan bi cau hinh cho launcher phu: QLBH POS (man hinh cam ung ban hang)
$posPropertiesPath = Join-Path $packagingDir "pos-launcher.properties"
$posPropertiesContent = @"
arguments=--touch
main-class=kazuko.ex.Main
main-jar=QLBH_Clone-1.0-SNAPSHOT.jar
icon=src/main/resources/IconTs.ico
win-shortcut=true
win-menu=true
"@
$posPropertiesContent | Set-Content -Path $posPropertiesPath -Encoding ASCII

# 4. Chay jpackage de tao bo cai dat exe
$destDir = Join-Path $scriptDir "dist"
New-Item -ItemType Directory -Force $destDir | Out-Null

$appVersion = "1.0.4"
$iconDesktopPath = Join-Path $scriptDir "src\main\resources\Icon.ico"

Write-Host "`n[3/4] Dang dong goi bo cai dat bang jpackage..." -ForegroundColor Yellow
Write-Host "   + Launcher 1: QLBH Clone (Ban Quan ly - Desktop)" -ForegroundColor DarkGray
Write-Host "   + Launcher 2: QLBH POS (Ban Ban hang cam ung - POS touch)" -ForegroundColor DarkGray
Write-Host "Qua trinh nay mat khoang 30 - 60 giay..." -ForegroundColor DarkGray

& $jpackageExe `
  --type exe `
  --name "QLBH Clone" `
  --input $stagingDir `
  --main-jar "QLBH_Clone-1.0-SNAPSHOT.jar" `
  --main-class kazuko.ex.Main `
  --app-version $appVersion `
  --vendor "Tan An Phat" `
  --description "Phan mem Quan ly Ban hang Tan An Phat" `
  --icon $iconDesktopPath `
  --add-launcher "QLBH POS=$posPropertiesPath" `
  --dest $destDir `
  --win-dir-chooser `
  --win-menu `
  --win-menu-group "QLBH Clone" `
  --win-shortcut `
  --win-per-user-install

if ($LASTEXITCODE -ne 0) {
    Write-Error "Dong goi jpackage that bai!"
    exit 1
}

# 5. Hoan thanh
Write-Host "`n[4/4] HOAN TAT DONG GOI THANH CONG!" -ForegroundColor Green
$exeFile = Get-ChildItem -Path $destDir -Filter "QLBH Clone-$appVersion.exe" | Select-Object -First 1
if ($exeFile) {
    $sizeMB = [math]::Round($exeFile.Length / 1MB, 2)
    Write-Host "--------------------------------------------------------" -ForegroundColor Green
    Write-Host "File cai dat dau ra : $($exeFile.FullName)" -ForegroundColor White
    Write-Host "Dung luong          : $sizeMB MB" -ForegroundColor White
    Write-Host "--------------------------------------------------------" -ForegroundColor Green
    Write-Host "Khi cai dat file nay tren may khac, man hinh Desktop se co 2 Shortcut:" -ForegroundColor Cyan
    Write-Host "   1. QLBH Clone  (Ban Quan tri / Quan ly ban hang)" -ForegroundColor White
    Write-Host "   2. QLBH POS    (Ban Ban hang cam ung POS)" -ForegroundColor White
}
