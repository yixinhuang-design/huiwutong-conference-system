# PowerShell 脚本：安装 Redis 和 Nacos

Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   智能会务系统 - 基础设施自动化安装脚本              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "⚠️  需要管理员权限！请以管理员身份运行此脚本。" -ForegroundColor Yellow
    exit
}

# ============================================================
# 1. 安装 Chocolatey（如果未安装）
# ============================================================

Write-Host "[1] 检查 Chocolatey..." -ForegroundColor Green

if (!(Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "   安装中..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.ServiceClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    Write-Host "   ✅ Chocolatey 已安装" -ForegroundColor Green
} else {
    Write-Host "   ✅ Chocolatey 已存在" -ForegroundColor Green
}

# ============================================================
# 2. 安装 Redis
# ============================================================

Write-Host ""
Write-Host "[2] 检查 Redis..." -ForegroundColor Green

if (!(Get-Command redis-server -ErrorAction SilentlyContinue)) {
    Write-Host "   安装中..." -ForegroundColor Yellow
    choco install redis -y
    Write-Host "   ✅ Redis 已安装" -ForegroundColor Green
} else {
    Write-Host "   ✅ Redis 已存在" -ForegroundColor Green
}

# ============================================================
# 3. 下载 Nacos
# ============================================================

Write-Host ""
Write-Host "[3] 检查 Nacos..." -ForegroundColor Green

$nacosDir = "G:\huiwutong新版合集\nacos"
$nacosZip = "$nacosDir\nacos.zip"

if (!(Test-Path "$nacosDir\bin\startup.cmd")) {
    Write-Host "   下载中..." -ForegroundColor Yellow
    
    if (!(Test-Path $nacosDir)) {
        New-Item -ItemType Directory -Path $nacosDir -Force | Out-Null
    }
    
    $url = "https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $nacosZip -TimeoutSec 300
        
        Write-Host "   解压中..." -ForegroundColor Yellow
        Expand-Archive -Path $nacosZip -DestinationPath $nacosDir -Force
        Remove-Item $nacosZip -Force
        
        Write-Host "   ✅ Nacos 已下载并解压" -ForegroundColor Green
    } catch {
        Write-Host "   ❌ 下载失败，请手动下载:" -ForegroundColor Red
        Write-Host "   https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip" -ForegroundColor Yellow
        Write-Host "   然后解压到: $nacosDir" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✅ Nacos 已存在" -ForegroundColor Green
}

# ============================================================
# 4. 启动服务
# ============================================================

Write-Host ""
Write-Host "[4] 启动服务..." -ForegroundColor Green
Write-Host ""

# 启动 Redis
Write-Host "   启动 Redis..." -ForegroundColor Yellow
Start-Process -FilePath "redis-server" -ArgumentList "--port 6379" -WindowStyle Minimized
Start-Sleep -Seconds 3
Write-Host "   ✅ Redis 已启动 (http://localhost:6379)" -ForegroundColor Green

# 启动 Nacos
Write-Host "   启动 Nacos..." -ForegroundColor Yellow
if (Test-Path "$nacosDir\bin\startup.cmd") {
    Push-Location "$nacosDir\bin"
    & cmd /c "startup.cmd -m standalone"
    Pop-Location
    Start-Sleep -Seconds 5
    Write-Host "   ✅ Nacos 已启动 (http://localhost:8848/nacos)" -ForegroundColor Green
    Write-Host "   📝 默认账号: nacos / nacos" -ForegroundColor Yellow
} else {
    Write-Host "   ⚠️  Nacos 未就绪，请手动启动" -ForegroundColor Yellow
}

# ============================================================
# 完成
# ============================================================

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║          基础设施安装和启动完成！                   ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

Write-Host "✅ 服务地址:" -ForegroundColor Green
Write-Host "   Redis:  http://localhost:6379" -ForegroundColor Cyan
Write-Host "   Nacos:  http://localhost:8848/nacos" -ForegroundColor Cyan
Write-Host ""

Write-Host "📝 下一步:" -ForegroundColor Yellow
Write-Host "   1. 在 Nacos 中创建命名空间和配置" -ForegroundColor Gray
Write-Host "   2. 修改后端服务配置使用 Nacos" -ForegroundColor Gray
Write-Host "   3. 启用 Redis 缓存" -ForegroundColor Gray
Write-Host ""
