@echo off
REM 智能会务系统后端服务启动脚本
REM 支持启动所有8个微服务

setlocal enabledelayedexpansion
set "BACKEND_DIR=%~dp0backend\conference-backend"
set "LOG_DIR=%BACKEND_DIR%\logs"

REM 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo.
echo ========================================================
echo 智能会务系统后端服务启动
echo ========================================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Java环境，请先安装JDK 21+
    pause
    exit /b 1
)

echo ✅ Java环境检查通过

REM 选择启动模式
echo.
echo 请选择启动模式:
echo.
echo 1. 启动排座服务 (conference-seating:8086)
echo 2. 启动报名服务 (conference-registration:8082)
echo 3. 启动会议服务 (conference-meeting:8083)
echo 4. 启动网关服务 (conference-gateway:8080)
echo 5. 启动所有服务
echo 6. 编译并打包所有模块
echo.
set /p choice="请输入选择 (1-6): "

if "%choice%"=="1" (
    call :start_service "conference-seating" "8086"
) else if "%choice%"=="2" (
    call :start_service "conference-registration" "8082"
) else if "%choice%"=="3" (
    call :start_service "conference-meeting" "8083"
) else if "%choice%"=="4" (
    call :start_service "conference-gateway" "8080"
) else if "%choice%"=="5" (
    call :start_all_services
) else if "%choice%"=="6" (
    call :build_all
) else (
    echo ❌ 无效选择
    exit /b 1
)

goto :eof

:start_service
set "SERVICE=%~1"
set "PORT=%~2"
set "JAR_FILE=%BACKEND_DIR%\%SERVICE%\target\%SERVICE%-1.0.0.jar"

if not exist "%JAR_FILE%" (
    echo ❌ 错误: 未找到 %SERVICE% 的JAR文件
    echo 尝试重新编译...
    cd /d "%BACKEND_DIR%"
    call mvn clean package -pl %SERVICE% -DskipTests
)

echo.
echo ========================================================
echo 启动服务: %SERVICE% (端口: %PORT%)
echo ========================================================
echo.

java -jar "%JAR_FILE%" --spring.profiles.active=prod --server.port=%PORT%

goto :eof

:start_all_services
echo.
echo ========================================================
echo 启动所有8个微服务
echo ========================================================
echo.

cd /d "%BACKEND_DIR%"

set "services=conference-gateway conference-auth conference-registration conference-meeting conference-notification conference-collaboration conference-seating"

for %%s in (%services%) do (
    set "jar=%BACKEND_DIR%\%%s\target\%%s-1.0.0.jar"
    if exist "!jar!" (
        echo ✅ 启动 %%s...
        start "%%s Service" java -jar "!jar!"
    ) else (
        echo ⚠️ 跳过 %%s (未找到JAR文件)
    )
)

echo.
echo ✅ 所有服务已启动
echo.
echo 服务访问地址:
echo   - API网关: http://localhost:8080
echo   - 排座服务: http://localhost:8086
echo   - 报名服务: http://localhost:8082
echo   - 会议服务: http://localhost:8083
echo.

goto :eof

:build_all
echo.
echo ========================================================
echo 编译打包所有模块
echo ========================================================
echo.

cd /d "%BACKEND_DIR%"

echo 正在执行: mvn clean package -DskipTests
call mvn clean package -DskipTests

if errorlevel 0 (
    echo.
    echo ✅ 编译完成!
    echo.
    echo 生成的JAR文件:
    for /r "%BACKEND_DIR%" %%f in (*-1.0.0.jar) do (
        echo   - %%~nf
    )
) else (
    echo.
    echo ❌ 编译失败!
)

goto :eof
