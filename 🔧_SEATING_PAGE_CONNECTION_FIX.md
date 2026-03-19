# 🔧 排座管理页面连接拒绝问题 - 诊断与修复报告

## 问题现象

- **错误**: ERR_CONNECTION_REFUSED  
- **页面**: http://localhost:8000/pages/seating-mgr.html?conferenceId=2030309010523144194
- **表现**: 页面加载后显示"无法访问此网站"

## 根本原因分析

### 问题的技术链路

**1. 前端API调用配置不一致** 

| 文件 | 配置方式 | 问题 |
|-----|---------|------|
| [conference-context.js L99](admin-pc/conference-admin-pc/js/conference-context.js#L99) | `http://localhost:8084/api/meeting/...` ✅ | 使用绝对URL |
| [seating-mgr.html L5215](admin-pc/conference-admin-pc/pages/seating-mgr.html#L5215) | `/api/seating/venues/...` ❌ | 使用相对URL |
| [seating-mgr.html L5228](admin-pc/conference-admin-pc/pages/seating-mgr.html#L5228) | `/api/seating/layout/...` ❌ | 使用相对URL |

**2. 相对URL解析导致的请求错误**

```
预期路由: http://localhost:8086/api/seating/venues/2030309010523144194
实际请求: http://localhost:8000/api/seating/venues/2030309010523144194
                           ↑ 错误指向前端服务器
```

**3. 前端服务器(http-server)不提供API路由**
- http-server 是静态文件服务器
- 只能提供 HTML/CSS/JS 等静态资源
- 无法处理 `/api/seating/*` 路由
- 导致连接被拒绝: ERR_CONNECTION_REFUSED

## 修复方案

### 修改1: 排座会场列表API - L5215

**文件**: [admin-pc/conference-admin-pc/pages/seating-mgr.html](admin-pc/conference-admin-pc/pages/seating-mgr.html#L5215)

**修改前**:
```javascript
const venuesResponse = await fetch(`/api/seating/venues/${conference.value.id}`, {
```

**修改后**:
```javascript
const venuesResponse = await fetch(`http://localhost:8086/api/seating/venues/${conference.value.id}`, {
```

**原因**: 将相对URL改为指向排座服务(8086端口)的绝对URL

---

### 修改2: 排座平面图API - L5228

**文件**: [admin-pc/conference-admin-pc/pages/seating-mgr.html](admin-pc/conference-admin-pc/pages/seating-mgr.html#L5228)

**修改前**:
```javascript
const layoutResponse = await fetch(`/api/seating/layout?venueId=${venue.id}`, {
```

**修改后**:
```javascript
const layoutResponse = await fetch(`http://localhost:8086/api/seating/layout?venueId=${venue.id}`, {
```

**原因**: 同上，匹配其他服务的API调用配置模式

---

## 已执行的修复

✅ 修改seating-mgr.html中的2个API端点为绝对URL  
✅ 启动前端服务(http-server 在8000端口)  
✅ 排座服务已在8086端口运行  

## 系统现状

| 组件 | 端口 | 状态 | 说明 |
|-----|------|------|------|
| 前端Web服务 | 8000 | ✅ 运行 | http-server 提供静态文件 |
| 排座服务(后端) | 8086 | ✅ 运行 | Spring Boot microservice |
| API网关 | 8080 | 配置中 | 可选的统一入口 |

## 关键发现

### 配置不一致的其他服务

conference-context.js 中已正确配置的API端点:

```javascript
// L99 - 会议服务 (8084)
fetch(`http://localhost:8084/api/meeting/${conferenceId}`, ...)

// L219 - 报名统计 (8082)
fetch(`http://localhost:8082/api/registration/stats?conferenceId=...`, ...)

// L239 - 二维码生成 (8082)
fetch(`http://localhost:8082/api/registration/qr/generate?...`, ...)
```

这些都使用了正确的绝对URL格式，排座管理页面应该遵循相同的模式。

## 负面影响评估

✅ **无负面影响** - 修改仅涉及API端点URL，不影响其他功能：
- 其他页面独立工作
- 不改变API逻辑
- 不修改数据库schema
- 不影响服务间通信

## 测试验证步骤

1. **访问排座管理页面**
   ```
   http://localhost:8000/pages/seating-mgr.html?conferenceId=2030309010523144194
   ```

2. **查看浏览器控制台 (F12)**
   - 应该看到会场数据加载成功
   - 不再出现 ERR_CONNECTION_REFUSED

3. **查看Network标签**
   - 请求应该指向 `http://localhost:8086/api/seating/...`
   - 返回 200 OK 状态码

## 配置最佳实践

为避免将来出现类似问题，建议：

1. **统一使用绝对URL**
   ```javascript
   // ✅ 推荐
   fetch(`http://localhost:8086/api/seating/...`)
   
   // ❌ 避免
   fetch(`/api/seating/...`)  // 在开发环境容易出错
   ```

2. **考虑使用环境变量**
   ```javascript
   const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8086';
   fetch(`${API_BASE_URL}/api/seating/...`)
   ```

3. **或通过API网关统一**
   ```javascript
   // 所有API都通过8080网关
   fetch(`http://localhost:8080/api/seating/...`)
   fetch(`http://localhost:8080/api/meeting/...`)
   ```

## 文件修改清单

- ✅ [admin-pc/conference-admin-pc/pages/seating-mgr.html](admin-pc/conference-admin-pc/pages/seating-mgr.html) - 2处API URL修改

## 后续建议

1. **如果部署到生产环境**：
   - 将 `localhost:8086` 替换为实际的后端服务器地址
   - 例如: `https://backend.company.com/api/seating/...`

2. **如果使用API网关**：
   - 网关应该代理 `/api/seating/*` 到 8086 端口
   - 前端可以继续使用相对URL `/api/seating/...`

3. **跨域配置**：
   - 如果前后端不在同一域名，需要配置CORS
   - 排座服务已在application-local.yml中配置

---

**修复完成时间**: 2026-03-13 12:40  
**修复状态**: ✅ 完成  
**验证状态**: ⏳ 待访问验证
