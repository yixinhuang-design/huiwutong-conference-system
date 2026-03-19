# =============================================================================
# 多租户排座系统 - 一键启动所有服务
# =============================================================================
# 此脚本启动:
# 1. 排座后端服务 (Port 8086)
# 2. 前端HTTP服务器 (Port 8000)
# 3. 数据库验证 (MySQL Port 3308)
# =============================================================================

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  多租户智能排座系统 - 完整启动脚本" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# 颜色定义
$GREEN = "Green"
$RED = "Red"
$YELLOW = "Yellow"
$CYAN = "Cyan"

# 1. 验证数据库连接
Write-Host "[1/3] 检查数据库连接..." -ForegroundColor $CYAN
$dbConnection = Get-NetTCPConnection -LocalPort 3308 -ErrorAction SilentlyContinue
if ($dbConnection) {
    Write-Host "  ✅ MySQL 数据库运行正常 (Port 3308)" -ForegroundColor $GREEN
} else {
    Write-Host "  ⚠️ MySQL 数据库未检测到，请确保已启动:" -ForegroundColor $YELLOW
    Write-Host "     Windows: net start MySQL80" -ForegroundColor $YELLOW
    Write-Host "     Docker: docker start mysql-container" -ForegroundColor $YELLOW
    Write-Host ""
}

# 2. 启动排座后端服务
Write-Host ""
Write-Host "[2/3] 启动排座后端服务 (Port 8086)..." -ForegroundColor $CYAN

# 检查是否已运行
$seatingService = Get-NetTCPConnection -LocalPort 8086 -ErrorAction SilentlyContinue
if ($seatingService) {
    Write-Host "  ✅ 排座服务已在运行 (Port 8086)" -ForegroundColor $GREEN
} else {
    Write-Host "  🔄 启动排座服务中..." -ForegroundColor $YELLOW
    $seatingPath = "backend\conference-backend\conference-seating\target\conference-seating-1.0.0.jar"
    
    if (Test-Path $seatingPath) {
        Start-Process -FilePath "java.exe" `
                      -ArgumentList "-jar", $seatingPath, "--spring.profiles.active=local" `
                      -WindowStyle Normal `
                      -NoNewWindow
        Write-Host "  ⏳ 排座服务启动中，请稍候5-10秒..." -ForegroundColor $YELLOW
        Start-Sleep -Seconds 5
        
        # 再次检查
        $seatingService = Get-NetTCPConnection -LocalPort 8086 -ErrorAction SilentlyContinue
        if ($seatingService) {
            Write-Host "  ✅ 排座服务启动成功 (Port 8086)" -ForegroundColor $GREEN
        } else {
            Write-Host "  ❌ 排座服务启动失败，请检查日志" -ForegroundColor $RED
        }
    } else {
        Write-Host "  ❌ 找不到排座服务JAR文件: $seatingPath" -ForegroundColor $RED
        Write-Host "     请确保已执行: mvn clean package -pl conference-seating" -ForegroundColor $RED
    }
}

# 3. 启动前端HTTP服务
Write-Host ""
Write-Host "[3/3] 启动前端HTTP服务 (Port 8000)..." -ForegroundColor $CYAN

$frontendPath = "admin-pc\conference-admin-pc"

if (Test-Path $frontendPath) {
    Write-Host "  🔄 启动前端服务中..." -ForegroundColor $YELLOW
    
    # 在新窗口中启动http-server
    $command = "cd '$($PWD.Path)\$frontendPath'; npx http-server -p 8000 --cors"
    Start-Process -FilePath "pwsh.exe" `
                  -ArgumentList "-NoExit", "-Command", $command `
                  -WindowStyle Normal
    
    Write-Host "  ⏳ 前端服务启动中，请稍候..." -ForegroundColor $YELLOW
    Start-Sleep -Seconds 3
    
    $frontendService = Get-NetTCPConnection -LocalPort 8000 -ErrorAction SilentlyContinue
    if ($frontendService) {
        Write-Host "  ✅ 前端服务启动成功 (Port 8000)" -ForegroundColor $GREEN
    } else {
        Write-Host "  ⚠️ 前端服务可能正在启动，请查看新打开的窗口" -ForegroundColor $YELLOW
    }
} else {
    Write-Host "  ❌ 找不到前端目录: $frontendPath" -ForegroundColor $RED
}

# 最终总结
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor $CYAN
Write-Host "  启动完成！" -ForegroundColor $GREEN
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor $CYAN
Write-Host ""
Write-Host "📋 服务地址:" -ForegroundColor $CYAN
Write-Host "  前端:     http://localhost:8000" -ForegroundColor $GREEN
Write-Host "  登录页:   http://localhost:8000/pages/login.html" -ForegroundColor $GREEN
Write-Host "  排座:     http://localhost:8086" -ForegroundColor $GREEN
Write-Host "  API:      http://localhost:8086/api/seating/..." -ForegroundColor $GREEN
Write-Host ""
Write-Host "🔍 验证方法:" -ForegroundColor $CYAN
Write-Host "  1. 打开浏览器访问 http://localhost:8000" -ForegroundColor $YELLOW
Write-Host "  2. 打开DevTools (F12) 查看Network标签" -ForegroundColor $YELLOW
Write-Host "  3. 确保API请求到 http://localhost:8086 而不是8000" -ForegroundColor $YELLOW
Write-Host ""
Write-Host "💡 提示:" -ForegroundColor $CYAN
Write-Host "  - 关闭任意窗口时会停止对应服务" -ForegroundColor $YELLOW
Write-Host "  - 使用 Ctrl+C 停止服务" -ForegroundColor $YELLOW
Write-Host "  - 查看新打开窗口的日志了解详细信息" -ForegroundColor $YELLOW
Write-Host ""

# 保持脚本窗口打开
Read-Host "按Enter键查看端口监听状态"

# 显示当前监听端口
Write-Host ""
Write-Host "当前监听的端口:" -ForegroundColor $CYAN
Get-NetTCPConnection -State Listen | Where-Object {$_.LocalPort -in (8000, 8086, 3308)} | Select-Object LocalPort, OwningProcess | Format-Table

Read-Host "按Enter键退出"
