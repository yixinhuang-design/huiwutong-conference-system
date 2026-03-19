@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo  认证服务启动脚本
echo ========================================
echo.

REM 改为相对路径
cd /d "%~dp0"

REM 检查Maven是否安装
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [✗] 未找到Maven
    echo 请确保Maven已安装且在PATH中
    pause
    exit /b 1
)

echo [✓] Maven已检测
echo.

REM 进入后端目录
cd /d "%~dp0backend\conference-backend"
if %errorlevel% neq 0 (
    echo [✗] 无法进入后端目录
    pause
    exit /b 1
)

echo [✓] 已进入后端目录
echo.

REM 编译并启动
echo 正在编译和启动认证服务...
echo （这将花费1-2分钟）
echo.

echo 编译中...
call mvn -s "maven-settings.xml" clean install -DskipTests -q

if %errorlevel% neq 0 (
    echo [✗] 编译失败
    pause
    exit /b 1
)

echo [✓] 编译成功
echo.
echo 启动认证服务 (http://localhost:8081)...
echo 此窗口会保持运行，不要关闭
echo.

call mvn -s "maven-settings.xml" -pl conference-auth spring-boot:run

echo.
echo 服务已关闭
echo.
