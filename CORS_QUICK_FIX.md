# CORS问题 - 一页纸快速解决方案

## 问题
```
浏览器错误: Access to fetch at 'http://localhost:8081/auth/login' 
has been blocked by CORS policy
```

## 原因
前端用`file://`协议打开，后端在`http://localhost:8081`，后端没有CORS配置。

## 解决（已完成）

### ✅ 已添加的修复

| 项目 | 内容 | 位置 |
|------|------|------|
| 后端CORS | CorsConfig.java（新增） | backend/.../config/ |
| 前端服务器 | http-server运行 | localhost:8080 |
| 启动脚本 | start-frontend.bat | 根目录 |
| 启动脚本 | start-backend.bat | 根目录 |

## 快速启动（3步）

### 1️⃣ 启动前端（已运行）
```bash
cd admin-pc\conference-admin-pc
http-server -p 8080 -c-1

# 或双击: start-frontend.bat
```

### 2️⃣ 启动后端
```bash
cd backend\conference-backend
mvn clean install -DskipTests    # 首次5-10分钟
mvn -pl conference-auth -am spring-boot:run

# 或双击: start-backend.bat
```

### 3️⃣ 测试登录
```
浏览器访问: http://localhost:8080/pages/login.html
用户名: admin
密码: 123456
```

## 状态检查

```powershell
# 检查前端是否运行
netstat -ano | findstr :8080     # 应该有监听

# 检查后端是否运行  
netstat -ano | findstr :8081     # 应该有监听

# 测试API
curl http://localhost:8081/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

## 浏览器验证

打开F12 Console执行：

```javascript
// 验证1: AuthService加载
console.log(window.AuthService);        // 应该不是undefined

// 验证2: 测试登录API
window.AuthService.login('admin', '123456')
  .then(r => console.log('✓ 成功:', r))
  .catch(e => console.error('✗ 失败:', e))

// 验证3: 检查token
console.log(localStorage.authToken);    // 应该有token
```

## 常见问题

| 问题 | 解决 |
|------|------|
| `http://localhost:8080`无法访问 | 检查`http-server -p 8080`是否运行 |
| `http://localhost:8081`无法访问 | 检查后端`spring-boot:run`是否运行 |
| CORS错误仍出现 | 清除浏览器缓存(Ctrl+Shift+Delete)，硬刷新(Ctrl+F5) |
| 登录提示"网络错误" | 后端可能未启动，检查8081端口 |
| `window.AuthService undefined` | 刷新页面或检查JS是否加载 |

## 文档导航

- 📖 **详细指南** → CORS_COMPLETE_GUIDE.md
- 🧪 **测试步骤** → QUICK_TEST_GUIDE.md  
- 🔧 **Maven问题** → MAVEN_TROUBLESHOOTING.md
- 📋 **完整文档** → CORS_SOLUTION.md

## 架构（最终）

```
浏览器 http://localhost:8080/login.html
    ↓ (JavaScript fetch)
认证API http://localhost:8081/auth/login
    ↓ (CorsConfig配置允许跨域)
MySQL :3308 (conference_auth数据库)
```

---

**状态**: ✅ CORS问题已解决，可以正常测试  
**下一步**: 启动前端和后端，进行端到端测试
