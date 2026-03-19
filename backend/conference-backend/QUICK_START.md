# 快速启动指南

## 前置条件

- ✅ JDK 21已安装
- ✅ Maven 3.9+已安装
- ✅ MySQL 9.6运行在127.0.0.1:3308
- ✅ MySQL root密码为 `Hnhx@123`

## 一键启动

### Windows (PowerShell)

```powershell
# 1. 进入项目目录
cd "G:\huiwutong新版合集\backend\conference-backend"

# 2. 启动认证服务（推荐：双击start-auth.bat）
.\start-auth.bat

# 或使用Maven命令
mvn -s maven-settings.xml -f conference-auth\pom.xml spring-boot:run
```

### Linux/Mac

```bash
# 1. 进入项目目录
cd backend/conference-backend

# 2. 启动认证服务
mvn -s maven-settings.xml -f conference-auth/pom.xml spring-boot:run
```

## 验证服务

### 使用PowerShell测试

```powershell
# 登录
$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"123456"}' `
  -UseBasicParsing

$response.Content | ConvertFrom-Json
```

### 使用curl测试（安装了curl的系统）

```bash
# 登录
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 使用Token访问受保护资源（替换<token>为实际的accessToken）
curl -X GET http://localhost:8081/auth/me \
  -H "Authorization: Bearer <token>"
```

### 使用Postman/Thunder Client

**请求URL**: `http://localhost:8081/auth/login`
**请求方法**: POST
**请求头**: `Content-Type: application/json`
**请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

## 常用账户

| 账户 | 密码 | 说明 |
|------|------|------|
| admin | 123456 | 管理员 |

## 常用端口

| 服务 | 端口 | 状态 |
|------|------|------|
| 认证服务(auth) | 8081 | ✅ 运行中 |
| 网关(gateway) | 8080 | ⏳ 待实现 |
| 报名服务(registration) | 8082 | ⏳ 待实现 |
| 其他服务 | 8083-8088 | ⏳ 待实现 |

## 查看日志

### 实时日志（服务运行窗口）
服务启动后会在控制台输出日志

### 停止服务
- Windows: 关闭服务窗口或按 Ctrl+C
- Linux: 按 Ctrl+C 或运行 `kill <进程ID>`

## 常见问题

**Q: 服务启动失败"Unknown database 'conference_auth'"**
A: 数据库未创建。运行:
```bash
mysql -h 127.0.0.1 -P 3308 -u root -p
CREATE DATABASE conference_auth DEFAULT CHARACTER SET utf8mb4;
```

**Q: 登录返回"用户名或密码错误"**
A: 检查用户是否存在:
```bash
mysql -h 127.0.0.1 -P 3308 -u root -p
USE conference_auth;
SELECT * FROM sys_user WHERE username = 'admin';
```

**Q: 无法连接到MySQL**
A: 确保:
1. MySQL服务已启动
2. 端口为3308（非默认的3306）
3. 密码为 `Hnhx@123`

## 下一步

1. ✅ 认证服务已完成 - 继续开发其他微服务
2. 部署API网关（conference-gateway）
3. 实现报名系统（conference-registration）
4. 集成其他业务服务

参考完整指南: [技术实施详细指南.md](../技术实施详细指南.md)
