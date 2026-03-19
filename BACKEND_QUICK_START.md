# 🚀 后端快速启动指南

## 前提条件检查

在启动服务之前，请确保以下环境已就位：

```
✓ Java 21 LTS       - java -version
✓ Maven 3.x         - mvn -version
✓ MySQL 9.6         - 运行在 localhost:3308
✓ Redis 7.2.4       - 运行在 localhost:6379
✓ Nacos 2.3.2       - 运行在 localhost:8848
```

---

## ⚡ 5分钟快速启动

### 步骤1: 验证编译

```powershell
cd g:\huiwutong新版合集\backend\conference-backend

# 编译所有核心模块
mvn clean compile -DskipTests -pl "conference-common,conference-gateway,conference-auth,conference-notification,conference-collaboration,conference-seating,conference-ai,conference-navigation,conference-data"
```

**预期结果:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 13.722 s
```

### 步骤2: 打包成JAR

```powershell
# 生成所有JAR文件
mvn clean package -DskipTests -pl "conference-common,conference-gateway,conference-auth,conference-notification,conference-collaboration,conference-seating,conference-ai,conference-navigation,conference-data"
```

**预期结果:**
```
[INFO] BUILD SUCCESS
[INFO] 生成8个JAR文件在各模块target目录
```

### 步骤3: 启动服务

**方式A: 使用启动脚本（推荐）**

```batch
# Windows 环境下
cd g:\huiwutong新版合集
start-backend-services.bat
```

**方式B: 手动启动**

```powershell
# 终端1 - 启动认证服务
cd g:\huiwutong新版合集\backend\conference-backend
java -jar conference-auth/target/conference-auth-1.0.0.jar

# 终端2 - 启动通知服务
java -jar conference-notification/target/conference-notification-1.0.0.jar

# 终端3 - 启动协同服务
java -jar conference-collaboration/target/conference-collaboration-1.0.0.jar

# 继续启动其他服务...
```

---

## ✅ 验证服务是否启动成功

### 1. 检查各服务状态

```bash
# 在浏览器中访问
http://localhost:8081/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
# ...依此类推
```

**成功响应:**
```json
{
  "status": "UP"
}
```

### 2. 测试登录 API

```bash
# 使用 Postman 或 curl
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**成功响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "username": "admin"
  }
}
```

### 3. 检查 Nacos 服务注册

访问 http://localhost:8848/nacos

- 用户名: nacos
- 密码: nacos

查看 "服务管理" → "服务列表"，应该看到所有注册的服务：
- conference-gateway
- conference-auth
- conference-notification
- conference-collaboration
- conference-seating
- conference-ai
- conference-navigation
- conference-data

---

## 📋 服务端口映射表

| 服务 | 端口 | 启动命令 |
|------|------|---------|
| API网关 | 8080 | `java -jar conference-gateway-1.0.0.jar` |
| 认证服务 | 8081 | `java -jar conference-auth-1.0.0.jar` |
| 通知服务 | 8083 | `java -jar conference-notification-1.0.0.jar` |
| 协同服务 | 8084 | `java -jar conference-collaboration-1.0.0.jar` |
| 排座服务 | 8085 | `java -jar conference-seating-1.0.0.jar` |
| AI服务 | 8086 | `java -jar conference-ai-1.0.0.jar` |
| 导航服务 | 8087 | `java -jar conference-navigation-1.0.0.jar` |
| 数据服务 | 8088 | `java -jar conference-data-1.0.0.jar` |

---

## 🧪 常见 API 测试

### 1. 获取认证 Token

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**预期:** 返回有效的 JWT Token

### 2. 测试通知服务

```bash
curl -X POST http://localhost:8083/api/notification/send \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "conferenceId": 1,
    "channel": "sms",
    "content": "会议即将开始"
  }'
```

### 3. 测试排座服务

```bash
curl -X POST http://localhost:8085/api/seating/allocate \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "conferenceId": 1,
    "algorithm": "optimal"
  }'
```

### 4. 获取数据仪表板

```bash
curl -X GET http://localhost:8088/api/data/dashboard?conferenceId=1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## 🔧 故障排查

### 问题1: 端口被占用

```powershell
# 查找占用端口8081的进程
Get-Process | Where-Object {$_.Name -like "*java*"} | Stop-Process -Force

# 或者用netstat查看
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### 问题2: MySQL连接失败

```bash
# 测试MySQL连接
mysql -h 127.0.0.1 -P 3308 -u root -pHnhx@123

# 检查database是否存在
SHOW DATABASES;

# 如果不存在，创建
CREATE DATABASE conference_db CHARACTER SET utf8mb4;
```

### 问题3: Nacos服务发现失败

```bash
# 检查Nacos是否运行
# 访问 http://localhost:8848/nacos

# 检查application.yml中的Nacos配置
# spring.cloud.nacos.discovery.server-addr: localhost:8848
```

### 问题4: 看不到日志输出

```powershell
# 确保启动命令中没有使用 &norestore 或后台运行标志
# 使用以下命令启动（会显示实时日志）
java -jar conference-auth-1.0.0.jar
```

---

## 📊 实时监控

### 1. 访问应用健康检查

```
http://localhost:8081/actuator
http://localhost:8081/actuator/health
http://localhost:8081/actuator/metrics
```

### 2. 查看Nacos服务状态

```
http://localhost:8848/nacos
-> 服务管理 -> 服务列表
```

### 3. Redis连接检查

```bash
# 使用redis-cli连接
redis-cli -h localhost -p 6379
> PING
PONG
```

---

## 💡 开发建议

### 本地开发环境设置

1. **在IDEA中运行单个服务**
   - 右键 → Run → Run 'AuthApplication'
   - 在 IDE 中可以方便地调试和查看日志

2. **使用热启动插件**
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-devtools</artifactId>
     <optional>true</optional>
   </dependency>
   ```

3. **实时重编译**
   - IDEA: Build → Build Project (Ctrl+F9)
   - 服务会自动重启

### 生产部署建议

1. **使用Docker容器化**
   ```dockerfile
   FROM openjdk:21-jdk-slim
   COPY conference-auth-1.0.0.jar app.jar
   ENTRYPOINT ["java","-jar","app.jar"]
   ```

2. **使用进程管理器（如systemd）**
3. **配置监控和告警**
4. **使用负载均衡（如Nginx）**

---

## 📚 相关文档

- [完整总结](./BACKEND_COMPLETION_SUMMARY.md)
- [技术路线图](./技术实现路线图.md)
- [技术详细指南](./技术实施详细指南.md)

---

## 🎯 下一步

1. ✅ 启动后端服务
2. ⏭️ **测试API功能**
3. ⏭️ 启动前端应用
4. ⏭️ 进行前后端集成测试
5. ⏭️ 完成 registration 模块开发

---

**最后更新:** 2026年2月27日  
**版本:** 1.0.0  
**状态:** 可运行 ✅
