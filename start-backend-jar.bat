@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM  智能会务系统 - 认证授权服务启动脚本（JAR模式）
REM ============================================================

cd /d "G:\huiwutong新版合集\backend\conference-backend"

echo.
echo [认证服务启动] 开始编译打包...

REM 先编译打包
call mvn -s "maven-settings.xml" clean package -DskipTests -pl conference-auth -q

if %errorlevel% neq 0 (
    echo [错误] 编译打包失败！
    pause
    exit /b 1
)

echo [完成] 编译打包成功
echo.
echo [启动服务] 启动认证服务...
echo.

REM 运行 JAR 包
java -jar "conference-auth\target\conference-auth-1.0.0.jar"

pause
