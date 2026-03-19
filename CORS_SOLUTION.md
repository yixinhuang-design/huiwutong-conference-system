# CORS问题解决方案

## 问题分析

你遇到的CORS错误是因为：

```
Access to fetch at 'http://localhost:8081/auth/login' from origin 'null' has been 
blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
```

### 根本原因
1. **前端运行在file://协议**（直接打开HTML文件）
2. **后端运行在http://localhost:8081**（完全不同的源）
3. **后端没有配置CORS**（没有返回必要的跨域头）

## 已实施的解决方案

### 方案1: 后端CORS配置 ✅

已添加**CorsConfig.java**配置：

```java
// conference-auth\src\main\java\com\conference\auth\config\CorsConfig.java

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 配置CORS策略
        // 允许所有来源、所有方法、所有请求头
        // 用于开发环境
    }
}
```

**效果**：后端现在会返回`Access-Control-Allow-Origin: *`头

### 方案2: 前端HTTP服务器 ✅

已创建**start-frontend.bat**脚本：
- 使用http-server在`http://localhost:8080`运行前端
- 这样前端和后端都在http协议下，降低CORS复杂度

**优势**：
- 避免file://协议的跨域问题
- 前端在合理的Web服务器上运行
- 更接近生产环境

## 使用步骤

### 快速启动（推荐方式）

**第1步：启动前端应用**

```bash
# 自动化脚本方式
双击: start-frontend.bat

# 或手动运行
cd admin-pc\conference-admin-pc
http-server -p 8080 -c-1
```

**输出示例**：
```
Starting up http-server, serving ./
Available on:
  http://127.0.0.1:8080
```

**第2步：启动后端认证服务**

```bash
# 自动化脚本方式
双击: start-backend.bat

# 或手动运行
cd backend\conference-backend
mvn clean install -DskipTests -q
mvn -pl conference-auth -am spring-boot:run
```

**输出示例**：
```
2026-02-26 18:55:00 ... Started AuthApplication in 5.123 seconds
Server running at http://localhost:8081
```

### 第3步：访问应用

打开浏览器访问：

```
http://localhost:8080/pages/login.html
```

### 第4步：测试登录

**系统管理员登录**：
- 用户名: `admin`
- 密码: `123456`

**租户管理员登录**：
- 租户代码: `default`
- 用户名: `admin`
- 密码: `123456`

## 验证CORS配置生效

在浏览器F12开发者工具中检查：

```javascript
// 打开Console，执行
fetch('http://localhost:8081/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: '123456'
  })
})
.then(r => r.json())
.then(data => console.log(data))
.catch(e => console.error(e))
```

**预期响应**（成功）：
```javascript
{
  code: 200,
  message: "success",
  data: {
    accessToken: "eyJ...",
    refreshToken: "eyJ...",
    userInfo: { ... }
  }
}
```

## 文件变更清单

### 新增文件

1. **conference-auth\src\main\java\com\conference\auth\config\CorsConfig.java**
   - CORS跨域配置类
   - 允许所有来源的请求
   - 配置预检请求缓存时间

2. **start-frontend.bat**
   - 前端HTTP服务器启动脚本
   - 自动检测Node.js/Python
   - 在http://localhost:8080运行

3. **start-backend.bat**
   - 后端服务启动脚本
   - 自动编译和启动认证服务
   - 在http://localhost:8081运行

## 常见问题排查

### 问题1：前端无法加载

```
错误: http://localhost:8080 无法连接
```

**解决**：
```bash
# 检查http-server是否运行
netstat -ano | findstr :8080

# 或重新启动
cd admin-pc\conference-admin-pc
http-server -p 8080 -c-1
```

### 问题2：登录仍然失败

```
错误: 网络连接失败，请检查服务器是否在线
```

**解决**：

1. 检查后端是否运行
```bash
netstat -ano | findstr :8081
```

2. 查看后端错误日志

3. 清除浏览器缓存 (Ctrl+Shift+Delete)

4. 硬刷新页面 (Ctrl+F5)

### 问题3：CORS仍然被阻止

```
错误: CORS policy: No 'Access-Control-Allow-Origin' header
```

**解决**：

1. 确认CorsConfig.java已部署到jar中
```bash
jar tf target/conference-auth-1.0.0.jar | grep CorsConfig
```

2. 确认后端已重启（修改代码后）

3. 检查请求URL是否正确
   - 应该是: `http://localhost:8081/auth/login`
   - 不能是: `https://` 或其他端口

## 架构图

```
┌─────────────────────────────┐
│  浏览器 (Chrome/Firefox)     │
└──────────────┬──────────────┘
               │
               ├─────────────────────────────────┐
               │                                 │
       HTTP请求 (同源)                   HTTP请求 (跨域)
               │                                 │
        ┌──────▼──────┐                  ┌──────▼─────────┐
        │ 前端应用    │                  │  认证服务       │
        │ :8080       │ ◄─────────────── │  :8081          │
        │ (HTML/JS)   │   带CORS头       │  (Spring Boot)  │
        └─────────────┘                  └─────────────────┘
                                                │
                                                │
                                         ┌──────▼─────────┐
                                         │  MySQL 9.6     │
                                         │  :3308          │
                                         └─────────────────┘
```

## 生产环境建议

如果要部署到生产环境，建议修改CORS配置以提高安全性：

```java
// 只允许特定域名
corsConfiguration.addAllowedOrigin("https://yourdomain.com");

// 只允许特定HTTP方法
corsConfiguration.addAllowedMethod("GET");
corsConfiguration.addAllowedMethod("POST");
corsConfiguration.addAllowedMethod("PUT");
corsConfiguration.addAllowedMethod("DELETE");

// 明确列出需要的请求头
corsConfiguration.addAllowedHeader("Content-Type");
corsConfiguration.addAllowedHeader("Authorization");

// 禁用凭证共享
corsConfiguration.setAllowCredentials(false);
```

## 关键配置文件

- **后端CORS**: `conference-auth\src\main\java\com\conference\auth\config\CorsConfig.java`
- **前端配置**: `admin-pc\conference-admin-pc\js\auth-service.js`
- **认证服务**: `conference-auth\src\main\java\com\conference\auth\controller\AuthController.java`

## 下一步

1. ✅ 使用 `start-frontend.bat` 启动前端
2. ✅ 使用 `start-backend.bat` 启动后端
3. ✅ 访问 `http://localhost:8080/pages/login.html`
4. ✅ 测试登录功能
5. ⏳ 如果需要，部署到生产环境
