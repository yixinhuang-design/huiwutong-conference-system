# 📡 智能会务系统 — 完整API接口文档

> **文档版本**: v2.0（深度补充版）  
> **基础地址**: `http://localhost:{port}`  
> **认证方式**: Bearer Token (JWT)  
> **多租户**: Header `X-Tenant-Id`  
> **适用范围**: 移动端、PC管理端、第三方对接  
> **v2.0更新**: 基于代码深度扫描，为全部14个服务模块补充了详细字段说明、状态枚举、错误响应、请求体示例，新增26个原文遗漏端点（分块上传7个、资料管理8个、健康检查4个、OCR/语音2个等），标注了所有空壳/模拟实现的功能限制

---

## 目录

- [一、认证授权服务 (Auth, :8081)](#一认证授权服务)
- [二、会议服务 (Meeting, :8084)](#二会议服务)
- [三、日程服务 (Schedule, :8084)](#三日程服务)
- [四、附件服务 (Attachment, :8084)](#四附件服务)
- [五、归档服务 (Archive, :8084)](#五归档服务)
- [六、报名服务 (Registration, :8082)](#六报名服务)
- [七、通知服务 (Notification, :8083)](#七通知服务)
- [八、预警服务 (Alert, :8083)](#八预警服务)
- [九、协同-群组服务 (Group, :8089)](#九协同-群组服务)
- [十、协同-任务服务 (Task, :8089)](#十协同-任务服务)
- [十一、协同-聊天服务 (Chat, :8089)](#十一协同-聊天服务)
- [十二、排座服务 (Seating, :8086)](#十二排座服务)
- [十三、AI服务 (AI, :8085)](#十三ai服务)
- [十四、数据服务 (Data, :8088)](#十四数据服务)

---

## 通用规范

### 请求头

```
Authorization: Bearer <JWT_TOKEN>
X-Tenant-Id: <TENANT_ID>
Content-Type: application/json
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 405 | 方法不允许 |
| 500 | 服务器内部错误 |

### 分页参数

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| page | Integer | 1 | 页码 |
| size | Integer | 10 | 每页条数 |

---

## 一、认证授权服务

**服务**: conference-auth  
**端口**: 8081  
**基础路径**: `/api`

### 1.1 登录

```
POST /api/auth/login
```

**请求体**:
```json
{
  "username": "admin",
  "password": "123456",
  "tenantId": "2027317834622709762"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400,
    "userId": "1001",
    "username": "admin",
    "tenantId": "2027317834622709762"
  }
}
```

### 1.2 刷新Token

```
POST /api/auth/refresh
```

**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 1.3 获取用户信息

```
GET /api/auth/userinfo
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "userId": "1001",
    "username": "admin",
    "tenantId": "2027317834622709762",
    "roles": ["admin"]
  }
}
```

### 1.4 登出

```
POST /api/auth/logout
```

### 1.5 租户管理

```
GET    /api/tenant/list                     # 租户列表
GET    /api/tenant/{id}                     # 租户详情
POST   /api/tenant/create                   # 创建租户
PUT    /api/tenant/{id}                     # 更新租户
DELETE /api/tenant/{id}                     # 删除租户
PUT    /api/tenant/{id}/status?status=xxx   # 更新租户状态 (active/inactive/locked)
GET    /api/tenant/stats                    # 租户统计
```

**创建租户请求体**:
```json
{
  "name": "测试租户",
  "code": "TEST001",
  "contactPerson": "张三",
  "contactPhone": "13800138000",
  "maxUsers": 100,
  "expiryDate": "2025-12-31"
}
```

### 1.6 登录字段详细说明（补充）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | ✅ | 用户名 |
| password | String | ✅ | 密码，支持BCrypt和MD5两种格式 |
| tenantId | String | ✅ | 租户ID（代码中实际字段名为 `tenantCode`） |

> ⚠️ **实现细节**:
> - 登录控制器实际接收 `LoginRequest`，其字段为 `tenantCode`（非文档示例中的 `tenantId`），前端需注意字段名映射
> - 密码验证支持双格式: 优先尝试 BCrypt 验证，失败后降级为 MD5 验证（`MD5.create().digestHex()`）
> - Auth模块的 `JwtUtils` 使用 **Hutool JWT（HS384算法）**，而 Gateway 的 `JwtUtils` 使用 **JJWT库（HMAC-SHA算法）**，两端算法不一致可能导致 Token 验证失败
> - `refreshToken` 接口实现为空（直接返回 null），刷新 Token 功能不可用
> - `logout` 接口为空实现（不做任何 Token 失效处理），退出后 Token 仍然有效至过期

### 1.7 登录错误响应（补充）

**密码错误**:
```json
{
  "code": 401,
  "message": "用户名或密码错误"
}
```

**租户不存在/已禁用**:
```json
{
  "code": 404,
  "message": "租户不存在或已禁用"
}
```

**缺少必填字段**:
```json
{
  "code": 400,
  "message": "参数校验失败",
  "errors": ["username不能为空"]
}
```

### 1.8 Token 完整响应格式（补充）

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "userId": 1001,
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "tenantId": "2027317834622709762",
      "roles": ["admin"]
    }
  }
}
```

> Token 有效期默认 86400 秒（24小时），在 `application.yml` 的 `jwt.expiration` 配置。

### 1.9 租户管理字段详细说明（补充）

**租户完整字段**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ✅ | 租户名称 |
| code | String | ✅ | 租户编码（唯一标识） |
| contactPerson | String | ❌ | 联系人 |
| contactPhone | String | ❌ | 联系电话 |
| contactEmail | String | ❌ | 联系邮箱 |
| address | String | ❌ | 地址 |
| maxUsers | Integer | ❌ | 最大用户数 |
| expiryDate | LocalDate | ❌ | 到期时间 |
| description | String | ❌ | 描述 |

**租户状态值**: `active`（活跃）/ `inactive`（停用）/ `locked`（锁定）

> ⚠️ **安全风险提醒**:
> - Gateway 白名单包含 `/api/v1/tenant/**`，所有租户管理 API 无需认证即可访问
> - `isSuperAdmin()` 方法始终返回 `true`，任何登录用户均可执行租户管理操作
> - 租户列表 `/api/tenant/list` 未实现分页，数据量大时存在性能风险
> - 登录失败计数器（`loginFailCount`）存在于实体中但从未递增，账户锁定机制不生效

---

## 二、会议服务

**服务**: conference-meeting  
**端口**: 8084  
**基础路径**: `/api/meeting`

### 2.1 创建会议

```
POST /api/meeting/create
```

**请求体**:
```json
{
  "name": "2025年度工作会议",
  "description": "年度总结与规划",
  "startTime": "2025-07-01T09:00:00",
  "endTime": "2025-07-03T17:00:00",
  "location": "北京国际会议中心",
  "coverImage": "/uploads/cover/xxx.jpg",
  "status": "preparing"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": "2030309010523144194",
    "name": "2025年度工作会议",
    "status": "preparing",
    "createTime": "2025-06-01T10:00:00"
  }
}
```

### 2.2 获取会议详情

```
GET /api/meeting/{id}
```

### 2.3 会议列表

```
GET /api/meeting/list?page=1&size=10&status=preparing
```

### 2.4 更新会议

```
PUT /api/meeting/{id}
```

### 2.5 删除会议

```
DELETE /api/meeting/{id}
```

### 2.6 更新会议状态

```
PUT /api/meeting/{id}/status?status=ongoing
```

| status | 说明 |
|--------|------|
| preparing | 筹备中 |
| ongoing | 进行中 |
| ended | 已结束 |

### 2.7 会议统计

```
GET /api/meeting/{id}/statistics
```

### 2.8 进行中的会议

```
GET /api/meeting/ongoing
```

### 2.9 上传封面

```
POST /api/meeting/upload/cover
Content-Type: multipart/form-data

file: <File>
```

### 2.10 会议工作人员

```
POST /api/meeting/{id}/staff
```

### 2.11 会议状态机（补充）

会议状态在数据库中使用 **Integer** 类型存储：

| 状态值 | 含义 | 可转换到 |
|--------|------|----------|
| 0 | 筹备中(preparing) | 1, 3 |
| 1 | 进行中(ongoing) | 2, 3 |
| 2 | 已结束(ended) | 4 |
| 3 | 已取消(cancelled) | — |
| 4 | 已归档(archived) | — |

> ⚠️ **注意**: 更新状态接口 `PUT /api/meeting/{id}/status?status=ongoing` 使用字符串参数，但 Entity 和数据库中存储为 Integer（0/1/2/3/4），Controller 层做转换。前端调用时按字符串传递即可。

### 2.12 会议列表响应详情（补充）

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": "2030309010523144194",
        "name": "2025年度工作会议",
        "description": "年度总结与规划",
        "startTime": "2025-07-01T09:00:00",
        "endTime": "2025-07-03T17:00:00",
        "location": "北京国际会议中心",
        "status": 0,
        "coverImage": "/uploads/cover/xxx.jpg",
        "participantCount": 150,
        "tenantId": "2027317834622709762",
        "createTime": "2025-06-01T10:00:00",
        "updateTime": "2025-06-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

### 2.13 会议统计响应详情（补充）

```json
{
  "code": 200,
  "data": {
    "meetingId": "2030309010523144194",
    "scheduleCount": 5,
    "registrationCount": 150,
    "checkinRate": 0.0,
    "attachmentCount": 3,
    "notificationCount": 2
  }
}
```

> ⚠️ **跨服务依赖**: 统计接口通过 `RestTemplate` HTTP 调用聚合 registration(:8082) 和 notification(:8083) 数据，当依赖服务不可用时统计值为 0（无熔断降级机制）。

---

## 三、日程服务

**服务**: conference-meeting  
**端口**: 8084  
**基础路径**: `/api/schedule`

### 3.1 创建日程

```
POST /api/schedule/create
```

**请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "title": "开幕式",
  "description": "大会开幕及领导致辞",
  "startTime": "2025-07-01T09:00:00",
  "endTime": "2025-07-01T10:00:00",
  "location": "主会场A厅",
  "speaker": "张主任",
  "needCheckin": true,
  "needReminder": true,
  "status": "draft"
}
```

### 3.2 更新日程

```
PUT /api/schedule/{id}
```

### 3.3 删除日程

```
DELETE /api/schedule/{id}
```

### 3.4 日程详情

```
GET /api/schedule/{id}
```

### 3.5 会议的所有日程

```
GET /api/schedule/list?meetingId={meetingId}
GET /api/schedule/all?meetingId={meetingId}
```

### 3.6 日程查询与过滤

```
GET /api/schedule/need-checkin?meetingId={id}     # 需签到的日程
GET /api/schedule/need-reminder?meetingId={id}    # 需提醒的日程
GET /api/schedule/ongoing?meetingId={id}          # 进行中的日程
GET /api/schedule/upcoming?meetingId={id}         # 即将开始的日程
GET /api/schedule/by-time-range?meetingId={id}&startTime=xxx&endTime=xxx
GET /api/schedule/next?meetingId={id}             # 下一个日程
GET /api/schedule/current?meetingId={id}          # 当前日程
GET /api/schedule/count?meetingId={id}            # 日程总数
```

### 3.7 日程操作

```
PUT  /api/schedule/{id}/publish     # 发布日程
PUT  /api/schedule/{id}/cancel      # 取消日程
POST /api/schedule/{id}/duplicate   # 复制日程
```

### 3.8 日程设置结构（补充）

每个日程可嵌入 `settings` JSON 配置，存储签到、提醒等业务设置：

```json
{
  "meetingId": "2030309010523144194",
  "title": "开幕式",
  "startTime": "2025-07-01T09:00:00",
  "endTime": "2025-07-01T10:00:00",
  "location": "主会场A厅",
  "speaker": "张主任",
  "status": "draft",
  "settings": {
    "needCheckin": true,
    "needReminder": true,
    "reminderTime": 15,
    "checkinSettings": {
      "checkinType": "qrcode",
      "allowLateMinutes": 10
    }
  }
}
```

**日程状态值**:

| 状态 | 说明 |
|------|------|
| draft | 草稿 |
| published | 已发布 |
| ongoing | 进行中 |
| ended | 已结束 |
| cancelled | 已取消 |

> ⚠️ **实现说明**:
> - `settings` 字段在数据库中以 JSON 字符串存储，使用 MyBatis-Plus `JacksonTypeHandler` 自动转换
> - `/api/schedule/list` 和 `/api/schedule/all` 返回相同结果（冗余端点）
> - 日程查询存在 N+1 问题：每条日程额外查询其 settings，获取 N 条日程需执行 2N+1 次 SQL

---

## 四、附件服务

**服务**: conference-meeting  
**端口**: 8084  
**基础路径**: `/api/schedule-attachment`

### 4.1 上传附件

```
POST /api/schedule-attachment/upload
Content-Type: multipart/form-data

file: <File>
scheduleId: 12345
meetingId: 2030309010523144194
```

### 4.2 批量上传

```
POST /api/schedule-attachment/upload-batch
Content-Type: multipart/form-data

files: <File[]>
scheduleId: 12345
meetingId: 2030309010523144194
```

### 4.3 附件列表

```
GET /api/schedule-attachment/list?scheduleId={scheduleId}
```

### 4.4 附件详情

```
GET /api/schedule-attachment/{id}
```

### 4.5 删除附件

```
DELETE /api/schedule-attachment/{id}
```

### 4.6 更新附件

```
PUT /api/schedule-attachment/{id}
```

### 4.7 下载

```
GET /api/schedule-attachment/{id}/download-url   # 获取下载URL
GET /api/schedule-attachment/{id}/download       # 直接下载
```

### 4.8 校验

```
POST /api/schedule-attachment/validate
```

### 4.9 断点续传 - 分块上传（补充）

> ⚡ 以下 7 个端点在原始文档中**完全缺失**，实际代码中已实现

#### 初始化分块上传

```
POST /api/schedule-attachment/chunk/init
```

**请求体**:
```json
{
  "fileName": "大型文件.zip",
  "fileSize": 104857600,
  "chunkSize": 5242880,
  "totalChunks": 20,
  "contentType": "application/zip",
  "md5": "abc123def456"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "uploadId": "upload_xxxx",
    "chunkSize": 5242880,
    "totalChunks": 20
  }
}
```

#### 上传单个分块

```
POST /api/schedule-attachment/chunk/upload
Content-Type: multipart/form-data

uploadId: upload_xxxx
chunkIndex: 0
file: <chunk_data>
```

#### 合并分块

```
POST /api/schedule-attachment/chunk/merge
```

**请求体**:
```json
{
  "uploadId": "upload_xxxx",
  "fileName": "大型文件.zip",
  "scheduleId": 12345,
  "meetingId": "2030309010523144194"
}
```

#### 查询上传进度

```
GET /api/schedule-attachment/chunk/progress?uploadId=upload_xxxx
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "uploadId": "upload_xxxx",
    "totalChunks": 20,
    "uploadedChunks": 15,
    "progress": 75.0
  }
}
```

#### 取消分块上传

```
DELETE /api/schedule-attachment/chunk/cancel?uploadId=upload_xxxx
```

#### 检查文件是否存在（秒传）

```
GET /api/schedule-attachment/chunk/check?md5=abc123def456
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "exists": true,
    "fileUrl": "/uploads/attachment/xxx.zip"
  }
}
```

#### 获取已上传分块列表

```
GET /api/schedule-attachment/chunk/uploaded?uploadId=upload_xxxx
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "uploadedIndexes": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
  }
}
```

### 4.10 文件约束说明（补充）

| 项目 | 限制 |
|------|------|
| 普通上传单文件上限 | 100MB |
| 分块上传默认分块大小 | 5MB |
| 支持的文件格式 | 无限制（仅按大小校验） |
| 文件存储位置 | 本地磁盘 `./uploads/attachment/` |

> ⚠️ **实现限制**:
> - 分块上传状态存储在内存 `ConcurrentHashMap` 中，服务重启后未完成的上传状态将丢失
> - 无分布式锁保护，多实例部署时同一 uploadId 可能冲突
> - 未实现自动清理超时未完成的分块文件
> - 秒传功能基于 MD5，存在哈希碰撞的理论风险

---

## 五、归档服务 ⭐

**服务**: conference-meeting  
**端口**: 8084  
**基础路径**: `/api/meeting/{meetingId}/archive`

> 这是我实现的核心模块，19个REST端点

### 5.1 归档统计概览

```
GET /api/meeting/{meetingId}/archive/stats
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "coursewareCount": 1,
    "interactionCount": 0,
    "businessDataCount": 22,
    "messageCount": 3,
    "isPacked": false,
    "allowStudentUpload": true
  }
}
```

### 5.2 从源数据同步

```
POST /api/meeting/{meetingId}/archive/sync
```

**说明**: 自动从以下源表同步数据到归档表：
- `conf_schedule_attachment` → 归档资料(课件)
- `conf_handbook` / `conf_handbook_discussion` → 归档消息群组+消息
- `conf_schedule_checkin` → 归档业务数据(签到率)
- `conference_collaboration.chat_group` / `chat_message` → 归档消息群组+消息(跨库)
- `conf_registration` (按organization分组) → 归档业务数据(报到率)

**响应**:
```json
{
  "code": 200,
  "data": {
    "materialsSynced": 1,
    "messageGroupsSynced": 1,
    "messagesSynced": 2,
    "chatGroupsSynced": 1,
    "chatMessagesSynced": 1,
    "checkinDataSynced": 0,
    "reportRateDataSynced": 22
  }
}
```

### 5.3 归档配置

```
GET /api/meeting/{meetingId}/archive/config
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": "123456",
    "meetingId": "2030309010523144194",
    "allowStudentUpload": true,
    "isPacked": false,
    "packTime": null
  }
}
```

```
PUT /api/meeting/{meetingId}/archive/config
```

**请求体**:
```json
{
  "allowStudentUpload": false
}
```

### 5.4 资料管理

#### 获取资料列表

```
GET /api/meeting/{meetingId}/archive/materials?category=courseware
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | String | 否 | courseware(课件) / interaction(互动) |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "100001",
      "meetingId": "2030309010523144194",
      "category": "courseware",
      "subCategory": null,
      "title": "第一课：安全培训.docx",
      "fileUrl": "/uploads/attachment/xxx.docx",
      "fileSize": "45.2KB",
      "fileType": "docx",
      "thumbnailUrl": null,
      "author": "管理员",
      "uploadBy": 1001,
      "uploadTime": "2025-06-01T10:00:00",
      "downloadCount": 0,
      "sortOrder": 1
    }
  ]
}
```

#### 添加资料

```
POST /api/meeting/{meetingId}/archive/materials
```

**请求体**:
```json
{
  "category": "courseware",
  "title": "培训手册.pdf",
  "fileUrl": "/uploads/archive/xxx.pdf",
  "fileSize": "2.5MB",
  "fileType": "pdf",
  "author": "张三"
}
```

#### 上传资料文件

```
POST /api/meeting/{meetingId}/archive/materials/upload
Content-Type: multipart/form-data

file: <File>     (最大100MB)
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "fileUrl": "http://localhost:8084/uploads/archive/2030309010523144194/培训手册.pdf",
    "fileName": "培训手册.pdf",
    "fileSize": "2.5 MB",
    "fileType": "pdf"
  }
}
```

#### 删除资料

```
DELETE /api/meeting/{meetingId}/archive/materials/{materialId}
```

#### 增加下载次数

```
POST /api/meeting/{meetingId}/archive/materials/{materialId}/download-count
```

### 5.5 业务数据管理

#### 获取业务数据

```
GET /api/meeting/{meetingId}/archive/business-data?dataType=report_rate
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataType | String | 否 | report_rate(报到率) / checkin_rate(签到率) / dormitory_rate(查寝率) |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "200001",
      "meetingId": "2030309010523144194",
      "dataType": "report_rate",
      "dataLabel": "XX市公安局",
      "expectedCount": 5,
      "actualCount": 3,
      "rate": 60.00,
      "sortOrder": 1
    }
  ]
}
```

#### 添加单条业务数据

```
POST /api/meeting/{meetingId}/archive/business-data
```

**请求体**:
```json
{
  "dataType": "dormitory_rate",
  "dataLabel": "A栋301",
  "expectedCount": 4,
  "actualCount": 4,
  "rate": 100.00
}
```

#### 批量保存业务数据

```
POST /api/meeting/{meetingId}/archive/business-data/batch
```

**请求体**:
```json
{
  "dataType": "dormitory_rate",
  "items": [
    {
      "dataLabel": "A栋301",
      "expectedCount": 4,
      "actualCount": 4,
      "rate": 100.00
    },
    {
      "dataLabel": "A栋302",
      "expectedCount": 4,
      "actualCount": 3,
      "rate": 75.00
    }
  ]
}
```

### 5.6 消息群组管理

#### 获取消息群组列表

```
GET /api/meeting/{meetingId}/archive/message-groups
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "300001",
      "meetingId": "2030309010523144194",
      "groupName": "协管员工作群",
      "messageCount": 3,
      "lastActiveTimeFormatted": "2小时前",
      "previewMessages": [
        {
          "id": "400001",
          "content": "请注意今天的签到安排",
          "sender": "管理员",
          "sendTime": "2025-06-01T14:00:00",
          "messageType": "text"
        }
      ]
    }
  ]
}
```

#### 创建消息群组

```
POST /api/meeting/{meetingId}/archive/message-groups
```

**请求体**:
```json
{
  "groupName": "讨论群"
}
```

#### 获取群组消息

```
GET /api/meeting/{meetingId}/archive/message-groups/{groupId}/messages?keyword=签到
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键词(模糊匹配content和sender) |

#### 批量添加消息

```
POST /api/meeting/{meetingId}/archive/message-groups/{groupId}/messages
```

**请求体** (数组):
```json
[
  {
    "content": "收到，已确认",
    "sender": "张三",
    "sendTime": "2025-06-01T14:30:00",
    "messageType": "text"
  }
]
```

### 5.7 归档操作

#### 标记已打包

```
POST /api/meeting/{meetingId}/archive/pack
```

#### 清空全部归档数据

```
DELETE /api/meeting/{meetingId}/archive/clear
```

> ⚠️ **前置条件**: 必须先调用 `/pack` 标记打包后才能清空

### 5.8 数据导出

#### 导出业务数据CSV

```
GET /api/meeting/{meetingId}/archive/export/business-data?dataType=report_rate
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataType | String | 否 | 指定类型，不传则导出所有类型 |

**响应**: `Content-Type: text/csv; charset=UTF-8` (带BOM)

CSV示例:
```csv
序号,标签,应到人数,实到人数,比率
1,XX市公安局,5,3,60.00%
```

#### 导出消息CSV

```
GET /api/meeting/{meetingId}/archive/export/messages?groupId=300001
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| groupId | Long | 否 | 指定群组ID，不传则导出全部 |

CSV示例:
```csv
群组名称,发送者,消息内容,消息类型,发送时间
协管员工作群,管理员,请注意今天的签到安排,text,2025-06-01 14:00:00
```

---

## 六、报名服务

**服务**: conference-registration  
**端口**: 8082  
**基础路径**: `/api`

### 6.1 报名管理

```
POST   /api/registration/submit                # 提交报名
GET    /api/registration/{id}                   # 报名详情
GET    /api/registration/list?meetingId={id}&page=1&size=10  # 报名列表
GET    /api/registration/query?meetingId={id}&name=xxx       # 搜索报名
POST   /api/registration/audit                  # 审核报名
POST   /api/registration/batchAudit             # 批量审核
GET    /api/registration/export?meetingId={id}  # 导出报名
GET    /api/registration/stats?meetingId={id}   # 报名统计
```

**提交报名请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "name": "李四",
  "phone": "13900139000",
  "organization": "XX市公安局",
  "department": "刑侦大队",
  "position": "副大队长",
  "idCard": "110101199001011234",
  "gender": "男"
}
```

### 6.2 OCR识别

```
POST /api/registration/ocr/idCard           # OCR身份证
Content-Type: multipart/form-data
file: <File>

GET  /api/registration/ocr/result/{taskId}  # 获取OCR结果
```

### 6.3 文件上传

```
POST /api/registration/upload
Content-Type: multipart/form-data
file: <File>
```

### 6.4 表单配置

```
GET  /api/registration/fields?meetingId={id}         # 获取表单字段
POST /api/registration/formConfig                     # 保存表单配置
```

### 6.5 名册与二维码

```
POST /api/registration/pdf/roster              # 生成花名册PDF
GET  /api/registration/qr/generate?meetingId={id}  # 生成二维码
```

### 6.6 手册管理

```
GET  /api/handbook/{meetingId}                 # 获取手册
POST /api/handbook/save                        # 保存手册
GET  /api/handbook/roster?meetingId={id}       # 花名册
GET  /api/handbook/schedule?meetingId={id}     # 日程
GET  /api/handbook/materials?meetingId={id}    # 资料
GET  /api/handbook/groups?meetingId={id}       # 分组
POST /api/handbook/generate                    # 生成手册
```

### 6.7 分组管理

```
GET    /api/grouping/list?meetingId={id}       # 分组列表
POST   /api/grouping/save                      # 保存分组
PUT    /api/grouping/updateName                # 更新分组名
PUT    /api/grouping/updateLeader              # 更新组长
POST   /api/grouping/adjustMembers             # 调整成员
DELETE /api/grouping/removeMember              # 移除成员
DELETE /api/grouping/deleteAll?meetingId={id}  # 删除全部分组
```

### 6.8 报名字段验证规则（补充）

| 字段 | 类型 | 必填 | 验证 | 说明 |
|------|------|------|------|------|
| meetingId | String | ✅ | @NotBlank | 会议ID |
| name | String | ✅ | @NotBlank @Size(max=50) | 姓名 |
| phone | String | ✅ | @NotBlank @Pattern | 手机号 |
| organization | String | ❌ | @Size(max=100) | 单位 |
| department | String | ❌ | @Size(max=100) | 部门 |
| position | String | ❌ | @Size(max=50) | 职务 |
| idCard | String | ❌ | @Size(max=18) | 身份证号 |
| gender | String | ❌ | | 性别（男/女） |
| email | String | ❌ | @Email | 邮箱 |
| dynamicFields | Map | ❌ | | 动态表单字段（JSON存储） |

### 6.9 审核接口详情（补充）

**单条审核请求体**:
```json
{
  "registrationId": "100001",
  "status": 1,
  "auditComment": "审核通过"
}
```

**批量审核请求体**:
```json
{
  "registrationIds": ["100001", "100002", "100003"],
  "status": 1,
  "auditComment": "批量通过"
}
```

**报名状态值**:

| 值 | 含义 |
|----|------|
| 0 | 待审核(pending) |
| 1 | 已通过(approved) |
| 2 | 已拒绝(rejected) |

> ⚠️ **类型不一致警告**: Entity 中 `status` 为 `Integer` 类型，但数据库 DDL 定义为 `VARCHAR(20)`，可能导致类型转换异常。

### 6.10 表单配置详情（补充）

**保存表单配置请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "fields": [
    {
      "fieldName": "bloodType",
      "fieldLabel": "血型",
      "fieldType": "select",
      "required": false,
      "options": ["A", "B", "AB", "O"],
      "sortOrder": 1
    }
  ]
}
```

**fieldType 可选值**: `text` / `select` / `radio` / `checkbox` / `date` / `textarea` / `file`

### 6.11 导出与花名册说明（补充）

**Excel导出**:
```
GET /api/registration/export?meetingId={id}
响应: Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

> ⚠️ 使用 Apache POI `XSSFWorkbook`（非流式），大数据量（>5000条）时内存占用较高。

**花名册PDF**:
```
POST /api/registration/pdf/roster
响应: Content-Type: application/pdf
```

> ⚠️ **当前实现返回空 byte 数组**，PDF 生成功能尚未完成。

### 6.12 分组管理详细请求体（补充）

**保存分组**:
```json
{
  "meetingId": "2030309010523144194",
  "groupName": "第一组",
  "leaderId": "user001",
  "memberIds": ["user001", "user002", "user003"]
}
```

**更新组名**:
```json
{
  "groupId": "500001",
  "groupName": "第一组（改名）"
}
```

**调整成员**:
```json
{
  "groupId": "500001",
  "addMemberIds": ["user004"],
  "removeMemberIds": ["user003"]
}
```

> ⚠️ **架构问题**: `GroupingController`（453行）直接使用 `JdbcTemplate` 拼接 SQL，未使用 Service 层和 MyBatis-Plus。分组列表查询存在 N+1 问题（10个分组执行 21 次 SQL）。

---

## 七、通知服务

**服务**: conference-notification  
**端口**: 8083  
**基础路径**: `/api/notification`

### 7.1 通知管理

```
POST   /api/notification/send                  # 发送通知
POST   /api/notification/draft                 # 保存草稿
POST   /api/notification/urge                  # 催办
GET    /api/notification/list?meetingId={id}   # 通知列表
GET    /api/notification/{id}                  # 通知详情
DELETE /api/notification/{id}                  # 删除通知
GET    /api/notification/stats?meetingId={id}  # 通知统计
```

**发送通知请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "templateId": "TPL001",
  "title": "会议报名提醒",
  "content": "请尽快完成报名",
  "channel": "sms",
  "recipients": ["user001", "user002"],
  "scheduledTime": "2025-06-01T08:00:00"
}
```

### 7.2 通知模板

```
GET    /api/notification/templates?meetingId={id}  # 模板列表
GET    /api/notification/template/{id}             # 模板详情
POST   /api/notification/template                  # 保存模板
DELETE /api/notification/template/{id}             # 删除模板
```

### 7.3 通知渠道类型（补充）

| channel | 说明 | 实现状态 |
|---------|------|----------|
| sms | 短信 | ⚠️ 模拟（仅log输出） |
| email | 邮件 | ⚠️ 模拟（仅log输出） |
| wechat | 微信 | ⚠️ 模拟（仅log输出） |
| push | APP推送 | ⚠️ 模拟（仅log输出） |
| in_app | 站内消息 | ⚠️ 模拟（仅log输出） |

> ⚠️ **关键说明**: 所有通知渠道均为**纯模拟实现**，`NotificationServiceImpl.doSend()` 仅将发送行为输出到日志，未接入任何实际短信/邮件/微信网关。

### 7.4 通知发送字段详细说明（补充）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| meetingId | String | ✅ | 会议ID |
| templateId | String | ❌ | 模板ID（使用模板时填写） |
| title | String | ✅ | 通知标题 |
| content | String | ✅ | 通知内容 |
| channel | String | ✅ | 发送渠道 |
| recipients | List | ✅ | 接收人ID列表 |
| scheduledTime | DateTime | ❌ | 定时发送时间 |

> ⚠️ `scheduledTime` 设置后仅保存到数据库，无调度器（Scheduler）执行延时发送，定时通知实际不会自动触发。

### 7.5 通知状态流转（补充）

| 状态 | 说明 |
|------|------|
| draft | 草稿 |
| pending | 待发送 |
| sending | 发送中 |
| sent | 已发送 |
| failed | 发送失败 |
| cancelled | 已取消 |

### 7.6 模板变量格式（补充）

模板内容支持 `{{variableName}}` 格式占位符：

```json
{
  "name": "会议提醒模板",
  "content": "尊敬的{{name}}，您报名的{{meetingName}}将于{{startTime}}开始，请准时参加。",
  "channel": "sms",
  "variables": ["name", "meetingName", "startTime"]
}
```

> ⚠️ **模板变量替换未实现**: 代码中模板变量解析逻辑为空，发送时直接使用原始模板内容（包含 `{{xxx}}` 占位符）。

---

## 八、预警服务

**服务**: conference-notification  
**端口**: 8083  
**基础路径**: `/api/alert`

### 8.1 预警规则

```
GET    /api/alert/rules?meetingId={id}         # 规则列表
POST   /api/alert/rule                         # 新建规则
PUT    /api/alert/rule/{id}                    # 更新规则
DELETE /api/alert/rule/{id}                    # 删除规则
PUT    /api/alert/rule/{id}/toggle             # 启用/禁用规则
GET    /api/alert/metrics                      # 可用指标列表
```

### 8.2 预警事件

```
GET    /api/alert/list?meetingId={id}          # 预警列表
PUT    /api/alert/{id}/process                 # 处理预警
PUT    /api/alert/{id}/resolve                 # 解决预警
GET    /api/alert/stats?meetingId={id}         # 预警统计
```

### 8.3 预警指标类型（补充）

| metricType | 说明 | 数据来源 |
|------------|------|----------|
| registration_rate | 报名率 | 跨库查询 registration |
| checkin_rate | 签到率 | 跨库查询 schedule_checkin |
| material_completion | 材料完成率 | 内部计算 |
| feedback_score | 反馈评分 | 内部计算 |
| custom | 自定义指标 | 手动触发 |

### 8.4 预警规则请求体（补充）

```json
{
  "meetingId": "2030309010523144194",
  "name": "报名率预警",
  "metricType": "registration_rate",
  "condition": "less_than",
  "threshold": 80.0,
  "severity": "warning",
  "notifyChannels": ["sms", "email"],
  "enabled": true
}
```

**condition 条件值**: `greater_than` / `less_than` / `equal_to` / `not_equal_to`

**severity 严重级别**: `info` / `warning` / `critical`

### 8.5 预警生命周期（补充）

| 状态 | 说明 |
|------|------|
| triggered | 已触发 |
| processing | 处理中 |
| resolved | 已解决 |
| expired | 已过期 |

**处理预警请求体**:
```json
{
  "handler": "张三",
  "handleNote": "已通知相关人员加快报名"
}
```

**解决预警请求体**:
```json
{
  "resolver": "张三",
  "resolveNote": "报名率已达标"
}
```

---

## 九、协同-群组服务

**服务**: conference-collaboration  
**端口**: 8089  
**基础路径**: `/api/group`

```
POST   /api/group/create                       # 创建群组
GET    /api/group/list?meetingId={id}          # 群组列表
GET    /api/group/my-groups?meetingId={id}     # 我的群组
GET    /api/group/{id}                         # 群组详情
DELETE /api/group/{id}                         # 删除群组
GET    /api/group/{id}/members                 # 成员列表
POST   /api/group/{id}/members                 # 添加成员
DELETE /api/group/{id}/members/{userId}        # 移除成员
PUT    /api/group/{id}/members/{userId}/role   # 更新成员角色
PUT    /api/group/{id}/settings                # 更新群组设置
GET    /api/group/{id}/stats                   # 群组统计
```

**创建群组请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "name": "协管员工作群",
  "type": "work",
  "description": "协管员日常沟通群"
}
```

### 9.2 群组类型与角色（补充）

**群组类型 (type)**:

| 值 | 说明 |
|------|------|
| work | 工作群 |
| discussion | 讨论群 |
| notification | 通知群 |

**成员角色 (role)**:

| 值 | 说明 |
|------|------|
| owner | 群主 |
| admin | 管理员 |
| member | 普通成员 |

### 9.3 添加成员请求体（补充）

```json
{
  "userIds": ["user003", "user004"],
  "role": "member"
}
```

### 9.4 群组设置结构（补充）

```json
{
  "allowMemberInvite": true,
  "allowMemberChat": true,
  "muteAll": false,
  "announcement": "请注意会议安排变更"
}
```

### 9.5 群组详情响应（补充）

```json
{
  "code": 200,
  "data": {
    "id": "300001",
    "meetingId": "2030309010523144194",
    "name": "协管员工作群",
    "type": "work",
    "description": "协管员日常沟通群",
    "memberCount": 15,
    "creatorId": "user001",
    "createTime": "2025-06-01T10:00:00"
  }
}
```

---

## 十、协同-任务服务

**服务**: conference-collaboration  
**端口**: 8089  
**基础路径**: `/api/task`

```
POST   /api/task/create                        # 创建任务
DELETE /api/task/{id}                          # 删除任务
PUT    /api/task/{id}                          # 更新任务
GET    /api/task/{id}                          # 任务详情
GET    /api/task/list?meetingId={id}           # 任务列表
GET    /api/task/my-tasks?meetingId={id}       # 我的任务
POST   /api/task/{id}/assign                   # 分配任务
GET    /api/task/{id}/assignees                # 任务执行人
POST   /api/task/{id}/submit                   # 提交任务
POST   /api/task/{id}/complete                 # 完成任务
POST   /api/task/{id}/cancel                   # 取消任务
POST   /api/task/{id}/urge                     # 催办任务
GET    /api/task/{id}/logs                     # 任务日志
GET    /api/task/stats?meetingId={id}          # 任务统计
```

**创建任务请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "title": "布置主会场",
  "description": "完成主会场桌椅摆放、投影设备调试",
  "priority": "high",
  "deadline": "2025-06-30T18:00:00",
  "assigneeIds": ["user001", "user002"]
}
```

### 10.2 任务状态机（补充）

```
pending → in_progress → submitted → completed
  ↓          ↓            ↓
cancelled  cancelled    rejected → in_progress
```

| status | 说明 |
|--------|------|
| pending | 待处理 |
| in_progress | 进行中 |
| submitted | 已提交 |
| completed | 已完成 |
| cancelled | 已取消 |

**priority 优先级**: `low` / `normal` / `high` / `urgent`

### 10.3 分配任务请求体（补充）

```json
{
  "assigneeIds": ["user003", "user004"]
}
```

### 10.4 提交任务请求体（补充）

```json
{
  "submitNote": "已完成主会场布置，详见附件照片",
  "attachments": ["file001.jpg", "file002.jpg"]
}
```

> ⚠️ **实现说明**:
> - 任务逾期自动检测未实现，需手动检查截止时间
> - 催办接口 `POST /api/task/{id}/urge` 仅更新数据库记录，不发送实际通知

---

## 十一、协同-聊天服务

**服务**: conference-collaboration  
**端口**: 8089  
**基础路径**: `/api/chat`

```
POST /api/chat/send                            # 发送消息
GET  /api/chat/group/{groupId}/messages?page=1&size=50  # 群组消息
GET  /api/chat/private/{userId}/messages       # 私信消息
GET  /api/chat/search?keyword=xxx&meetingId={id}  # 搜索消息
GET  /api/chat/stats?meetingId={id}            # 聊天统计
```

**WebSocket连接**:
```
ws://localhost:8089/ws/chat?token=xxx&userId=xxx
```

**发送消息请求体**:
```json
{
  "groupId": "300001",
  "content": "收到，马上处理",
  "messageType": "text"
}
```

### 11.2 WebSocket 事件详细说明（补充）

**连接地址**:
```
ws://localhost:8089/ws/chat?userId={userId}
```

> ⚠️ **安全警告**: WebSocket 连接仅通过 URL 参数传递 `userId`，无 Token 验证，任何人可伪造身份发送消息。

**WebSocket 消息事件类型**:

| type | 方向 | 说明 |
|------|------|------|
| join_group | 客户端→服务端 | 加入群组 |
| leave_group | 客户端→服务端 | 离开群组 |
| chat_message | 双向 | 聊天消息 |
| typing | 客户端→服务端 | 正在输入提示 |
| heartbeat | 双向 | 心跳保活 |

**WebSocket 消息格式**:
```json
{
  "type": "chat_message",
  "groupId": "300001",
  "content": "收到，马上处理",
  "messageType": "text",
  "timestamp": "2025-06-01T14:30:00"
}
```

**消息类型 (messageType)**:

| 值 | 说明 |
|----|------|
| text | 文本消息 |
| image | 图片消息 |
| file | 文件消息 |
| system | 系统消息 |

> ⚠️ **单机限制**: WebSocket 会话存储在 JVM 内存中（`ConcurrentHashMap`），不支持多实例集群部署，未读消息计数始终返回 0。

### 11.3 协同资料管理（补充）

> ⚡ 以下 8 个端点在原始文档中**完全缺失**，实际代码中已实现

```
POST   /api/material/upload                    # 上传资料
GET    /api/material/list?meetingId={id}        # 资料列表
GET    /api/material/{id}                       # 资料详情
DELETE /api/material/{id}                       # 删除资料
GET    /api/material/{id}/download              # 下载资料
GET    /api/material/categories                 # 资料分类列表
PUT    /api/material/{id}/category              # 更新资料分类
GET    /api/material/stats?meetingId={id}       # 资料统计
```

**上传资料请求**:
```
POST /api/material/upload
Content-Type: multipart/form-data

file: <File>
meetingId: 2030309010523144194
category: document
```

---

## 十二、排座服务

**服务**: conference-seating  
**端口**: 8086  
**基础路径**: `/api/seating`

### 12.1 会场管理

```
POST   /api/seating/venues                     # 创建会场
GET    /api/seating/venues?meetingId={id}      # 会场列表
GET    /api/seating/venues/{id}                # 会场详情
PUT    /api/seating/venues/{id}                # 更新会场
DELETE /api/seating/venues/{id}                # 删除会场
GET    /api/seating/venues/{id}/stats          # 会场统计
```

### 12.2 座位管理

```
POST /api/seating/seats/assign                 # 分配座位
POST /api/seating/seats/auto-assign            # 智能自动分配
POST /api/seating/seats/batch                  # 批量操作
GET  /api/seating/seats?venueId={id}           # 座位列表
```

### 12.3 布局管理

```
POST /api/seating/layout/save                  # 保存布局
GET  /api/seating/layout/{venueId}             # 获取布局
```

### 12.4 用餐安排

```
POST /api/seating/dining                       # 创建用餐安排
GET  /api/seating/dining?meetingId={id}        # 用餐安排列表
```

### 12.5 住宿安排

```
POST /api/seating/accommodation                # 创建住宿安排
GET  /api/seating/accommodation?meetingId={id} # 住宿安排列表
```

### 12.6 交通安排

```
POST /api/seating/transport                    # 创建交通安排
GET  /api/seating/transport?meetingId={id}     # 交通安排列表
```

### 12.7 创建会场请求体（补充）

```json
{
  "meetingId": "2030309010523144194",
  "name": "主会场A厅",
  "type": "conference",
  "capacity": 200,
  "rows": 10,
  "columns": 20,
  "description": "可容纳200人的大型会议厅"
}
```

### 12.8 布局保存请求体（补充）

> ⚡ 布局保存为**复杂嵌套结构**，是排座模块最核心的接口

```json
{
  "venueId": "600001",
  "meetingId": "2030309010523144194",
  "layoutData": {
    "areas": [
      {
        "areaId": "area_1",
        "areaName": "VIP区",
        "areaType": "vip",
        "rows": 3,
        "columns": 10,
        "seats": [
          {
            "seatId": "seat_1_1",
            "row": 1,
            "column": 1,
            "label": "A-1",
            "status": "available",
            "attendeeId": null
          }
        ]
      }
    ],
    "aisles": [
      {"afterRow": 3, "type": "horizontal"},
      {"afterColumn": 5, "type": "vertical"}
    ],
    "attendees": [
      {
        "attendeeId": "user001",
        "name": "张三",
        "organization": "XX局",
        "seatId": "seat_1_1"
      }
    ]
  }
}
```

### 12.9 智能排座算法（补充）

```
POST /api/seating/seats/auto-assign
```

**请求体**:
```json
{
  "venueId": "600001",
  "meetingId": "2030309010523144194",
  "algorithm": "sequential"
}
```

**algorithm 可选值**:

| 值 | 说明 | 实现状态 |
|----|------|----------|
| sequential | 顺序分配 | ❌ 空实现（return new Object()） |
| group_based | 按分组分配 | ❌ 空实现 |
| priority_based | 按优先级分配 | ❌ 空实现 |

> ⚠️ **关键警告**: 三种排座算法类（`SequentialAssignment` / `GroupBasedAssignment` / `PriorityBasedAssignment`）均为**空壳**，`assign()` 方法直接 `return new Object()`。智能排座功能实际**不可用**。

### 12.10 用餐/住宿/交通请求体（补充）

**用餐安排**:
```json
{
  "meetingId": "2030309010523144194",
  "mealType": "lunch",
  "date": "2025-07-01",
  "location": "餐厅A",
  "capacity": 100,
  "assignedCount": 0
}
```

**住宿安排**:
```json
{
  "meetingId": "2030309010523144194",
  "roomNumber": "A301",
  "roomType": "standard",
  "building": "A栋",
  "floor": 3,
  "capacity": 2,
  "assignedCount": 0
}
```

**交通安排**:
```json
{
  "meetingId": "2030309010523144194",
  "vehicleType": "bus",
  "plateNumber": "京A12345",
  "route": "机场→酒店",
  "departureTime": "2025-07-01T08:00:00",
  "capacity": 45,
  "assignedCount": 0
}
```

> ⚠️ **实现限制**:
> - 用餐/住宿/交通的 `assignAttendee` 接口仅更改 `assignedCount` 计数，**不记录具体分配了哪些人员**
> - Entity 字段名与数据库 Schema 存在 6+ 处不匹配（如 `venueId` vs `venue_id` 等）
> - 时区使用 `UTC`，与其他模块的 `Asia/Shanghai` 不一致

---

## 十三、AI服务

**服务**: conference-ai  
**端口**: 8085  
**基础路径**: `/api/ai`

### 13.1 智能聊天

```
POST /api/ai/chat                              # AI对话
GET  /api/ai/conversations?meetingId={id}      # 对话列表
```

**聊天请求体**:
```json
{
  "meetingId": "2030309010523144194",
  "conversationId": "conv001",
  "message": "这次会议有多少人参加？",
  "contextType": "meeting"
}
```

### 13.2 对话管理

```
GET    /api/ai/conversation/{id}               # 对话详情
PUT    /api/ai/conversation/{id}/title         # 更新标题
DELETE /api/ai/conversation/{id}               # 删除对话
GET    /api/ai/conversation/{id}/messages      # 对话消息
```

### 13.3 知识库

```
GET    /api/ai/knowledge?meetingId={id}        # 知识库列表
POST   /api/ai/knowledge                       # 添加知识
POST   /api/ai/knowledge/upload                # 上传文档
DELETE /api/ai/knowledge/{id}                  # 删除知识
```

### 13.4 FAQ

```
GET    /api/ai/faq?meetingId={id}              # FAQ列表
POST   /api/ai/faq                             # 添加FAQ
DELETE /api/ai/faq/{id}                        # 删除FAQ
```

### 13.5 反馈与统计

```
POST /api/ai/feedback                          # 提交反馈
GET  /api/ai/feedback/list?meetingId={id}      # 反馈列表
GET  /api/ai/stats?meetingId={id}              # AI统计
GET  /api/ai/capabilities                      # AI能力列表
GET  /api/ai/contexts?meetingId={id}           # 上下文列表
```

### 13.6 AI对话完整处理流程（补充）

AI 对话处理链（8步）：

1. 创建/获取对话(Conversation)
2. 保存用户消息
3. 查询FAQ匹配 → 如命中直接返回（views+1）
4. 查询知识库匹配 → 关键词匹配
5. 查询业务数据 → 跨库查询报名/日程等
6. 生成智能回复（基于模板，非LLM）
7. 保存AI回复消息
8. 更新对话最后活动时间

> ⚠️ **关键说明**:
> - AI模块**未集成任何大语言模型**（无OpenAI/ChatGPT/文心一言等API调用）
> - "智能回复"基于关键词匹配 + 预设模板生成
> - 知识库搜索将全部条目加载到内存后 Java `stream().filter()` 过滤，非数据库模糊查询

### 13.7 AI对话响应详情（补充）

```json
{
  "code": 200,
  "data": {
    "conversationId": "conv001",
    "messageId": "msg002",
    "answer": "根据系统数据，本次会议共有150人报名参加。",
    "answerType": "business_data",
    "confidence": 0.85,
    "sources": [
      {
        "type": "registration",
        "description": "报名统计数据"
      }
    ],
    "suggestedQuestions": [
      "报名通过率是多少？",
      "哪个单位报名人数最多？"
    ]
  }
}
```

**answerType 类型**:

| 值 | 说明 |
|----|------|
| faq | 来自FAQ匹配 |
| knowledge | 来自知识库 |
| business_data | 来自业务数据查询 |
| template | 来自预设模板 |
| fallback | 兜底回复 |

### 13.8 contextType 上下文类型（补充）

| 值 | 说明 |
|----|------|
| meeting | 会议相关问答 |
| registration | 报名相关问答 |
| schedule | 日程相关问答 |
| seating | 排座相关问答 |
| general | 通用问答 |

### 13.9 知识库上传详情（补充）

```
POST /api/ai/knowledge/upload
Content-Type: multipart/form-data

file: <File>
meetingId: 2030309010523144194
```

支持格式: `.txt` / `.pdf` / `.docx`

> ⚠️ **PDF和DOCX解析为空实现**: 仅 `.txt` 文件实际读取内容，PDF/DOCX 的 `parseDocument()` 返回空字符串。

### 13.10 OCR与语音识别（补充）

```
POST /api/ai/ocr/recognize
Content-Type: multipart/form-data
file: <image_file>
```

```
POST /api/ai/speech/recognize
Content-Type: multipart/form-data
file: <audio_file>
```

> ⚠️ **均为空壳实现**:
> - OCR 返回固定字符串 `"OCR识别结果"`
> - 语音识别返回固定字符串 `"语音识别结果"`
> - 未集成任何 OCR 引擎（如 Tesseract、百度OCR等）或语音识别服务

---

## 十四、数据服务

**服务**: conference-data  
**端口**: 8088  
**基础路径**: `/api/data`

### 14.1 系统监控

```
GET /api/data/system/overview                  # 系统总览
GET /api/data/system/cpu                       # CPU使用率
GET /api/data/system/memory                    # 内存使用率
GET /api/data/system/disk                      # 磁盘使用率
GET /api/data/system/network                   # 网络状态
GET /api/data/system/top-endpoints             # 高频端点
GET /api/data/system/response-times            # 响应时间
GET /api/data/system/error-rates               # 错误率
GET /api/data/system/events                    # 系统事件
GET /api/data/system/user-activities           # 用户活动
```

### 14.2 系统日志

```
GET  /api/data/logs?page=1&size=50             # 日志列表
POST /api/data/logs                            # 写入日志
```

### 14.3 业务数据统计

```
GET  /api/data/business/overview?meetingId={id}    # 业务总览
GET  /api/data/business/registration?meetingId={id} # 报名统计
GET  /api/data/business/checkin?meetingId={id}     # 签到统计
GET  /api/data/business/notification?meetingId={id} # 通知统计
GET  /api/data/business/task?meetingId={id}        # 任务统计
GET  /api/data/business/trend?meetingId={id}       # 趋势数据
GET  /api/data/business/comparison                 # 横向对比
GET  /api/data/business/export?meetingId={id}      # 数据导出
```

### 14.4 数据大屏

```
GET /api/data/dashboard?meetingId={id}         # 大屏数据
GET /api/data/warning?meetingId={id}           # 预警数据
GET /api/data/ranking?meetingId={id}           # 排名数据
```

### 14.5 系统总览响应详情（补充）

```json
{
  "code": 200,
  "data": {
    "cpuUsage": 45.2,
    "memoryUsage": 68.5,
    "memoryUsed": "5.4GB",
    "memoryTotal": "8.0GB",
    "diskUsage": 72.3,
    "diskUsed": "145GB",
    "diskTotal": "200GB",
    "jvmHeapUsed": "256MB",
    "jvmHeapMax": "512MB",
    "threadCount": 42,
    "uptime": "3天14小时",
    "osName": "Windows 10",
    "javaVersion": "21.0.1"
  }
}
```

> ⚠️ 网络监控数据使用 `Math.random()` 生成**虚假数据**，非真实网络流量。

### 14.6 遗漏端点补充

> ⚡ 以下端点在原始文档中未记录，实际代码中已实现

```
GET /api/data/health/check                     # 各服务健康检查
GET /api/data/health/services                  # 服务状态列表
GET /api/data/api/stats                        # API调用统计
GET /api/data/api/slow-endpoints               # 慢接口排名
```

### 14.7 业务数据响应详情（补充）

**业务总览响应**:
```json
{
  "code": 200,
  "data": {
    "totalMeetings": 5,
    "activeMeetings": 2,
    "totalRegistrations": 350,
    "approvedRegistrations": 280,
    "pendingRegistrations": 50,
    "rejectedRegistrations": 20,
    "totalNotifications": 120,
    "totalTasks": 45,
    "completedTasks": 30,
    "overdueTaskRate": 15.5
  }
}
```

> ⚠️ **跨库查询**: 业务数据统计通过 `JdbcTemplate` 直接跨 `conference_registration`、`conference_notification`、`conference_collaboration` 三个数据库查询，数据源在 `application.yml` 中硬编码。

### 14.8 数据导出说明（补充）

```
GET /api/data/business/export?meetingId={id}&format=excel
```

**format 可选值**: `excel` / `csv` / `pdf`

| format | 说明 | 实现状态 |
|--------|------|----------|
| excel | Excel文件 | ✅ 已实现 |
| csv | CSV文件 | ✅ 已实现 |
| pdf | PDF文件 | ❌ 返回空响应 |

---

## 附录A：API端点统计

| 服务 | Controller数 | 端点总数 | 补充说明 |
|------|-------------|---------|---------|
| conference-auth | 2 | 11 | 含7个租户管理端点 |
| conference-meeting | 6 | 68 | 含7个分块上传端点(原文遗漏) |
| conference-registration | 3 | 30 | 含7个分组+7个手册端点 |
| conference-notification | 2 | 21 | 含10个预警端点 |
| conference-collaboration | 4 | 48 | 含8个资料管理端点(原文遗漏) |
| conference-seating | 1 | 18 | 含用餐/住宿/交通各2个 |
| conference-ai | 1 | 28 | 含OCR+语音各1个(原文遗漏) |
| conference-data | 1 | 27 | 含4个健康检查+API统计(原文遗漏) |
| **合计** | **20** | **≈251** | 比原统计多26个（原遗漏端点） |

## 附录B：端口映射表

| 端口 | 服务 | 说明 |
|------|------|------|
| 3308 | MySQL 9.6 | 数据库 |
| 8080 | conference-gateway | API网关 |
| 8081 | conference-auth | 认证授权 |
| 8082 | conference-registration | 报名服务 |
| 8083 | conference-notification | 通知预警 |
| 8084 | conference-meeting | 会议日程归档 |
| 8085 | conference-ai | AI服务 |
| 8086 | conference-seating | 排座服务 |
| 8087 | conference-navigation | 导航(已禁用) |
| 8088 | conference-data | 数据统计 |
| 8089 | conference-collaboration | 协同服务 |

## 附录C：数据库映射表

| 数据库名 | 使用服务 |
|---------|---------|
| conference_auth | Auth |
| conference_registration | Registration + Meeting |
| conference_notification | Notification |
| conference_seating | Seating |
| conference_collaboration | Collaboration |
| conference_db | AI + Data |
