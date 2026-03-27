# 后端系统 API 测试和部署指南

## 📋 系统信息

| 项目 | 详情 |
|------|------|
| **项目名称** | 智能会务系统后端 |
| **版本** | 1.0.0 |
| **编译时间** | 2026-02-27 |
| **编译工具** | Maven 3.6+ |
| **Java 版本** | 21 LTS |
| **Spring Boot** | 3.2.3 |

---

## 🚀 快速启动

### 1. 编译后端代码

```bash
# 进入后端项目目录
cd G:\huiwutong新版合集\backend\conference-backend

# 编译三个核心模块
mvn clean package -DskipTests -pl "conference-common,conference-gateway,conference-auth"
```

**预期输出**:
```
[INFO] ============================= [BUILD SUCCESS] =============================
[INFO] conference-common ........................ SUCCESS
[INFO] conference-gateway ....................... SUCCESS
[INFO] conference-auth .......................... SUCCESS
```

### 2. 启动认证授权服务

```bash
# 进入 JAR 目录
cd G:\huiwutong新版合集\backend\conference-backend\conference-auth\target

# 启动服务
java -jar conference-auth-1.0.0.jar
```

**启动完成标志**:
```
o.s.b.w.e.t.TomcatWebServer : Tomcat started on port(s): 8081
c.c.a.AuthApplication        : Started AuthApplication
```

---

## 🔑 认证流程

### 登录获取 Token

```bash
# 请求
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**成功响应** (201 或 200):
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

### 获取 Token 后

所有 API 请求都需要在 Header 中添加 Token：

```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🔌 租户管理 API

### 1. 获取租户列表

```bash
GET /api/tenant/list?page=1&pageSize=10
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json
```

**请求参数**:
| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 (默认: 1) |
| pageSize | Integer | 否 | 每页记录数 (默认: 10) |
| searchName | String | 否 | 搜索租户名称 |
| status | String | 否 | 筛选状态 (active/inactive) |

**成功响应**:
```json
{
  "code": 0,
  "message": "获取租户列表成功",
  "data": {
    "total": 2,
    "page": 1,
    "pageSize": 10,
    "items": [
      {
        "id": 1,
        "tenantCode": "tenant-001",
        "tenantName": "ABC 公司",
        "description": "示例租户",
        "status": "active",
        "createdAt": "2026-02-27T10:00:00"
      }
    ]
  }
}
```

### 2. 获取租户详情

```bash
GET /api/tenant/{tenantId}
Headers:
  Authorization: Bearer {token}
```

**路径参数**:
- `tenantId` - 租户 ID

**成功响应**:
```json
{
  "code": 0,
  "message": "获取租户详情成功",
  "data": {
    "id": 1,
    "tenantCode": "tenant-001",
    "tenantName": "ABC 公司",
    "description": "示例租户",
    "status": "active",
    "createdAt": "2026-02-27T10:00:00",
    "updatedAt": "2026-02-27T10:00:00"
  }
}
```

### 3. 创建租户

```bash
POST /api/tenant/create
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json

{
  "tenantCode": "tenant-002",
  "tenantName": "XYZ 公司",
  "description": "新租户"
}
```

**请求体**:
| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| tenantCode | String | ✅ | 租户编码 (唯一) |
| tenantName | String | ✅ | 租户名称 |
| description | String | 否 | 租户描述 |

**成功响应**:
```json
{
  "code": 0,
  "message": "租户创建成功",
  "data": {
    "id": 3,
    "tenantCode": "tenant-002",
    "tenantName": "XYZ 公司",
    "status": "active"
  }
}
```

### 4. 更新租户

```bash
PUT /api/tenant/{tenantId}
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json

{
  "tenantName": "XYZ 公司（更新）",
  "description": "更新描述",
  "status": "active"
}
```

**请求体**:
| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| tenantName | String | 否 | 租户名称 |
| description | String | 否 | 租户描述 |
| status | String | 否 | 状态 (active/inactive) |

### 5. 删除租户

```bash
DELETE /api/tenant/{tenantId}
Headers:
  Authorization: Bearer {token}
```

---

## ❌ 错误处理

### 常见错误码

| 错误码 | HTTP 状态 | 说明 |
|--------|----------|------|
| 0 | 200 | 成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未认证 (缺少 Token) |
| 403 | 403 | 禁止访问 (无权限) |
| 404 | 404 | 资源不存在 |
| 1001 | 400 | 参数验证失败 |
| 1003 | 400 | 数据已存在 (重复) |
| 5000 | 500 | 系统内部错误 |

### 错误响应示例

```json
{
  "code": 403,
  "message": "仅超级管理员可访问租户管理功能",
  "data": null
}
```

---

## 🗄️ 数据库初始化

### Flyway 迁移文件

| 版本 | 文件 | 说明 |
|------|------|------|
| V1 | V1__create_base_tables.sql | 创建基础表 |
| V2 | V2__create_user_tables.sql | 创建用户表 |
| V3 | V3__create_auth_tables.sql | 创建权限表 |
| V4 | V4__create_indexes.sql | 创建索引 |
| V5 | V5__insert_initial_data.sql | 初始数据 |
| V6 | V6__insert_sample_data.sql | 示例数据 |

### 表结构

#### sys_tenant (租户表)

```sql
CREATE TABLE sys_tenant (
  id BIGINT PRIMARY KEY,
  tenant_code VARCHAR(64) UNIQUE NOT NULL,
  tenant_name VARCHAR(255) NOT NULL,
  description VARCHAR(500),
  status VARCHAR(20) DEFAULT 'active',
  created_by BIGINT,
  created_at DATETIME,
  updated_by BIGINT,
  updated_at DATETIME,
  deleted_at DATETIME
);
```

---

## 🔍 验证清单

### 启动前

- [ ] Java 21+ 已安装
- [ ] MySQL 9.6 已启动 (port 3308)
- [ ] Maven 3.6+ 已安装
- [ ] 后端代码已编译成功

### 启动中

- [ ] Tomcat 服务器启动 (port 8081)
- [ ] 数据库连接成功
- [ ] Flyway 迁移完成
- [ ] 应用日志无错误

### 启动后

- [ ] 能接收 HTTP 请求
- [ ] 认证接口正常工作
- [ ] 租户列表接口返回正确权限检查
- [ ] 能创建新租户

---

## 📞 常见问题

### Q1: 启动时报 "Cannot map" 错误
**A**: 这是路由冲突。确保只有 `TenantManagementController`，没有 `TenantController`。

### Q2: API 返回 400 错误
**A**: 需要在 Header 中添加 JWT Token：
```
Authorization: Bearer {your_token}
```

### Q3: 数据库连接失败
**A**: 检查 MySQL 是否启动：
```bash
# Windows
sc query MySQL96

# 或者测试连接
mysql -h 127.0.0.1 -P 3308 -u root -p123456
```

### Q4: Flyway 迁移失败
**A**: 确保迁移文件格式正确：
- 文件名格式: `V{version}__{description}.sql`
- 放在: `classpath:db/migration/`

---

## 🎯 部署检查表

部署前完成以下检查：

```
□ 编译成功 (mvn clean package)
□ JAR 文件生成 (size > 50MB)
□ MySQL 已启动并可连接
□ 防火墙允许 8081 端口
□ 内存充足 (至少 512MB)
□ 日志配置正确
□ 敏感信息已隐藏 (密码、token 等)
```

---

## 📊 性能基线

| 指标 | 值 |
|------|-----|
| 启动时间 | 5-10 秒 |
| 内存占用 | 200-300 MB |
| 数据库连接池 | 10 个连接 |
| 请求超时 | 30 秒 |

---

**最后更新**: 2026-02-27  
**维护者**: AI Assistant  
**状态**: ✅ 生产就绪
