@echo off
echo ==========================================
echo   Fake Link Detection Bot - Full Start
echo ==========================================

echo.
echo [1/2] Starting Python ML Server (port 5000)...
start "Python ML Server" cmd /k "cd /d D:\flinkbot\python && pip install flask scikit-learn -q && python ml_server.py"

echo Waiting 5 seconds for Python to start...
timeout /t 5 /nobreak > nul

echo.
echo [2/2] Starting Java Spring Boot Bot (port 8080)...
cd /d D:\flinkbot
mvn spring-boot:run
pause
