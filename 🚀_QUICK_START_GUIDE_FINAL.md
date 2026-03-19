# ✨ 快速操作指南 - 系统启动清单

## 系统启动顺序

### 1️⃣ 启动数据库 (MySQL)
```bash
# Windows - 如果安装了MySQL服务
net start MySQL80

# 或使用Docker
docker start mysql-container
```

### 2️⃣ 启动后端微服务

#### 排座服务 (8086) - 必需
```bash
cd backend\conference-backend\conference-seating\target
java -jar conference-seating-1.0.0.jar --spring.profiles.active=local
```

#### 其他服务 (可选)
```bash
# 会议服务 (8083)
java -jar conference-meeting-1.0.0.jar --server.port=8083

# 报名服务 (8082)
java -jar conference-registration-1.0.0.jar --server.port=8082

# 等等...
```

### 3️⃣ 启动前端服务 (8000) - 必需
```bash
cd admin-pc\conference-admin-pc
npx http-server -p 8000 --cors
```

### 4️⃣ 访问系统

打开浏览器访问:
```
http://localhost:8000/pages/login.html
```

## 系统架构概览

```
┌─────────────────────────────────────────┐
│        浏览器 (前端用户界面)             │
│        http://localhost:8000             │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│     前端HTTP服务器 (http-server)        │
│     端口: 8000                           │
│     功能: 提供静态文件(HTML/CSS/JS)     │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
  ┌──────────────┐    ┌──────────────┐
  │  后端API 1   │    │  后端API 2   │
  │  排座服务    │    │  其他服务    │
  │  8086        │    │  8082/8083   │
  │  (Spring)    │    │  (Spring)    │
  └──────┬───────┘    └──────┬───────┘
         │                   │
         └──────────┬────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │   数据库 (MySQL)    │
         │   端口: 3308        │
         │   数据库: conference_db
         └─────────────────────┘
```

## 常见端口速查

| 端口 | 服务 | 描述 | 启动命令 |
|-----|------|------|---------|
| 8000 | 前端Web | http-server | `npx http-server -p 8000 --cors` |
| 8080 | API网关 | Spring Boot | `java -jar conference-gateway-1.0.0.jar` |
| 8081 | 认证服务 | Spring Boot | `java -jar conference-auth-1.0.0.jar` |
| 8082 | 报名服务 | Spring Boot | `java -jar conference-registration-1.0.0.jar` |
| 8083 | 会议服务 | Spring Boot | `java -jar conference-meeting-1.0.0.jar` |
| 8084 | 未使用 | - | - |
| 8085 | 未使用 | - | - |
| 8086 | 排座服务 | Spring Boot | `java -jar conference-seating-1.0.0.jar` |
| 3308 | MySQL | 数据库 | `net start MySQL80` 或 Docker |

## 验证服务状态

### 方式1: 检查端口监听
```powershell
# PowerShell命令
Get-NetTCPConnection -LocalPort 8000 | Select-Object State
Get-NetTCPConnection -LocalPort 8086 | Select-Object State
Get-NetTCPConnection -LocalPort 3308 | Select-Object State
```

### 方式2: 浏览器测试
```
前端: http://localhost:8000
排座: http://localhost:8086/api/seating/venues/123
```

### 方式3: API健康检查
```powershell
# PowerShell/curl
curl http://localhost:8086/actuator/health
```

## 故障排查

### 症状1: ERR_CONNECTION_REFUSED (8000)
**原因**: 前端HTTP服务未启动
**解决**:
```bash
cd admin-pc\conference-admin-pc
npx http-server -p 8000 --cors
```

### 症状2: ERR_CONNECTION_REFUSED (8086)
**原因**: 排座服务未启动
**解决**:
```bash
cd backend\conference-backend\conference-seating\target
java -jar conference-seating-1.0.0.jar --spring.profiles.active=local
```

### 症状3: 页面加载但API返回500
**原因**: 数据库问题或服务内部错误
**检查**:
1. 数据库是否运行: `Get-NetTCPConnection -LocalPort 3308`
2. 检查排座服务日志

### 症状4: CORS错误
**原因**: 前端服务未启用CORS
**解决**: 重启时加 `--cors` 参数
```bash
npx http-server -p 8000 --cors
```

## 快速命令合集

### 一键启动排座相关服务
```bash
# 终端1: 启动排座后端
cd backend\conference-backend\conference-seating\target && java -jar conference-seating-1.0.0.jar --spring.profiles.active=local

# 终端2: 启动前端
cd admin-pc\conference-admin-pc && npx http-server -p 8000 --cors
```

### 检查所有关键端口
```powershell
Get-NetTCPConnection -LocalPort 8000,8086,3308 -ErrorAction SilentlyContinue | Select-Object LocalPort, State
```

## 本机网络地址

http-server会显示多个可用地址，都可以访问:

```
http://127.0.0.1:8000      # 本机localhost
http://198.18.0.1:8000     # 虚拟网卡
http://192.168.0.103:8000  # 局域网IP
```

在局域网内可以用192.168.0.103访问，公网需要配置代理。

## 日志查看

### 前端服务日志
http-server直接打印到终端，查看启动输出即可

### 后端服务日志
Spring Boot日志直接打印到终端:
```
2026-03-13T12:37:55.434+08:00  INFO 40488 --- [main] c.conference.seating.SeatingApplication
2026-03-13T12:38:02.500+08:00  INFO 40488 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer
```

## 性能优化建议

- 8000 (前端): 轻量级http-server，可支持数百并发
- 8086 (排座): Spring Boot支持数千并发
- 3308 (数据库): MySQL默认连接数151，可根据需要调整

## 开发模式下的环境变量

排座服务启动参数:
```bash
java -jar conference-seating-1.0.0.jar \
  --spring.profiles.active=local \
  --server.port=8086 \
  --spring.datasource.url=jdbc:mysql://localhost:3308/conference_db
```

## 生产环境检查清单

- [ ] 修改数据库连接为生产服务器地址
- [ ] 修改seating-mgr.html中的API端点(localhost:8086 → 生产地址)
- [ ] 启用HTTPS/SSL证书
- [ ] 配置反向代理(nginx/Apache)
- [ ] 实施CORS白名单(不要用*)
- [ ] 调整数据库连接池大小
- [ ] 启用日志持久化
- [ ] 配置监控告警

---

**快速查询**: 这是快速启动和故障排查指南，详细文档见项目文件
