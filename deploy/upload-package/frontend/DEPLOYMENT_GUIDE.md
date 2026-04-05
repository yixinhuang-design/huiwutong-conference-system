# 管理后台登录系统部署启动指南

**版本**: v1.0.0  
**最后更新**: 2026-02-26  
**适用对象**: 开发人员、测试人员、运维人员

---

## 📋 目录

1. [系统需求](#系统需求)
2. [快速启动](#快速启动)
3. [环境配置](#环境配置)
4. [后端服务部署](#后端服务部署)
5. [前端部署](#前端部署)
6. [测试验证](#测试验证)
7. [常见问题](#常见问题)
8. [故障排查](#故障排查)

---

## 系统需求

### 硬件要求
```
最低配置:
  - CPU: 双核 2.0 GHz
  - 内存: 4GB
  - 硬盘: 20GB可用空间

推荐配置:
  - CPU: 四核 2.5 GHz+
  - 内存: 8GB+
  - 硬盘: 50GB+ SSD
```

### 软件要求

#### 后端环境
| 软件 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | OpenJDK或Oracle JDK |
| Maven | 3.8.1+ | 构建工具 |
| MySQL | 5.7+ 或 8.0+ | 数据库 |

#### 前端环境
| 软件 | 版本 | 说明 |
|------|------|------|
| Node.js | 16+ | 可选，用于本地开发 |
| npm | 7+ | 可选，用于包管理 |
| 浏览器 | 最新版 | Chrome/Firefox/Safari/Edge |

---

## 快速启动

### 一键启动脚本 (Windows)

创建文件 `start-all.bat`:

```batch
@echo off
chcp 65001 > nul
echo 正在启动管理后台登录系统...
echo.

REM 检查Java安装
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java未安装或未配置到系统路径
    echo 请下载安装Java 17及以上版本
    pause
    exit /b 1
)
echo ✅ Java环境检查通过

REM 检查Maven安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven未安装或未配置到系统路径
    echo 请下载安装Maven 3.8.1及以上版本
    pause
    exit /b 1
)
echo ✅ Maven环境检查通过

REM 启动MySQL
echo.
echo 正在启动MySQL数据库...
REM 根据实际安装路径修改以下行
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysqld.exe --console
REM 或者使用Windows服务:
REM net start MySQL80

REM 等待MySQL启动
timeout /t 5

REM 启动认证服务
echo.
echo 正在启动认证服务 (port 8081)...
cd "G:\huiwutong新版合集\backend\conference-backend"
call mvn -s maven-settings.xml -f conference-auth\pom.xml spring-boot:run

echo.
echo ✅ 系统启动完成！
echo 登录页地址: file:///G:/huiwutong新版合集/admin-pc/conference-admin-pc/pages/login.html
echo 后端地址: http://localhost:8081
pause
```

### 一键启动脚本 (Linux/macOS)

创建文件 `start-all.sh`:

```bash
#!/bin/bash

echo "正在启动管理后台登录系统..."
echo ""

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装"
    echo "请使用以下命令安装Java 17:"
    echo "  Ubuntu: sudo apt-get install openjdk-17-jdk"
    echo "  macOS: brew install openjdk@17"
    exit 1
fi
echo "✅ Java环境检查通过: $(java -version 2>&1 | head -n 1)"

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装"
    echo "请使用以下命令安装Maven:"
    echo "  Ubuntu: sudo apt-get install maven"
    echo "  macOS: brew install maven"
    exit 1
fi
echo "✅ Maven环境检查通过: $(mvn -v | head -n 1)"

# 启动MySQL
echo ""
echo "正在启动MySQL数据库..."
# 根据实际安装方式修改:
# docker run -d -p 3308:3306 -e MYSQL_ROOT_PASSWORD=Hnhx@123 mysql:8.0
# 或
# brew services start mysql
# 或
# sudo service mysql start

sleep 5

# 启动认证服务
echo ""
echo "正在启动认证服务 (port 8081)..."
cd "G/huiwutong新版合集/backend/conference-backend"
mvn -s maven-settings.xml -f conference-auth/pom.xml spring-boot:run

echo ""
echo "✅ 系统启动完成！"
echo "登录页地址: file:///G/huiwutong新版合集/admin-pc/conference-admin-pc/pages/login.html"
echo "后端地址: http://localhost:8081"
```

---

## 环境配置

### 1. Java环境配置

#### Windows
```powershell
# 检查Java版本
java -version

# 如果未安装，下载安装:
# https://www.oracle.com/java/technologies/downloads/#java17
# 或使用开源OpenJDK:
# https://adoptium.net/

# 配置JAVA_HOME环境变量
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.1"
$env:Path += ";$env:JAVA_HOME\bin"

# 验证
java -version
javac -version
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# CentOS/RHEL
sudo dnf install java-17-openjdk java-17-openjdk-devel

# 验证
java -version
```

#### macOS
```bash
# 使用Homebrew
brew install openjdk@17

# 创建符号链接
sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# 验证
java -version
```

### 2. Maven配置

#### Windows
```powershell
# 下载Maven: https://maven.apache.org/download.cgi
# 解压到指定目录，假设: C:\tools\apache-maven-3.8.1

$env:M2_HOME = "C:\tools\apache-maven-3.8.1"
$env:Path += ";$env:M2_HOME\bin"

# 验证
mvn -version
```

#### Linux/macOS
```bash
# Ubuntu/Debian
sudo apt-get install maven

# CentOS/RHEL
sudo dnf install maven

# macOS
brew install maven

# 验证
mvn -version
```

### 3. MySQL配置

#### Windows (本地安装)
```powershell
# 下载: https://dev.mysql.com/downloads/mysql/

# 验证服务状态
Get-Service | Where-Object {$_.Name -like "*MySQL*"}

# 启动MySQL服务
Start-Service MySQL80  # 或相应版本号

# 连接数据库
mysql -h localhost -P 3308 -u root -p
# 输入密码: Hnhx@123
```

#### Docker方式（推荐）
```bash
# 创建Docker网络
docker network create confapp-net

# 启动MySQL容器
docker run -d \
  --name mysql-server \
  --network confapp-net \
  -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=Hnhx@123 \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0

# 验证
docker ps | grep mysql-server
docker logs mysql-server
```

---

## 后端服务部署

### 步骤1: 初始化数据库

```bash
# 连接MySQL
mysql -h localhost -P 3308 -u root -pHnhx@123

# 在MySQL命令行中执行:
CREATE DATABASE conference_auth;
USE conference_auth;

# 创建所需的表（参考现有的建表脚本）
# source /path/to/init-db.sql
```

### 步骤2: 修改应用配置

编辑 `conference-auth/src/main/resources/application.yml`:

```yaml
# 修改以下部分:

spring:
  datasource:
    url: jdbc:mysql://localhost:3308/conference_auth?serverTimezone=UTC
    username: root
    password: Hnhx@123  # 根据实际修改
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8081  # 确保端口正确

# 禁用Nacos（开发环境）
spring:
  cloud:
    nacos:
      config:
        enabled: false
      discovery:
        enabled: false
```

### 步骤3: 构建应用

```bash
cd "G:\huiwutong新版合集\backend\conference-backend"

# 使用Maven settings文件（包含私有仓库配置）
mvn -s maven-settings.xml clean install

# 或使用当前项目配置
mvn -f conference-auth/pom.xml clean package
```

### 步骤4: 启动服务

```bash
# 开发环境：直接运行
mvn -s maven-settings.xml -f conference-auth/pom.xml spring-boot:run

# 生产环境：打包后运行
java -jar conference-auth/target/conference-auth-1.0.0-SNAPSHOT.jar

# 使用自定义配置启动
java -jar conference-auth/target/conference-auth-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3308/conference_auth \
  --spring.datasource.password=Hnhx@123
```

### 步骤5: 验证服务

```bash
# 测试登录接口
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'

# 期望返回:
# {
#   "code": 200,
#   "message": "登录成功",
#   "data": {
#     "accessToken": "eyJhbGc...",
#     "refreshToken": "eyJhbGc...",
#     "userInfo": { ... }
#   }
# }
```

---

## 前端部署

### 方案1: 本地文件方式

直接在浏览器中打开文件：

```
file:///G:/huiwutong新版合集/admin-pc/conference-admin-pc/pages/login.html
```

**优点**: 无需Web服务器，开发快速  
**缺点**: 可能有跨域问题，某些功能受限

### 方案2: 使用Python简易Web服务器

```bash
# Python 3.x
cd "G:\huiwutong新版合集\admin-pc\conference-admin-pc"
python -m http.server 8000

# 然后访问:
# http://localhost:8000/pages/login.html
```

### 方案3: 使用Node.js静态服务器

```bash
# 全局安装
npm install -g http-server

# 启动服务器
cd "G:\huiwutong新版合集\admin-pc\conference-admin-pc"
http-server -p 8000

# 访问
# http://localhost:8000/pages/login.html
```

### 方案4: 使用Apache/Nginx

#### Apache配置
```apache
<VirtualHost *:80>
    ServerName admin.conference.local
    DocumentRoot "G:\huiwutong新版合集\admin-pc\conference-admin-pc"
    
    <Directory "G:\huiwutong新版合集\admin-pc\conference-admin-pc">
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>
    
    # CORS设置
    Header set Access-Control-Allow-Origin "*"
    Header set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
</VirtualHost>
```

#### Nginx配置
```nginx
server {
    listen 80;
    server_name admin.conference.local;
    
    root /path/to/admin-pc/conference-admin-pc;
    index index.html;
    
    # CORS配置
    add_header 'Access-Control-Allow-Origin' '*';
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS';
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理（如果需要）
    location /api/ {
        proxy_pass http://localhost:8081;
    }
}
```

---

## 测试验证

### 完整的自动化测试流程

```powershell
# 1. 启动后端
Start-Process powershell -ArgumentList `
  "cd 'G:\huiwutong新版合集\backend\conference-backend'; `
   mvn -s maven-settings.xml -f conference-auth\pom.xml spring-boot:run"

# 等待服务启动
Start-Sleep -Seconds 10

# 2. 测试登录接口
$response = Invoke-RestMethod -Uri "http://localhost:8081/auth/login" `
  -Method Post `
  -Headers @{"Content-Type"="application/json"} `
  -Body (@{
    username = "admin"
    password = "123456"
  } | ConvertTo-Json)

Write-Host "登录响应: $($response | ConvertTo-Json)"

# 3. 提取Token
$token = $response.data.accessToken
Write-Host "Token获取成功: $($token.Substring(0, 20))..."

# 4. 测试获取用户信息
$userResponse = Invoke-RestMethod -Uri "http://localhost:8081/auth/me" `
  -Method Get `
  -Headers @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
  }

Write-Host "用户信息: $($userResponse | ConvertTo-Json)"

# 5. 打开登录页面
Start-Process "file:///G:/huiwutong新版合集/admin-pc/conference-admin-pc/pages/login.html"
```

### 手动测试清单

- [ ] 后端服务启动成功（端口8081）
- [ ] 数据库连接正常
- [ ] 登录页面能正常加载
- [ ] 使用默认凭证能成功登录
- [ ] Token正确保存在localStorage
- [ ] 自动跳转到首页
- [ ] 首页能正常显示
- [ ] 按F5刷新仍需要登录（或保持登录状态）

---

## 常见问题

### Q1: "Java不是内部或外部命令"
```
A: Java未安装或未配置到系统PATH
   
解决方案:
1. 下载安装Java 17+
2. 配置JAVA_HOME环境变量
3. 将%JAVA_HOME%\bin加入PATH
4. 重启命令行窗口
5. 运行: java -version 验证
```

### Q2: "无法连接到数据库"
```
A: 数据库未启动或连接信息错误

解决方案:
1. 确保MySQL已启动: net start MySQL80
2. 检查application.yml中的连接参数
3. 验证密码: mysql -u root -pHnhx@123
4. 检查3308端口是否被占用: netstat -ano | findstr 3308
```

### Q3: "8081端口已被占用"
```
A: 端口被其他应用占用

解决方案:
1. 查找占用端口的进程
   Windows: netstat -ano | findstr 8081
   Linux: lsof -i :8081
2. 关闭或杀死该进程
   Windows: taskkill /PID <PID> /F
   Linux: kill -9 <PID>
3. 或修改application.yml中的server.port
4. 重启服务
```

### Q4: "无法加载页面样式"
```
A: CSS文件路径错误或CORS问题

解决方案:
1. 使用Web服务器而不是file://协议
2. 检查CSS文件路径是否正确
3. 检查浏览器控制台的错误信息
4. 清空浏览器缓存 (Ctrl+Shift+Delete)
5. 确保所有CSS文件都在正确位置
```

### Q5: "Token保存失败"
```
A: localStorage被禁用或权限不足

解决方案:
1. 确保浏览器允许localStorage
2. 检查浏览器隐私模式设置
3. 清空浏览器数据和缓存
4. 尝试其他浏览器测试
5. 检查浏览器控制台的详细错误
```

---

## 故障排查

### 诊断脚本 (Windows PowerShell)

```powershell
# 系统诊断脚本
Write-Host "=== 系统诊断 ===" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Java
Write-Host "1. 检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✅ Java已安装: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "❌ Java未安装或不在PATH中" -ForegroundColor Red
}

# 2. 检查Maven
Write-Host ""
Write-Host "2. 检查Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version
    Write-Host "✅ Maven已安装: $($mvnVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven未安装或不在PATH中" -ForegroundColor Red
}

# 3. 检查MySQL连接
Write-Host ""
Write-Host "3. 检查MySQL连接..." -ForegroundColor Yellow
try {
    $mysqlTest = mysql -h localhost -P 3308 -u root -pHnhx@123 -e "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ MySQL连接正常" -ForegroundColor Green
    } else {
        Write-Host "❌ MySQL连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ MySQL工具未安装" -ForegroundColor Red
}

# 4. 检查端口
Write-Host ""
Write-Host "4. 检查端口占用..." -ForegroundColor Yellow
$ports = @(8081, 3308, 8000, 80, 443)
foreach ($port in $ports) {
    $portStatus = netstat -ano -p tcp | Select-String ":$port " | Select-Object -First 1
    if ($portStatus) {
        Write-Host "⚠️  端口 $port 已被占用: $portStatus" -ForegroundColor Yellow
    } else {
        Write-Host "✅ 端口 $port 可用" -ForegroundColor Green
    }
}

# 5. 检查文件
Write-Host ""
Write-Host "5. 检查文件..." -ForegroundColor Yellow
$requiredFiles = @(
    "G:\huiwutong新版合集\admin-pc\conference-admin-pc\pages\login.html",
    "G:\huiwutong新版合集\admin-pc\conference-admin-pc\js\auth-service.js",
    "G:\huiwutong新版合集\backend\conference-backend\conference-auth\pom.xml"
)
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "✅ 文件存在: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ 文件缺失: $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== 诊断完成 ===" -ForegroundColor Cyan
```

### 日志检查

```bash
# 查看Spring Boot日志
tail -f $JAVA_LOG_FILE

# 查看MySQL错误日志
tail -f /var/log/mysql/error.log

# 浏览器控制台
F12 → Console标签 → 检查红色错误
```

---

## 📞 技术支持

遇到问题？按以下步骤排查：

1. **检查系统环境**: 运行上述诊断脚本
2. **查看日志**: 检查服务和浏览器控制台日志
3. **参考文档**: 查看本指南的"常见问题"和"故障排查"部分
4. **搜索方案**: 根据错误信息搜索解决方案
5. **联系技术支持**: 提供完整的错误日志和系统信息

---

**完成本指南中的所有步骤后，系统应该可以正常运行！** 🚀
