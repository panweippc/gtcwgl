@echo off
cd /d %~dp0
echo ========================================
echo   JXC Backend Server Start
echo ========================================
echo Port: 8080   Context: /jxc   DB: jxc_db
echo.
:: Check and stop old process on port 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
  echo Found old process on 8080, PID=%%a, stopping...
  taskkill /PID %%a /F >nul 2>&1
  timeout /t 2 /nobreak >nul
)
echo Starting backend... (Ctrl+C to stop)
echo ----------------------------------------
java -Dfile.encoding=UTF-8 -jar jxc-server.jar
pause
