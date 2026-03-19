@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║          智能会务系统 - 后端服务启动脚本                  ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

setlocal enabledelayedexpansion

REM 定义后端目录
set BACKEND_HOME=%~dp0\backend\conference-backend

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java未安装或未配置到PATH中
    pause
    exit /b 1
)

echo ✓ Java已安装，继续启动服务...
echo.
echo 🚀 启动后端微服务...
echo.

REM 定义服务列表和对应的端口
set services[0]=conference-gateway:8080
set services[1]=conference-auth:8081
set services[2]=conference-notification:8083
set services[3]=conference-collaboration:8084
set services[4]=conference-seating:8085
set services[5]=conference-ai:8086
set services[6]=conference-navigation:8087
set services[7]=conference-data:8088

REM 启动所有JAR服务
cd /d "%BACKEND_HOME%"

REM 启动Gateway服务
echo 📦 启动 API网关 (端口 8080)...
start "conference-gateway" cmd /k "java -jar conference-gateway\target\conference-gateway-1.0.0.jar"
timeout /t 3 /nobreak

REM 启动Auth服务
echo 📦 启动 认证授权服务 (端口 8081)...
start "conference-auth" cmd /k "java -jar conference-auth\target\conference-auth-1.0.0.jar"
timeout /t 3 /nobreak

REM 启动Notification服务
echo 📦 启动 通知提醒服务 (端口 8083)...
start "conference-notification" cmd /k "java -jar conference-notification\target\conference-notification-1.0.0.jar"
timeout /t 2 /nobreak

REM 启动Collaboration服务
echo 📦 启动 会务协同服务 (端口 8084)...
start "conference-collaboration" cmd /k "java -jar conference-collaboration\target\conference-collaboration-1.0.0.jar"
timeout /t 2 /nobreak

REM 启动Seating服务
echo 📦 启动 智能排座服务 (端口 8085)...
start "conference-seating" cmd /k "java -jar conference-seating\target\conference-seating-1.0.0.jar"
timeout /t 2 /nobreak

REM 启动AI服务
echo 📦 启动 AI助教服务 (端口 8086)...
start "conference-ai" cmd /k "java -jar conference-ai\target\conference-ai-1.0.0.jar"
timeout /t 2 /nobreak

REM 启动Navigation服务
echo 📦 启动 位置导航服务 (端口 8087)...
start "conference-navigation" cmd /k "java -jar conference-navigation\target\conference-navigation-1.0.0.jar"
timeout /t 2 /nobreak

REM 启动Data服务
echo 📦 启动 数据指挥服务 (端口 8088)...
start "conference-data" cmd /k "java -jar conference-data\target\conference-data-1.0.0.jar"
timeout /t 2 /nobreak

echo.
echo ════════════════════════════════════════════════════════════
echo ✅ 所有服务启动命令已执行！
echo ════════════════════════════════════════════════════════════
echo.
echo 📝 服务端口映射：
echo   • 前端应用:      http://localhost:8080/pages/login.html
echo   • API网关:       http://localhost:8080
echo   • 认证服务:      http://localhost:8081
echo   • 通知服务:      http://localhost:8083
echo   • 协同服务:      http://localhost:8084
echo   • 排座服务:      http://localhost:8085
echo   • AI服务:        http://localhost:8086
echo   • 导航服务:      http://localhost:8087
echo   • 数据服务:      http://localhost:8088
echo.
echo 🔧 基础设施：
echo   • Nacos:         http://localhost:8848/nacos (nacos/nacos)
echo   • MySQL:         localhost:3308
echo   • Redis:         localhost:6379
echo.
echo 📖 测试账号:
echo   • 用户名:        admin
echo   • 密码:          123456
echo.
echo ⚠️  说明：
echo   • 每个服务在独立的窗口中运行
echo   • 关闭窗口可停止对应服务
echo   • 首次启动可能需要2-3分钟初始化
echo   • 检查各服务的启动日志，确保无错误
echo.
echo ════════════════════════════════════════════════════════════
