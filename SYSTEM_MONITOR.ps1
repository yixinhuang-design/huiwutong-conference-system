# =============================================================================
# 系统运行状态监控脚本 - 实时检查所有关键服务
# =============================================================================

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   多租户智能排座系统 - 服务监控仪表板                       ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$script:monitoringActive = $true
$refreshInterval = 5  # 秒数

function Get-ServiceStatus {
    param([int]$Port, [string]$ServiceName)
    
    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue -WarningAction SilentlyContinue
        
        if ($connection) {
            return @{
                Status = "✅ 运行中"
                Port = $Port
                Process = "PID: {0}" -f $connection.OwningProcess
                State = $connection.State
                Color = "Green"
            }
        } else {
            return @{
                Status = "❌ 未运行"
                Port = $Port
                Process = "N/A"
                State = "N/A"
                Color = "Red"
            }
        }
    } catch {
        return @{
            Status = "⚠️ 检查失败"
            Port = $Port
            Process = "错误"
            State = "N/A"
            Color = "Yellow"
        }
    }
}

function Test-ServiceHealth {
    param([string]$Url, [string]$ServiceName)
    
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 3 -WarningAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            return @{ 
                Status = "✅ 健康"
                StatusCode = $response.StatusCode
                Color = "Green"
            }
        } else {
            return @{
                Status = "⚠️ 异常"
                StatusCode = $response.StatusCode
                Color = "Yellow"
            }
        }
    } catch {
        return @{
            Status = "❌ 无响应"
            StatusCode = "N/A"
            Color = "Red"
        }
    }
}

function Display-Dashboard {
    Clear-Host
    
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║   系统服务监控仪表板                                         ║" -ForegroundColor Cyan
    Write-Host "║   更新时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')       ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    
    # 前端服务 (8000)
    Write-Host "📱 前端HTTP服务" -ForegroundColor Cyan
    Write-Host "─────────────────────────────────────────────────────" -ForegroundColor Gray
    
    $frontend = Get-ServiceStatus -Port 8000 -ServiceName "前端服务"
    Write-Host ("  端口状态: {0}" -f $frontend.Status) -ForegroundColor $frontend.Color
    Write-Host ("  监听端口: {0}" -f $frontend.Port)
    Write-Host ("  进程信息: {0}" -f $frontend.Process)
    Write-Host ("  连接状态: {0}" -f $frontend.State)
    
    if ($frontend.Color -eq "Green") {
        $frontendHealth = Test-ServiceHealth -Url "http://localhost:8000" -ServiceName "前端"
        Write-Host ("  健康检查: {0}" -f $frontendHealth.Status) -ForegroundColor $frontendHealth.Color
    }
    Write-Host ""
    
    # 排座后端服务 (8086)
    Write-Host "🎪 排座后端服务" -ForegroundColor Cyan
    Write-Host "─────────────────────────────────────────────────────" -ForegroundColor Gray
    
    $seating = Get-ServiceStatus -Port 8086 -ServiceName "排座服务"
    Write-Host ("  端口状态: {0}" -f $seating.Status) -ForegroundColor $seating.Color
    Write-Host ("  监听端口: {0}" -f $seating.Port)
    Write-Host ("  进程信息: {0}" -f $seating.Process)
    Write-Host ("  连接状态: {0}" -f $seating.State)
    
    if ($seating.Color -eq "Green") {
        $seatingHealth = Test-ServiceHealth -Url "http://localhost:8086/actuator/health" -ServiceName "排座"
        Write-Host ("  健康检查: {0}" -f $seatingHealth.Status) -ForegroundColor $seatingHealth.Color
    }
    Write-Host ""
    
    # 数据库 (3308)
    Write-Host "💾 MySQL数据库" -ForegroundColor Cyan
    Write-Host "─────────────────────────────────────────────────────" -ForegroundColor Gray
    
    $database = Get-ServiceStatus -Port 3308 -ServiceName "数据库"
    Write-Host ("  端口状态: {0}" -f $database.Status) -ForegroundColor $database.Color
    Write-Host ("  监听端口: {0}" -f $database.Port)
    Write-Host ("  进程信息: {0}" -f $database.Process)
    Write-Host ("  连接状态: {0}" -f $database.State)
    Write-Host ""
    
    # 系统总体状态
    Write-Host "📊 系统总体状态" -ForegroundColor Cyan
    Write-Host "─────────────────────────────────────────────────────" -ForegroundColor Gray
    
    $allGreen = @($frontend, $seating, $database) | Where-Object { $_.Color -eq "Green" }
    
    if ($allGreen.Count -eq 3) {
        Write-Host "  整体状态: 🟢 全部正常" -ForegroundColor Green
        Write-Host "  运行服务: 3/3" -ForegroundColor Green
        Write-Host "  系统评分: ⭐⭐⭐⭐⭐ (5/5)" -ForegroundColor Green
    } elseif ($allGreen.Count -eq 2) {
        Write-Host "  整体状态: 🟡 部分故障" -ForegroundColor Yellow
        Write-Host "  运行服务: 2/3" -ForegroundColor Yellow
        Write-Host "  系统评分: ⭐⭐⭐ (3/5)" -ForegroundColor Yellow
    } else {
        Write-Host "  整体状态: 🔴 多个故障" -ForegroundColor Red
        Write-Host "  运行服务: $($allGreen.Count)/3" -ForegroundColor Red
        Write-Host "  系统评分: ⭐ (1/5)" -ForegroundColor Red
    }
    Write-Host ""
    
    # 快速命令
    Write-Host "⌨️ 快速命令" -ForegroundColor Cyan
    Write-Host "─────────────────────────────────────────────────────" -ForegroundColor Gray
    Write-Host "  R: 刷新监控        S: 启动所有服务" -ForegroundColor Gray
    Write-Host "  T: 运行测试        L: 查看日志" -ForegroundColor Gray
    Write-Host "  Q: 退出监控" -ForegroundColor Gray
    Write-Host ""
}

function Start-AllServices {
    Write-Host "🚀 启动所有服务..." -ForegroundColor Yellow
    
    # 启动排座服务 (如果未运行)
    $seatingCheck = Get-NetTCPConnection -LocalPort 8086 -ErrorAction SilentlyContinue
    if (-not $seatingCheck) {
        Write-Host "  启动排座服务中..." -ForegroundColor Yellow
        $seatingPath = "backend\conference-backend\conference-seating\target\conference-seating-1.0.0.jar"
        if (Test-Path $seatingPath) {
            Start-Process -FilePath "java.exe" `
                          -ArgumentList "-jar", $seatingPath, "--spring.profiles.active=local" `
                          -WindowStyle Hidden
            Write-Host "  ✅ 排座服务启动命令已发送" -ForegroundColor Green
        } else {
            Write-Host "  ❌ 未找到排座服务JAR文件" -ForegroundColor Red
        }
    } else {
        Write-Host "  ℹ️ 排座服务已运行" -ForegroundColor Cyan
    }
    
    # 启动前端服务 (如果未运行)
    $frontendCheck = Get-NetTCPConnection -LocalPort 8000 -ErrorAction SilentlyContinue
    if (-not $frontendCheck) {
        Write-Host "  启动前端服务中..." -ForegroundColor Yellow
        $command = "cd '$($PWD.Path)\admin-pc\conference-admin-pc'; npx http-server -p 8000 --cors"
        Start-Process -FilePath "pwsh.exe" `
                      -ArgumentList "-NoExit", "-Command", $command `
                      -WindowStyle Normal
        Write-Host "  ✅ 前端服务启动命令已发送" -ForegroundColor Green
    } else {
        Write-Host "  ℹ️ 前端服务已运行" -ForegroundColor Cyan
    }
    
    Write-Host ""
    Write-Host "  ⏳ 等待服务启动（5-10秒）..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    Write-Host ""
}

function Run-Tests {
    Write-Host "🧪 运行系统测试..." -ForegroundColor Yellow
    Write-Host ""
    
    # 测试前端
    Write-Host "  测试1: 前端连接性" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8000/pages/login.html" -TimeoutSec 3 -WarningAction SilentlyContinue
        Write-Host "    ✅ 前端响应正常 (HTTP $($response.StatusCode))" -ForegroundColor Green
    } catch {
        Write-Host "    ❌ 前端无响应" -ForegroundColor Red
    }
    
    # 测试API
    Write-Host "  测试2: 排座API连接性" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -TimeoutSec 3 -WarningAction SilentlyContinue
        Write-Host "    ✅ API响应正常 (HTTP $($response.StatusCode))" -ForegroundColor Green
    } catch {
        Write-Host "    ❌ API无响应" -ForegroundColor Red
    }
    
    # 测试数据库
    Write-Host "  测试3: 数据库连接性" -ForegroundColor Cyan
    $dbConnection = Get-NetTCPConnection -LocalPort 3308 -ErrorAction SilentlyContinue
    if ($dbConnection) {
        Write-Host "    ✅ 数据库可访问 (Port 3308)" -ForegroundColor Green
    } else {
        Write-Host "    ❌ 数据库不可达" -ForegroundColor Red
    }
    
    # 测试CORS
    Write-Host "  测试4: CORS支持" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8086/api/seating/venues/123" -TimeoutSec 3 -Method OPTIONS -WarningAction SilentlyContinue
        if ($response.Headers['Access-Control-Allow-Origin']) {
            Write-Host "    ✅ CORS已启用" -ForegroundColor Green
        } else {
            Write-Host "    ⚠️ CORS响应头不完整" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "    ❌ CORS检查失败" -ForegroundColor Red
    }
    
    Write-Host ""
}

# 主循环
while ($script:monitoringActive) {
    Display-Dashboard
    
    Write-Host "按键选择操作 (R=刷新, S=启动, T=测试, L=日志, Q=退出): " -ForegroundColor Yellow -NoNewline
    $key = [System.Console]::ReadKey($true).KeyChar
    
    switch ([char]$key) {
        'R' { continue }
        'r' { continue }
        
        'S' { 
            Start-AllServices
            continue
        }
        's' { 
            Start-AllServices
            continue
        }
        
        'T' { 
            Run-Tests
            Read-Host "按Enter继续"
            continue
        }
        't' { 
            Run-Tests
            Read-Host "按Enter继续"
            continue
        }
        
        'L' {
            Write-Host ""
            Write-Host "📋 系统日志位置:" -ForegroundColor Cyan
            Write-Host "  排座服务: 控制台输出" -ForegroundColor Yellow
            Write-Host "  前端服务: 控制台输出" -ForegroundColor Yellow
            Write-Host "  数据库: 系统日志" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "💡 查看排座服务日志:" -ForegroundColor Cyan
            Write-Host "  查找包含进程ID的powershell窗口，查看输出" -ForegroundColor Yellow
            Write-Host ""
            Read-Host "按Enter继续"
            continue
        }
        'l' {
            Write-Host ""
            Write-Host "📋 系统日志位置:" -ForegroundColor Cyan
            Write-Host "  排座服务: 控制台输出" -ForegroundColor Yellow
            Write-Host "  前端服务: 控制台输出" -ForegroundColor Yellow
            Write-Host "  数据库: 系统日志" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "💡 查看排座服务日志:" -ForegroundColor Cyan
            Write-Host "  查找包含进程ID的powershell窗口，查看输出" -ForegroundColor Yellow
            Write-Host ""
            Read-Host "按Enter继续"
            continue
        }
        
        'Q' {
            Write-Host ""
            Write-Host "👋 退出监控" -ForegroundColor Yellow
            $script:monitoringActive = $false
            break
        }
        'q' {
            Write-Host ""
            Write-Host "👋 退出监控" -ForegroundColor Yellow
            $script:monitoringActive = $false
            break
        }
        
        default {
            Write-Host ""
            Write-Host "❓ 无效的命令，请选择: R/S/T/L/Q" -ForegroundColor Red
            Write-Host ""
            Start-Sleep -Seconds 2
            continue
        }
    }
}

Write-Host ""
Write-Host "👋 感谢使用系统监控工具" -ForegroundColor Cyan
