@echo off
REM ========================================
REM 智能排座系统 - 前端HTTP服务器启动脚本
REM ========================================
REM 此脚本启动一个本地HTTP服务器来运行前端应用
REM 这样可以避免CORS跨域问题（相比file://协议）

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  前端HTTP服务器启动
echo ========================================
echo.

REM 获取当前脚本所在目录
set "SCRIPT_DIR=%~dp0"
set "ADMIN_PC_DIR=%SCRIPT_DIR%admin-pc\conference-admin-pc"

REM 检查admin-pc目录是否存在
if not exist "%ADMIN_PC_DIR%" (
    echo 错误: admin-pc目录不存在
    echo 期望路径: %ADMIN_PC_DIR%
    pause
    exit /b 1
)

echo 前端应用位置: %ADMIN_PC_DIR%
echo.

REM 检查Node.js是否安装
where node >nul 2>nul
if %errorlevel% equ 0 (
    echo 检测到Node.js已安装，将使用http-server
    echo.
    
    REM 检查http-server是否安装
    npm list -g http-server >nul 2>nul
    if %errorlevel% equ 0 (
        echo [✓] http-server已安装
        echo.
        echo 启动HTTP服务器...
        echo.
        cd /d "%ADMIN_PC_DIR%"
        http-server -p 8000 -c-1
    ) else (
        echo [?] http-server未安装，正在全局安装...
        call npm install -g http-server
        if !errorlevel! equ 0 (
            echo [✓] 安装完成
            echo.
            echo 启动HTTP服务器...
            echo.
            cd /d "%ADMIN_PC_DIR%"
            http-server -p 8000 -c-1
        ) else (
            echo [✗] 安装失败
            pause
            exit /b 1
        )
    )
    goto :end
)

REM 如果没有Node.js，尝试Python
where python >nul 2>nul
if %errorlevel% equ 0 (
    echo 检测到Python已安装，将使用Python HTTP服务器
    echo.
    echo 启动HTTP服务器...
    echo.
    cd /d "%ADMIN_PC_DIR%"
    python -m http.server 8000
    goto :end
)

REM 如果都没有，提示用户
echo.
echo ========================================
echo [✗] 错误: 未找到HTTP服务器
echo ========================================
echo.
echo 请安装以下任一软件:
echo   1. Node.js (https://nodejs.org)
echo      然后运行: npm install -g http-server
echo.
echo   2. Python (https://www.python.org)
echo.
echo 或者手动启动本地服务器:
echo   - VSCode: 安装 "Live Server" 扩展
echo   - 其他方案: 使用 "SimpleHTTPServer" 或其他本地服务器
echo.
pause
exit /b 1

:end
echo.
echo 服务器已关闭
echo.
