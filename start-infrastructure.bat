@echo off
REM ============================================================
REM  智能会务系统 - Redis 和 Nacos 启动脚本
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ============================================================
echo    智能会务系统 - 基础设施启动
echo ============================================================
echo.

REM ============================================================
REM  1. 尝试启动 Redis
REM ============================================================

echo [1] 检查 Redis...

set REDIS_PATH=C:\Program Files\Redis\redis-server.exe

if exist "%REDIS_PATH%" (
    echo [✓] 发现 Redis，启动中...
    start "Redis Server" "%REDIS_PATH%" --port 6379
    timeout /t 3 /nobreak
    echo [✓] Redis 已启动 (端口: 6379)
) else (
    echo [!] Redis 未安装
    echo.
    echo 请使用以下方法之一安装 Redis:
    echo.
    echo 方法 1 - 使用 Chocolatey (推荐):
    echo   choco install redis -y
    echo.
    echo 方法 2 - 从 GitHub 下载:
    echo   https://github.com/tporadowski/redis/releases
    echo.
    echo 方法 3 - 使用 WSL2:
    echo   wsl --install
    echo   wsl redis-server
    echo.
)

REM ============================================================
REM  2. 尝试启动 Nacos
REM ============================================================

echo.
echo [2] 检查 Nacos...

set NACOS_HOME=G:\huiwutong新版合集\nacos

if exist "%NACOS_HOME%\bin\startup.cmd" (
    echo [✓] 发现 Nacos，启动中...
    cd /d "%NACOS_HOME%\bin"
    call startup.cmd -m standalone
    echo [✓] Nacos 已启动 (端口: 8848)
    echo.
    echo Nacos 地址: http://localhost:8848/nacos
    echo 默认账号: nacos
    echo 默认密码: nacos
) else (
    echo [!] Nacos 未安装
    echo.
    echo 请下载 Nacos 并解压到: %NACOS_HOME%
    echo 下载地址: https://github.com/alibaba/nacos/releases
    echo.
)

echo.
echo ============================================================
echo    基础设施启动完成
echo ============================================================
echo.
pause
