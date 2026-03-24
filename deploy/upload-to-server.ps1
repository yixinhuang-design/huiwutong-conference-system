# ================================================================
# 智能会议系统 - Windows端打包上传脚本
# 在本地Windows机器执行，将构建产物上传到服务器
# 用法: powershell -ExecutionPolicy Bypass -File upload-to-server.ps1
# ================================================================

$ErrorActionPreference = "Stop"

# ===== 配置 =====
$SERVER_IP = "39.103.85.255"
$SERVER_USER = "root"
$PROJECT_ROOT = "G:\huiwutong新版合集"
$BACKEND_ROOT = "$PROJECT_ROOT\backend\conference-backend"
$FRONTEND_ROOT = "$PROJECT_ROOT\admin-pc\conference-admin-pc"
$DEPLOY_ROOT = "$PROJECT_ROOT\deploy"
$REMOTE_HOME = "/opt/conference"

# 服务列表
$SERVICES = @(
    "conference-gateway",
    "conference-auth",
    "conference-registration",
    "conference-notification",
    "conference-meeting",
    "conference-ai",
    "conference-seating",
    "conference-data",
    "conference-collaboration"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  智能会议系统 - 打包上传工具" -ForegroundColor Cyan
Write-Host "  目标服务器: $SERVER_IP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ===== 步骤1: Maven构建 =====
Write-Host "[步骤1] Maven全量构建..." -ForegroundColor Yellow
Set-Location $BACKEND_ROOT
mvn clean package -DskipTests --no-transfer-progress 2>&1 | Select-Object -Last 20
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven构建失败!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Maven构建成功" -ForegroundColor Green

# ===== 步骤2: 创建上传目录 =====
Write-Host "`n[步骤2] 打包部署文件..." -ForegroundColor Yellow
$UPLOAD_DIR = "$PROJECT_ROOT\deploy\upload-package"
if (Test-Path $UPLOAD_DIR) { Remove-Item $UPLOAD_DIR -Recurse -Force }
New-Item -ItemType Directory -Path "$UPLOAD_DIR\jars" -Force | Out-Null
New-Item -ItemType Directory -Path "$UPLOAD_DIR\config" -Force | Out-Null
New-Item -ItemType Directory -Path "$UPLOAD_DIR\sql" -Force | Out-Null
New-Item -ItemType Directory -Path "$UPLOAD_DIR\scripts" -Force | Out-Null
New-Item -ItemType Directory -Path "$UPLOAD_DIR\frontend" -Force | Out-Null

# 复制JAR文件
foreach ($svc in $SERVICES) {
    $jar = "$BACKEND_ROOT\$svc\target\$svc-1.0.0.jar"
    if (Test-Path $jar) {
        Copy-Item $jar "$UPLOAD_DIR\jars\"
        $size = [math]::Round((Get-Item $jar).Length / 1MB, 1)
        Write-Host "  ✅ $svc-1.0.0.jar (${size}MB)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 缺少: $svc-1.0.0.jar" -ForegroundColor Red
    }
}

# 复制SQL文件
Copy-Item "$DEPLOY_ROOT\sql\*" "$UPLOAD_DIR\sql\" -Recurse -ErrorAction SilentlyContinue

# 复制各服务的migration SQL
foreach ($svc in $SERVICES) {
    $migDir = "$BACKEND_ROOT\$svc\src\main\resources\db\migration"
    if (Test-Path $migDir) {
        # 确定目标数据库名
        $dbName = switch ($svc) {
            "conference-auth" { "conference_auth" }
            "conference-registration" { "conference_registration" }
            "conference-meeting" { "conference_registration" }
            "conference-notification" { "conference_notification" }
            "conference-seating" { "conference_seating" }
            "conference-ai" { "conference_db" }
            "conference-data" { "conference_db" }
            "conference-collaboration" { "conference_collaboration" }
            default { "" }
        }
        if ($dbName) {
            $destDir = "$UPLOAD_DIR\sql\migrations\$dbName"
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            Copy-Item "$migDir\*.sql" $destDir -ErrorAction SilentlyContinue
            Write-Host "  📄 $svc migrations -> $dbName" -ForegroundColor Gray
        }
    }
}

# 复制Nginx配置和部署脚本
Copy-Item "$DEPLOY_ROOT\nginx\conference.conf" "$UPLOAD_DIR\config\" -ErrorAction SilentlyContinue
Copy-Item "$DEPLOY_ROOT\deploy.sh" "$UPLOAD_DIR\scripts\"
Copy-Item "$DEPLOY_ROOT\service-ctl.sh" "$UPLOAD_DIR\scripts\"

# 复制前端文件（排除不需要的文件）
$frontendExclude = @("*.md", "*.backup", "*.corrupted*", "node_modules", ".git", "test-*", "docs", "文档")
$frontendItems = Get-ChildItem $FRONTEND_ROOT -Exclude $frontendExclude
foreach ($item in $frontendItems) {
    if ($item.Name -notmatch '(test-|diagnose_|临时|关闭弹窗|强制关闭|捕获渲染|深度数据|深度检查|页面诊断|启动测试)') {
        Copy-Item $item.FullName "$UPLOAD_DIR\frontend\$($item.Name)" -Recurse -ErrorAction SilentlyContinue
    }
}
Write-Host "  ✅ 前端文件已复制" -ForegroundColor Green

# ===== 步骤3: 压缩 =====
Write-Host "`n[步骤3] 压缩打包..." -ForegroundColor Yellow
$ZIP_FILE = "$PROJECT_ROOT\deploy\conference-deploy-package.tar.gz"
if (Test-Path $ZIP_FILE) { Remove-Item $ZIP_FILE -Force }

# 使用tar压缩（Git Bash的tar）
$tarExe = "C:\Program Files\Git\usr\bin\tar.exe"
if (-not (Test-Path $tarExe)) {
    $tarExe = (Get-Command tar -ErrorAction SilentlyContinue).Source
}
if ($tarExe) {
    Set-Location "$UPLOAD_DIR\.."
    & $tarExe czf $ZIP_FILE -C "$UPLOAD_DIR\.." "upload-package"
    $zipSize = [math]::Round((Get-Item $ZIP_FILE).Length / 1MB, 1)
    Write-Host "✅ 压缩完成: conference-deploy-package.tar.gz (${zipSize}MB)" -ForegroundColor Green
} else {
    Write-Host "⚠️ tar未找到，使用zip替代..." -ForegroundColor Yellow
    $ZIP_FILE = "$PROJECT_ROOT\deploy\conference-deploy-package.zip"
    Compress-Archive -Path "$UPLOAD_DIR\*" -DestinationPath $ZIP_FILE -Force
    $zipSize = [math]::Round((Get-Item $ZIP_FILE).Length / 1MB, 1)
    Write-Host "✅ 压缩完成: conference-deploy-package.zip (${zipSize}MB)" -ForegroundColor Green
}

# ===== 步骤4: 上传到服务器 =====
Write-Host "`n[步骤4] 上传到服务器 ($SERVER_IP)..." -ForegroundColor Yellow
Write-Host "使用SCP上传，请输入服务器root密码:" -ForegroundColor Yellow

# 检查scp是否可用
if (Get-Command scp -ErrorAction SilentlyContinue) {
    # 上传压缩包
    scp $ZIP_FILE "${SERVER_USER}@${SERVER_IP}:/tmp/"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 上传成功!" -ForegroundColor Green
        
        # SSH执行远程部署
        Write-Host "`n[步骤5] 远程解压和部署..." -ForegroundColor Yellow
        $REMOTE_CMD = @"
mkdir -p $REMOTE_HOME
cd /tmp
if [ -f conference-deploy-package.tar.gz ]; then
    tar xzf conference-deploy-package.tar.gz
    cp -r upload-package/jars/* $REMOTE_HOME/jars/ 2>/dev/null || true
    cp -r upload-package/sql/* $REMOTE_HOME/sql/ 2>/dev/null || true
    cp -r upload-package/config/* $REMOTE_HOME/config/ 2>/dev/null || true
    cp -r upload-package/scripts/* $REMOTE_HOME/scripts/ 2>/dev/null || true
    cp -r upload-package/frontend/* /www/wwwroot/conference-admin/ 2>/dev/null || true
    chmod +x $REMOTE_HOME/scripts/*.sh
    rm -rf upload-package conference-deploy-package.tar.gz
elif [ -f conference-deploy-package.zip ]; then
    unzip -o conference-deploy-package.zip -d upload-package
    cp -r upload-package/jars/* $REMOTE_HOME/jars/ 2>/dev/null || true
    cp -r upload-package/sql/* $REMOTE_HOME/sql/ 2>/dev/null || true
    cp -r upload-package/config/* $REMOTE_HOME/config/ 2>/dev/null || true
    cp -r upload-package/scripts/* $REMOTE_HOME/scripts/ 2>/dev/null || true
    cp -r upload-package/frontend/* /www/wwwroot/conference-admin/ 2>/dev/null || true
    chmod +x $REMOTE_HOME/scripts/*.sh
    rm -rf upload-package conference-deploy-package.zip
fi
echo 'Files deployed successfully!'
ls -la $REMOTE_HOME/jars/*.jar 2>/dev/null | awk '{print \$5, \$9}' | while read size name; do printf "  %-50s %sMB\n" "\$name" \$(echo "scale=1; \$size/1048576" | bc); done
"@
        ssh "${SERVER_USER}@${SERVER_IP}" $REMOTE_CMD
        
        Write-Host "`n✅ 部署文件已上传到服务器" -ForegroundColor Green
        Write-Host "下一步: SSH到服务器执行 'bash $REMOTE_HOME/scripts/deploy.sh'" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 上传失败" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️ SCP未找到" -ForegroundColor Yellow
    Write-Host "请手动上传: $ZIP_FILE" -ForegroundColor Yellow
    Write-Host "上传到服务器: scp $ZIP_FILE root@${SERVER_IP}:/tmp/" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  部署包路径: $ZIP_FILE" -ForegroundColor Cyan
Write-Host "  上传后执行: bash /opt/conference/scripts/deploy.sh" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
