@echo off
cd /d %~dp0
echo ========================================
echo   JXC Nginx Start (port 8088)
echo ========================================
echo Note: port 80 is used by Chanjet T+, using 8088
echo Prerequisite: backend started on 8080
echo Access: http://localhost:8088
echo.
set NGINX_HOME=E:\AI\gtcwgl\tools\nginx-1.24.0
:: Stop old nginx instance if running
"%NGINX_HOME%\nginx.exe" -s stop -p "%NGINX_HOME%" >nul 2>&1
timeout /t 1 /nobreak >nul
echo Starting Nginx... (stop: nginx -s stop)
echo ----------------------------------------
"%NGINX_HOME%\nginx.exe" -p "%NGINX_HOME%"
pause
