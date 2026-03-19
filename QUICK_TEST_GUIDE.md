# 快速测试指南 - CORS问题解决

## 🚀 快速开始（5分钟）

### 环境要求

- ✅ Node.js 16+ (已安装http-server)
- ✅ Maven 3.8+ (用于编译Java后端)
- ✅ Java 21+ (用于运行Spring Boot)
- ✅ MySQL 9.6 (端口3308，密码: Hnhx@123)

### 步骤1：启动前端（已完成）

**状态**: ✅ 运行中

```bash
# 自动启动
双击: start-frontend.bat

# 或手动启动
cd admin-pc\conference-admin-pc
http-server -p 8080 -c-1
```

**验证**：
```
✓ http://127.0.0.1:8080 可访问
```

### 步骤2：启动后端（需要做）

#### 方案A：自动启动脚本（推荐）

```bash
双击: start-backend.bat
```

这个脚本会：
1. 检查Maven环境
2. 进入backend目录
3. 清理并编译所有模块（约2分钟）
4. 启动认证服务

#### 方案B：手动启动

```bash
# 进入后端目录
cd backend\conference-backend

# 编译所有依赖（第一次需要下载，约5-10分钟）
mvn clean install -DskipTests

# 启动认证服务
mvn -pl conference-auth -am spring-boot:run
```

**预期输出**（成功）：
```
2026-02-26 18:55:00.123 INFO  AuthApplication : Starting AuthApplication
2026-02-26 18:55:05.456 INFO  AuthApplication : Started AuthApplication in 5.333 seconds
```

**验证**：
```bash
# 打开新的PowerShell窗口，执行
curl http://localhost:8081/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

### 步骤3：测试登录功能

1. **打开浏览器**：
   ```
   http://localhost:8080/pages/login.html
   ```

2. **选择登录方式**：
   - 选择"系统管理员登录"标签

3. **输入凭证**：
   - 用户名: `admin`
   - 密码: `123456`

4. **点击登录**

### 步骤4：验证成功

**成功迹象**：
- ✅ 页面跳转到 `http://localhost:8080/index.html`
- ✅ 浏览器Console无红色错误
- ✅ localStorage中有认证token

**浏览器检查**：

```javascript
// 在F12 Console执行

// 检查1: 验证AuthService加载
console.log('AuthService:', window.AuthService);
// 应该输出: AuthService { ... }

// 检查2: 验证token存储
console.log('Token:', localStorage.getItem('authToken'));
// 应该输出: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

// 检查3: 验证用户信息
console.log('User:', JSON.parse(localStorage.getItem('userInfo')));
// 应该输出: { id: 1, username: 'admin', tenantCode: 'default', ... }

// 检查4: 直接调用API
window.AuthService.login('admin', '123456').then(r => console.log('Login result:', r));
```

## 🔧 故障排除

### 问题1：前端加载失败
```
错误: Cannot GET /pages/login.html
```

**解决**：
```bash
# 验证http-server运行
netstat -ano | findstr :8080

# 验证admin-pc目录结构
dir admin-pc\conference-admin-pc\pages\login.html

# 重启服务
cd admin-pc\conference-admin-pc
http-server -p 8080 -c-1
```

### 问题2：后端编译失败
```
[ERROR] Non-parseable POM
[ERROR] Could not find the selected project
```

**解决**：
```bash
# 清除Maven缓存
rmdir /S /Q "%UserProfile%\.m2\repository"

# 重新编译
cd backend\conference-backend
mvn clean install -DskipTests

# 或使用阿里云镜像
mvn -s maven-settings.xml clean install -DskipTests
```

### 问题3：登录仍然失败
```
错误: 网络连接失败，请检查服务器是否在线
```

**排查步骤**：

```bash
# 1. 检查后端是否运行
netstat -ano | findstr :8081

# 2. 测试API直连
curl http://localhost:8081/auth/login -X POST ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"

# 3. 检查MySQL是否运行
netstat -ano | findstr :3308
mysql -h 127.0.0.1 -P 3308 -u root -p
```

### 问题4：CORS错误仍然出现
```
Access to fetch from origin 'http://localhost:8080' has been blocked by CORS policy
```

**原因**：后端未加载CorsConfig

**解决**：

1. **验证文件存在**：
```bash
dir backend\conference-backend\conference-auth\src\main\java\com\conference\auth\config\CorsConfig.java
```

2. **验证部署**：
```bash
# 检查编译的JAR中是否包含CorsConfig
jar tf backend\conference-backend\conference-auth\target\*.jar | findstr CorsConfig
```

3. **完整重新启动**：
```bash
# 完全清除
cd backend\conference-backend
mvn clean

# 重新编译
mvn install -DskipTests

# 重启服务（关闭现有的后端进程）
mvn -pl conference-auth -am spring-boot:run
```

### 问题5：MySQL连接失败
```
ERROR: Access denied for user 'root'@'localhost'
```

**验证MySQL**：
```bash
# 检查MySQL是否运行
netstat -ano | findstr :3308

# 检查密码
mysql -h 127.0.0.1 -P 3308 -u root -pHnhx@123

# 查看数据库
show databases;
use conference_auth;
show tables;
```

## 📊 系统架构

```
┌──────────────────────────────────────────────────────────┐
│                    用户浏览器                            │
└─────────────────────────────────────────────────────────┘
                           │
                    HTTP请求 / 响应
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
    ┌───▼────────────────┐         ┌────────▼─────────┐
    │   前端应用         │         │   CORS配置的     │
    │  localhost:8080    │────────▶│   认证服务        │
    │                    │◀────────│  localhost:8081   │
    │ - login.html       │         │                   │
    │ - auth-service.js  │         │ CorsConfig.java ✅│
    │ - app-init.js      │         │ AuthController    │
    └────────────────────┘         └────────┬──────────┘
                                            │
                                    MySQL连接 (3308)
                                            │
                                   ┌────────▼──────┐
                                   │    MySQL 9.6  │
                                   │  conference_  │
                                   │    auth       │
                                   └───────────────┘
```

## ✅ 完整验证清单

- [ ] 前端在http://localhost:8080运行
- [ ] 后端在http://localhost:8081运行
- [ ] MySQL在port 3308运行
- [ ] 浏览器访问http://localhost:8080/pages/login.html
- [ ] window.AuthService对象存在
- [ ] 输入admin/123456能成功登录
- [ ] 重定向到index.html
- [ ] localStorage中有authToken
- [ ] F12 Console无红色错误

## 🎯 测试用例

### 用例1：系统管理员登录

**输入**：
- 用户名: `admin`
- 密码: `123456`

**预期**：
- 登录成功
- 跳转到index.html
- localStorage.authToken存在

### 用例2：租户管理员登录

**输入**：
- 租户代码: `default`
- 用户名: `admin`
- 密码: `123456`

**预期**：
- 登录成功
- localStorage.tenantCode = "default"

### 用例3：错误密码

**输入**：
- 用户名: `admin`
- 密码: `wrongpassword`

**预期**：
- 显示错误提示
- 保留在login.html页面

## 📝 日志位置

- **前端日志**：浏览器F12 Console
- **后端日志**：后端启动窗口的标准输出
- **MySQL日志**：`C:\ProgramData\MySQL\MySQL Server 9.6\Data\`

## 🔑 关键文件

**后端CORS配置**（新增）：
```
backend\conference-backend\conference-auth\src\main\java\
  com\conference\auth\config\CorsConfig.java
```

**启动脚本**（新增）：
```
start-backend.bat      (Windows)
start-backend.sh       (Linux/Mac)
start-frontend.bat     (Windows)
```

**配置文档**（新增）：
```
CORS_SOLUTION.md       (本文档概览)
```

## ⏱️ 预期时间

- ✅ 启动前端：< 1分钟
- ⏳ 编译后端（首次）：5-10分钟（取决于网络)
- ⏳ 编译后端（后续）：1-2分钟
- ✅ 测试登录：< 1分钟

**总计首次运行**：15-20分钟

## 下一步

1. ✅ 启动前端 (`start-frontend.bat`)
2. ✅ 启动后端 (`start-backend.bat`)
3. ✅ 测试登录功能
4. ✅ 验证token存储
5. ⏳ 继续其他功能开发

如有问题，参考CORS_SOLUTION.md中的详细说明。
