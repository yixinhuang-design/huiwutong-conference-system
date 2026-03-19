# API 400 错误诊断完整指南

## 问题现象

在tenant-management.html 页面加载时，调用 `/api/tenant/list` 返回 **400 Bad Request** 错误，尽管：
- ✅ 已登录admin账号  
- ✅ 已获取认证令牌
- ✅ Authorization header 已正确添加

## 根本原因分析

### 错误链条
```
用户登录 (成功)
    ↓
获取 accessToken (成功)
    ↓
保存到 localStorage['authToken'] (成功)
    ↓
页面加载，调用 loadTenants()
    ↓
getToken() 返回 token (成功)
    ↓
添加 Authorization: Bearer {token} header (成功)
    ↓
后端收到请求
    ↓
TenantFilter 处理:
  - extractTokenFromHeader() (成功)
  - validateToken() (可能失败)
  - getTenantIdFromToken() (成功)
  - getUserIdFromToken() (失败❌ 返回 null)
    ↓
TenantContextHolder.setUserId(null) 
    ↓
后端业务逻辑:
  - Long currentUserId = TenantContextHolder.getUserId(); (null)
  - checkSuperAdminPermission(null) (权限检查失败)
    ↓
返回 400 Bad Request❌
```

### 最可能的原因

**原因1: JWT Token中缺少userId字段** ⭐⭐⭐⭐⭐ (最可能)
- 登录时生成Token没有包含userId
- JwtUtils.generateToken()应该包含userId，但实际没有

**原因2: Token验证失败** ⭐⭐⭐
- Token签名无效
- Token已过期

**原因3: TenantFilter注册失败** ⭐⭐
- Spring Filter未正确注册
- 请求没有经过TenantFilter处理

## 快速诊断步骤

### 步骤1: 检查Token内容

在浏览器console执行：

```javascript
// 1. 查看token是否存在
const token = localStorage.getItem('authToken');
console.log('Token:', token ? token.substring(0, 20) + '...' : '未找到');

// 2. 如果token存在，解析payload
if (token) {
    const parts = token.split('.');
    if (parts.length === 3) {
        try {
            const payload = JSON.parse(atob(parts[1]));
            console.log('Token Payload:', payload);
            
            // 关键字段检查
            console.log('检查结果:');
            console.log('  ✓ userId:', payload.userId ? '存在' : '❌ 缺失');
            console.log('  ✓ tenantId:', payload.tenantId ? '存在' : '❌ 缺失');
            console.log('  ✓ username:', payload.username ? '存在' : '❌ 缺失');
            console.log('  ✓ exp:', new Date(payload.exp * 1000).toLocaleString());
        } catch (e) {
            console.log('❌ 无法解析Token');
        }
    } else {
        console.log('❌ Token格式不正确，不是标准JWT');
    }
}
```

**预期输出 (正常):**
```javascript
Token Payload: {
  userId: 1,           // ✓ 必须存在
  tenantId: 1,         // ✓ 必须存在
  username: "admin",   // ✓ 应该存在
  exp: 1706123456,     // 过期时间戳
  iat: 1706037056      // 发行时间戳
}
```

**症状 (异常):**
```javascript
// 情况1: userId缺失
Token Payload: {
  tenantId: 1,
  username: "admin",
  // ❌ userId 缺失！这就是400错误的原因
}

// 情况2: Token过期
exp: 1206037056  // 时间已过去

// 情况3: Token格式错误
"eyJ0eXAiOiJKV1QifQ"  // 只有2部分，不是有效JWT
```

### 步骤2: 检查localStorage

```javascript
// 查看所有认证相关的localStorage
console.log('authToken:', localStorage.getItem('authToken') ? '存在' : '❌ 不存在');
console.log('token:', localStorage.getItem('token') ? '存在' : '不存在');
console.log('refreshToken:', localStorage.getItem('refreshToken') ? '存在' : '不存在');
console.log('userId:', localStorage.getItem('userId'));
console.log('tenantId:', localStorage.getItem('tenantId'));
console.log('username:', localStorage.getItem('username'));
```

**预期:** authToken 应该存在

### 步骤3: 监听网络请求

打开浏览器F12 → Network标签

刷新页面，查看 `/api/tenant/list` 请求：

**Headers 标签检查:**
```
Request Headers:
  Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...  ✓
  Content-Type: application/json  ✓

Response Headers:
  HTTP/1.1 400 Bad Request  ❌
```

**Response 标签查看:**
```json
{
  "code": 400,
  "message": "请求参数不正确或缺少认证信息",
  "data": null
}
```

## 根据检查结果的解决方案

### 情况A: Token 中 userId 缺失

**症状:** 
- Token payload 中没有 userId 字段
- 或 userId 为 null

**解决方案:**

检查后端登录接口 (AuthController.login()):

```java
// 查看这段代码是否正确
private Map<String, Object> buildClaims(SysTenant tenant, SysUser user, List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    
    // ✓ 检查这些字段是否都被添加
    claims.put("userId", user.getId());       // 必须添加
    claims.put("tenantId", tenant.getId());   // 必须添加
    claims.put("username", user.getUsername()); // 应该添加
    claims.put("roles", roles);               // 可选
    
    return claims;
}
```

如果 userId 缺失，需要在这里添加。

### 情况B: Token 格式不正确

**症状:**
- Token 不是标准 JWT 格式（不是 xxx.xxx.xxx）
- 或包含特殊字符导致编码问题

**解决方案:**

1. 清除localStorage中所有token相关数据
2. 重新登录获取新token
3. 使用浏览器F12再次验证

```javascript
// 清除所有认证数据
localStorage.removeItem('authToken');
localStorage.removeItem('token');
localStorage.removeItem('refreshToken');
localStorage.removeItem('userId');
localStorage.removeItem('tenantId');

// 或全部清除
localStorage.clear();

// 然后重新访问登录页面
window.location.href = '/admin-pc/pages/login.html';
```

### 情况C: Token 已过期

**症状:**
```javascript
exp: 1206037056  // 时间远早于当前时间
// 当前时间戳大于 exp，Token已过期
```

**解决方案:**

需要实现Token自动刷新：

```javascript
// 在 app-init.js 中应该有这样的逻辑
async function refreshTokenIfNeeded() {
    const expireTime = localStorage.getItem('tokenExpireTime');
    const now = Date.now();
    
    // Token 即将过期（5分钟内）
    if (expireTime && (expireTime - now) < 300000) {
        const refreshToken = localStorage.getItem('refreshToken');
        // 调用后端 POST /auth/refresh 获取新token
        // 更新 localStorage['authToken']
    }
}
```

### 情况D: localStorage 中没有 authToken

**症状:**
```javascript
localStorage.getItem('authToken')  // null
localStorage.getItem('token')      // null
```

**原因:**
1. 登录后页面跳转时，token 保存失败
2. localStorage 被清除了
3. 浏览器隐私模式，localStorage 保存不了

**解决方案:**

1. 检查登录页面是否正确保存token：
```javascript
// 在login.html中应该有
window.AuthService.saveAuth(response.data);
// 或
localStorage.setItem('authToken', response.data.accessToken);
```

2. 强制重新登录：
```javascript
window.location.href = '/admin-pc/pages/login.html';
```

3. 如果是隐私模式：
   - 关闭隐私窗口，用普通窗口重试

## 完整诊断Checklist

- [ ] Token 存在于 localStorage['authToken']
- [ ] Token 是标准JWT格式 (xxx.xxx.xxx)
- [ ] Token payload 包含 userId 字段
- [ ] Token payload 包含 tenantId 字段
- [ ] Token 未过期 (exp > 当前时间戳)
- [ ] Authorization header 格式正确 (Bearer {token})
- [ ] 网络请求到达后端 (Network 标签可见)
- [ ] 后端TenantFilter 能解析token (检查后端日志)
- [ ] TenantContextHolder.getUserId() 不为null

## 高级诊断：查看后端日志

在后端启动日志中搜索关键字：

```
// 正常日志
[FILTER] 租户上下文已设置 - TenantID: 1, UserID: 1
[CONTROLLER] 用户ID:1 查看租户列表

// 异常日志
[ERROR] Failed to extract userId from token: ...
[WARN] 租户上下文未设置
[WARN] 用户ID:null 尝试访问超管功能
```

## 仍然无法解决？

### 最后的核查清单

1. **确认数据库有数据:**
```sql
SELECT id, username, password, tenant_id, status FROM sys_user WHERE username='admin' LIMIT 1;
```

应该返回:
```
id: 1
username: admin
password: 123456 (或加密形式)
tenant_id: 1
status: 1 (启用)
```

2. **确认登录接口正常工作:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode":"default",
    "username":"admin",
    "password":"123456"
  }'
```

应该返回:
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "tenantId": 1
    }
  }
}
```

3. **直接测试/api/tenant/list:**
```bash
# 先从登录接口获取token
TOKEN="刚刚得到的accessToken值"

curl -X GET http://localhost:8081/api/tenant/list \
  -H "Authorization: Bearer $TOKEN"
```

应该返回:
```json
{
  "code": 200,
  "data": {
    "total": 4,
    "items": [...租户列表...]
  }
}
```

## 获取帮助

如果按照上述步骤仍无法解决，请收集以下信息：

1. Token payload 内容 (console.log输出)
2. 网络请求的完整request/response headers
3. 后端应用日志中的相关ERROR行
4. 浏览器console中的完整错误信息
5. tenant-management.html 加载时的完整console输出

然后在技术支持时提供这些信息。
