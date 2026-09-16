@echo off
chcp 65001 >nul
title D¢ng g¢i c…i d?t QLBH Clone (Setup.exe)
cd /d "%~dp0"

echo ========================================================
echo   CHU?N B? DONG GOI B? CAI D?T QLBH CLONE (SETUP.EXE)
echo ========================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0package-installer.ps1"

echo.
pause
