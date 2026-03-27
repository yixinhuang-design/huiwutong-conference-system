# 后端API对接说明

## 📌 接口对接概述

后端服务采用微服务架构，通过网关统一访问（端口8080）。

### 服务端口分配
- **网关**: 8080
- **认证服务**: 8081
- **报名服务**: 8082
- **通知服务**: 8083
- **协作服务**: 8084
- **座位服务**: 8085
- **AI服务**: 8086
- **导航服务**: 8087
- **数据服务**: 8088

### 默认管理员账号
- 用户名: `admin`
- 密码: `admin123`

---

## 🔐 认证服务 (端口 8081)

### API文件
**`api/auth.js`**

### 接口列表

#### 1. 用户登录
```javascript
POST /auth/login
Content-Type: application/json

Request:
{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "id": "1",
      "username": "admin",
      "realName": "管理员",
      "tenantId": "1",
      "role": "super_admin"
    }
  }
}
```

#### 2. 刷新Token
```javascript
POST /auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}

Response:
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200
  }
}
```

#### 3. 获取当前用户信息
```javascript
GET /auth/me
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "1",
    "username": "admin",
    "realName": "管理员",
    "tenantId": "1",
    "tenantCode": "DEFAULT",
    "role": "super_admin"
  }
}
```

#### 4. 用户登出
```javascript
POST /auth/logout
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

## 🏢 租户管理 (认证服务)

### API文件
**`api/tenant.js`**

### 接口列表

#### 1. 获取租户列表
```javascript
GET /tenant/list?page=1&pageSize=10&searchName=&status=
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "获取租户列表成功",
  "data": {
    "total": 12,
    "items": [
      {
        "id": "1",
        "tenantCode": "ORG001",
        "tenantName": "市委组织部",
        "contactName": "张主任",
        "contactPhone": "13800138001",
        "contactEmail": "zhang@org.gov.cn",
        "tenantType": "government",
        "validStartDate": "2024-01-01",
        "validEndDate": "2025-12-31",
        "maxUsers": 100,
        "maxConferences": 50,
        "storageQuota": 100,
        "status": 1,
        "domain": "org.huiwutong.com",
        "logoUrl": "",
        "createTime": "2024-01-01T10:00:00"
      }
    ]
  }
}
```

#### 2. 获取租户详情
```javascript
GET /tenant/{tenantId}
Authorization: Bearer {token}
```

#### 3. 创建租户
```javascript
POST /tenant/create
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "tenantCode": "ORG002",
  "tenantName": "市教育局",
  "contactName": "李科长",
  "contactPhone": "13800138002",
  "contactEmail": "li@edu.gov.cn",
  "tenantType": "government",
  "validStartDate": "2024-01-01",
  "validEndDate": "2025-12-31",
  "maxUsers": 200,
  "maxConferences": 100,
  "storageQuota": 200,
  "domain": "edu.huiwutong.com"
}
```

#### 4. 更新租户
```javascript
PUT /tenant/{tenantId}
Authorization: Bearer {token}
```

#### 5. 删除租户
```javascript
DELETE /tenant/{tenantId}
Authorization: Bearer {token}
```

#### 6. 切换租户状态
```javascript
PUT /tenant/{tenantId}/status?status=active
Authorization: Bearer {token}

// status: active(启用) | inactive(禁用)
```

#### 7. 获取租户统计
```javascript
GET /tenant/{tenantId}/stats
Authorization: Bearer {token}
```

---

## 📝 报名服务 (端口 8082)

### API文件
**`api/registration.js`**

### 接口列表

#### 1. 提交报名
```javascript
POST /api/registration/submit
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "conferenceId": 1,
  "registrationData": {
    "name": "张三",
    "phone": "13800138001",
    "idCard": "110101199001011234",
    "gender": "male",
    "department": "市教育局",
    "position": "科长"
  }
}
```

#### 2. 获取报名详情
```javascript
GET /api/registration/{id}
Authorization: Bearer {token}
```

#### 3. 分页查询报名列表
```javascript
GET /api/registration/list?conferenceId=1&page=1&pageSize=10
Authorization: Bearer {token}
```

#### 4. 根据手机号查询报名状态
```javascript
GET /api/registration/query?conferenceId=1&phone=13800138001
Authorization: Bearer {token}
```

#### 5. 审核报名
```javascript
POST /api/registration/audit
Authorization: Bearer {token}
X-User-Id: {userId}

Request:
{
  "registrationId": 1,
  "status": "approved",  // approved | rejected
  "remark": "审核通过"
}
```

#### 6. 批量审核报名
```javascript
POST /api/registration/batchAudit
Authorization: Bearer {token}
X-User-Id: {userId}

Request:
[
  {
    "registrationId": 1,
    "status": "approved",
    "remark": "审核通过"
  }
]
```

#### 7. 导出报名名册（Excel）
```javascript
GET /api/registration/export?conferenceId=1
Authorization: Bearer {token}

Response: 下载Excel文件
```

#### 8. 获取报名统计
```javascript
GET /api/registration/stats?conferenceId=1
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "pending": 20,
    "approved": 75,
    "rejected": 5,
    "checkedIn": 50
  }
}
```

#### 9. OCR识别身份证
```javascript
POST /api/registration/ocr/idCard
Authorization: Bearer {token}

Request:
{
  "photoUrl": "http://example.com/idcard.jpg"
}

Response:
{
  "code": 200,
  "message": "识别成功",
  "data": {
    "name": "张三",
    "idCard": "110101199001011234",
    "gender": "男",
    "nation": "汉",
    "birthDate": "1990-01-01",
    "address": "北京市东城区xxx"
  }
}
```

#### 10. 上传报名附件
```javascript
POST /api/registration/upload
Authorization: Bearer {token}

FormData:
- conferenceId: 1
- fileType: "idCard" | "diploma"
- file: (二进制文件)

Response:
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "http://localhost:8080/uploads/xxx.jpg",
    "filename": "xxx.jpg"
  }
}
```

#### 11. 获取动态报名字段配置
```javascript
GET /api/registration/form/fields?conferenceId=1
Authorization: Bearer {token}
```

#### 12. 保存动态报名字段配置
```javascript
POST /api/registration/form/fields/save
Authorization: Bearer {token}
```

#### 13. 获取OCR规则校验结果
```javascript
GET /api/registration/ocr/result?registrationId=1
Authorization: Bearer {token}
```

#### 14. 生成PDF名册
```javascript
POST /api/registration/pdf/roster?conferenceId=1&coverImage=&remarks=
Authorization: Bearer {token}

Response: 下载PDF文件
```

#### 15. 生成会议注册二维码
```javascript
GET /api/registration/qr/generate?conferenceId=1&registrationUrl=
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "QR码生成成功",
  "data": "data:image/png;base64,..."
}
```

---

## 🔔 通知服务 (端口 8083)

### API文件
**`api/notification.js`**

### 接口列表

#### 1. 发送通知
```javascript
POST /api/notification/send
Authorization: Bearer {token}

Request:
{
  "conferenceId": 1,
  "channel": "sms",  // sms | email | app | wechat
  "content": "通知内容"
}
```

#### 2. 获取通知统计
```javascript
GET /api/notification/stats?conferenceId=1
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "sent": 95,
    "failed": 5
  }
}
```

#### 3. 发送催报通知
```javascript
POST /api/notification/urge
Authorization: Bearer {token}
```

---

## 💺 座位服务 (端口 8085)

### API文件
**`api/seating.js`**

### 会场管理

#### 1. 获取会场列表
```javascript
GET /api/seating/venues/{conferenceId}
Authorization: Bearer {token}
```

#### 2. 获取会场详情
```javascript
GET /api/seating/venues/detail/{venueId}
Authorization: Bearer {token}
```

#### 3. 创建会场
```javascript
POST /api/seating/venues
Authorization: Bearer {token}
```

#### 4. 更新会场
```javascript
PUT /api/seating/venues/{venueId}
Authorization: Bearer {token}
```

#### 5. 删除会场
```javascript
DELETE /api/seating/venues/{venueId}
Authorization: Bearer {token}
```

#### 6. 获取会场座位统计
```javascript
GET /api/seating/venues/stats/{venueId}
Authorization: Bearer {token}
```

### 座位管理

#### 7. 获取座位列表
```javascript
GET /api/seating/seats?venueId=1
Authorization: Bearer {token}
```

#### 8. 分配座位
```javascript
POST /api/seating/seats/assign
Authorization: Bearer {token}

Request:
{
  "venueId": 1,
  "seatId": "A-01",
  "registrationId": 1
}
```

#### 9. 交换座位
```javascript
POST /api/seating/seats/swap
Authorization: Bearer {token}

Request:
{
  "venueId": 1,
  "seatId1": "A-01",
  "seatId2": "A-02"
}
```

#### 10. 获取座位统计
```javascript
GET /api/seating/seats/stats/{venueId}
Authorization: Bearer {token}
```

### 布局管理

#### 11. 保存座位布局
```javascript
POST /api/seating/layout/save
Authorization: Bearer {token}
```

#### 12. 加载座位布局
```javascript
GET /api/seating/layout/load?conferenceId=1&scheduleId=
Authorization: Bearer {token}
```

### 辅助服务

#### 13. 获取用餐列表
```javascript
GET /api/seating/dinings/{conferenceId}
Authorization: Bearer {token}
```

#### 14. 创建用餐
```javascript
POST /api/seating/dinings
Authorization: Bearer {token}
```

#### 15. 获取住宿列表
```javascript
GET /api/seating/accommodations/{conferenceId}
Authorization: Bearer {token}
```

#### 16. 创建住宿
```javascript
POST /api/seating/accommodations
Authorization: Bearer {token}
```

#### 17. 获取车辆列表
```javascript
GET /api/seating/transports/{conferenceId}
Authorization: Bearer {token}
```

#### 18. 创建车辆
```javascript
POST /api/seating/transports
Authorization: Bearer {token}
```

---

## 📌 请求头说明

所有需要在请求头中携带的公共信息：

### 1. 认证Token
```javascript
Authorization: Bearer {token}
```

### 2. 租户ID（多租户系统）
```javascript
X-Tenant-Id: {tenantId}
```

### 3. 用户ID
```javascript
X-User-Id: {userId}
```

### 4. Content-Type
```javascript
Content-Type: application/json
```

---

## 📤 响应格式

### 成功响应
```javascript
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误响应
```javascript
{
  "code": 400,  // 或其他错误码
  "message": "错误描述",
  "data": null
}
```

---

## 🔧 工具函数

### Token管理
```javascript
import { getToken, setToken, removeToken } from '@/utils/auth'

// 获取Token
const token = getToken()

// 设置Token
setToken('your-token')

// 移除Token
removeToken()
```

### 用户信息管理
```javascript
import { getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { getTenantId, setTenantId, removeTenantId } from '@/utils/auth'
import { getUserId, setUserId, removeUserId } from '@/utils/auth'

// 获取用户信息
const userInfo = getUserInfo()

// 保存用户信息
setUserInfo(userInfo)

// 获取租户ID
const tenantId = getTenantId()

// 保存租户ID
setTenantId('1')

// 获取用户ID
const userId = getUserId()
```

---

## ✅ 已完成的API对接

### 1. 认证服务 ✅
- ✅ 登录接口
- ✅ 刷新Token
- ✅ 获取用户信息
- ✅ 登出

### 2. 租户管理 ✅
- ✅ 租户列表
- ✅ 租户详情
- ✅ 创建租户
- ✅ 更新租户
- ✅ 删除租户
- ✅ 切换租户状态
- ✅ 租户统计

### 3. 报名服务 ✅
- ✅ 提交报名
- ✅ 报名详情
- ✅ 报名列表（分页）
- ✅ 手机号查询
- ✅ 审核报名
- ✅ 批量审核
- ✅ 导出名册
- ✅ 报名统计
- ✅ OCR识别
- ✅ 上传附件
- ✅ 动态字段配置
- ✅ OCR校验结果
- ✅ PDF名册生成
- 二维码生成

### 4. 通知服务 ✅
- ✅ 发送通知
- ✅ 通知统计
- ✅ 催报通知

### 5. 座位服务 ✅
- ✅ 会场管理（6个接口）
- ✅ 座位管理（4个接口）
- ✅ 布局管理（2个接口）
- ✅ 用餐管理（2个接口）
- ✅ 住宿管理（2个接口）
- ✅ 车辆管理（2个接口）

---

## 🚀 使用示例

### 登录
```javascript
import auth from '@/api/auth'

// 密码登录
const res = await auth.login({
  username: 'admin',
  password: 'admin123'
})

console.log(res.token)
console.log(res.userInfo)
```

### 获取租户列表
```javascript
import tenant from '@/api/tenant'

const list = await tenant.list({
  page: 1,
  pageSize: 10,
  searchName: '组织部',
  status: 'active'
})
```

### 提交报名
```javascript
import registration from '@/api/registration'

const res = await registration.submit({
  conferenceId: 1,
  registrationData: {
    name: '张三',
    phone: '13800138001',
    idCard: '110101199001011234',
    gender: 'male',
    department: '市教育局',
    position: '科长'
  }
})
```

### 发送通知
```javascript
import notification from '@/api/notification'

await notification.send(1, 'sms', '您的报名已审核通过')
```

### 获取座位列表
```javascript
import seating from '@/api/seating'

const seats = await seating.getSeats(1)
```

---

## ⚠️ 注意事项

1. **Token管理**：所有需要认证的接口必须在请求头中携带 `Authorization: Bearer {token}`

2. **多租户**：通过 `X-Tenant-Id` 请求头指定租户

3. **用户上下文**：通过 `X-User-Id` 请求头传递当前操作用户

4. **错误处理**：所有错误响应会自动显示Toast提示，401错误会自动跳转登录页

5. **文件上传/下载**：使用封装好的 `upload()` 和 `download()` 方法

6. **大整数问题**：后端返回的ID已转换为字符串，避免JavaScript精度丢失

7. **Base地址**：所有请求通过网关 `http://localhost:8080` 统一访问

---

## 📝 后续对接计划

等待后端完成以下接口后，继续对接：

1. **会议管理服务** - 会议CRUD、日程管理
2. **协作服务** - 群组、聊天室
3. **数据服务** - 统计分析、报表导出
4. **AI服务** - 智能推荐、问答

---

**最后更新时间**: 2026-03-14
**API版本**: v1.0
