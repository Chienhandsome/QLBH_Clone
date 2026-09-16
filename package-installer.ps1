# =====================================================================
# Script đóng gói bộ cài đặt Setup.exe cho QLBH Clone
# Hỗ trợ chạy trên máy tính khác mà KHÔNG CẦN cài Java hay Firebird Server
# Tự động tạo 2 Shortcut ngoài Desktop: Bản Quản lý & Bản POS cảm ứng
# =====================================================================

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $scriptDir) { $scriptDir = (Get-Location).Path }
Set-Location $scriptDir

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   BẮT ĐẦU ĐÓNG GÓI BỘ CÀI ĐẶT SETUP.EXE - QLBH CLONE   " -ForegroundColor Cyan
Write-Host "       (Tích hợp cả 2 bản: Quản lý và Bán hàng POS)     " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Thiết lập môi trường (Java 22 + WiX Toolset)
$jdkBin = "C:\Program Files\Java\jdk-22\bin"
$wixBin = "C:\Program Files (x86)\WiX Toolset v3.14\bin"

if (Test-Path $jdkBin) {
    $env:Path = "$jdkBin;$env:Path"
}
if (Test-Path $wixBin) {
    $env:Path = "$wixBin;$env:Path"
}

# Kiểm tra công cụ jpackage
$jpackageExe = $null
$cmd = Get-Command jpackage.exe -ErrorAction SilentlyContinue
if ($cmd) {
    $jpackageExe = $cmd.Source
} elseif (Test-Path "$jdkBin\jpackage.exe") {
    $jpackageExe = "$jdkBin\jpackage.exe"
}

if (-not $jpackageExe) {
    Write-Error "Không tìm thấy công cụ jpackage.exe. Vui lòng kiểm tra đường dẫn JDK 22!"
    exit 1
}

# Kiểm tra WiX Toolset
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    Write-Error "Không tìm thấy WiX Toolset (candle.exe / light.exe). Cần WiX Toolset v3.14 để tạo file .exe!"
    exit 1
}

# 2. Biên dịch mã nguồn Java bằng Maven
Write-Host "`n[1/4] Biên dịch dự án và tạo fat JAR (Maven package)..." -ForegroundColor Yellow
$mvnCmd = "mvn.cmd"
$mvnCheck = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($mvnCheck) {
    $mvnCmd = $mvnCheck.Source
}

& $mvnCmd package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Biên dịch Maven thất bại!"
    exit 1
}

# 3. Chuẩn bị thư mục staging đóng gói: installer-input-embedded
Write-Host "`n[2/4] Chuẩn bị tệp tin & thư viện nhúng (Firebird Embedded, DB Seed)..." -ForegroundColor Yellow
$stagingDir = Join-Path $scriptDir "installer-input-embedded"
$firebirdDir = Join-Path $stagingDir "firebird"
$seedDir = Join-Path $stagingDir "seed"
$packagingDir = Join-Path $scriptDir "packaging"

New-Item -ItemType Directory -Force $stagingDir | Out-Null
New-Item -ItemType Directory -Force $firebirdDir | Out-Null
New-Item -ItemType Directory -Force $seedDir | Out-Null
New-Item -ItemType Directory -Force $packagingDir | Out-Null

# Copy file JAR mới nhất
$jarSource = Join-Path $scriptDir "target\QLBH_Clone-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarSource)) {
    Write-Error "Không tìm thấy file JAR đầu ra tại: $jarSource"
    exit 1
}
Copy-Item $jarSource -Destination (Join-Path $stagingDir "QLBH_Clone-1.0-SNAPSHOT.jar") -Force

# Đảm bảo Firebird Embedded runtime đầy đủ
$fbSource = Join-Path $scriptDir "packaging\firebird\Firebird-2.5.9.27139-0_x64_embed"
if (Test-Path $fbSource) {
    Copy-Item "$fbSource\*" -Destination $firebirdDir -Recurse -Force
}

# Đảm bảo CSDL mẫu (seed)
$dbSource = Join-Path $scriptDir "data\DEMO.FDB"
if (Test-Path $dbSource) {
    Copy-Item $dbSource -Destination (Join-Path $seedDir "DEMO.FDB") -Force
}

# Chuẩn bị cấu hình cho launcher phụ: QLBH POS (màn hình cảm ứng bán hàng)
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

# 4. Chạy jpackage để tạo bộ cài đặt exe
$destDir = Join-Path $scriptDir "dist"
New-Item -ItemType Directory -Force $destDir | Out-Null

$appVersion = "1.0.3"
$iconDesktopPath = Join-Path $scriptDir "src\main\resources\Icon.ico"

Write-Host "`n[3/4] Đang đóng gói bộ cài đặt bằng jpackage..." -ForegroundColor Yellow
Write-Host "   + Launcher 1: QLBH Clone (Bản Quản lý - Desktop)" -ForegroundColor DarkGray
Write-Host "   + Launcher 2: QLBH POS (Bản Bán hàng cảm ứng - POS touch)" -ForegroundColor DarkGray
Write-Host "Quá trình này mất khoảng 30 - 60 giây..." -ForegroundColor DarkGray

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
    Write-Error "Đóng gói jpackage thất bại!"
    exit 1
}

# 5. Hoàn thành
Write-Host "`n[4/4] HOÀN TẤT ĐÓNG GÓI THÀNH CÔNG!" -ForegroundColor Green
$exeFile = Get-ChildItem -Path $destDir -Filter "QLBH Clone-$appVersion.exe" | Select-Object -First 1
if ($exeFile) {
    $sizeMB = [math]::Round($exeFile.Length / 1MB, 2)
    Write-Host "--------------------------------------------------------" -ForegroundColor Green
    Write-Host "File cài đặt đầu ra : $($exeFile.FullName)" -ForegroundColor White
    Write-Host "Dung lượng          : $sizeMB MB" -ForegroundColor White
    Write-Host "--------------------------------------------------------" -ForegroundColor Green
    Write-Host "Khi cài đặt file này trên máy khác, màn hình Desktop sẽ có 2 Shortcut:" -ForegroundColor Cyan
    Write-Host "   1. 'QLBH Clone'  (Bản Quản trị/Quản lý bán hàng)" -ForegroundColor White
    Write-Host "   2. 'QLBH POS'    (Bản Bán hàng cảm ứng POS)" -ForegroundColor White
}
