@echo off
REM Restart Plant Service with fresh token generation

echo.
echo ============================================
echo   PlantPulse - Plant Service Restart
echo ============================================
echo.

REM Change to plant-service directory
cd /d "C:\Users\User\Downloads\Bilka2\plant-service"

REM Clean Maven cache
echo [1/3] Cleaning Maven cache...
mvn clean -q
echo.

REM Start service
echo [2/3] Starting Plant Service with fresh token generation...
echo This will take 30-60 seconds to start...
echo.
echo ============================================
mvn spring-boot:run
echo ============================================