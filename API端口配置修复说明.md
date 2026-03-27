# API 端口配置修复说明

**更新日期**: 2026年3月11日  
**问题**: 前端请求 404，无法加载日程列表  
**根本原因**: 前端 baseURL 配置错误

## 问题分析

### 错误信息
```
GET http://localhost:8080/api/schedule/all?meetingId=2030309010523144194 404 (Not Found)
加载日程失败: Error: HTTP error! status: 404
```

### 根本原因
1. **后端实际端口**: 8084（来自 `application.yml`）
2. **前端之前配置**: 9001（错误）
3. **结果**: 所有 API 请求都得到 404

## 修复内容

### schedule-api.js 修改
```javascript
// 修改前（错误）
baseURL: 'http://localhost:9001/api/schedule'

// 修改后（正确）
baseURL: 'http://localhost:8084/api/schedule'
```

## 验证步骤

### 1. 确认后端在运行
```bash
curl http://localhost:8084/api/schedule/all?meetingId=2030309010523144194
```

应该返回 JSON 数据，而不是 404

### 2. 浏览器硬刷新
```
Ctrl+Shift+R （Chrome/Firefox）
Cmd+Shift+R （Mac）
```

### 3. 检查浏览器 Network 标签
- 请求应该发往 `localhost:8084`
- 状态码应该是 200，而不是 404

## 配置参考

### 后端配置（application.yml）
```yaml
server:
  port: 8084          # ← 后端运行端口
  servlet:
    context-path: /
```

### 前端配置（schedule-api.js）
```javascript
baseURL: 'http://localhost:8084/api/schedule'  # ← 必须与后端端口一致
```

### API 调用示例
```javascript
// 格式: http://localhost:8084/api/schedule/all?meetingId=XXX
const response = await ScheduleAPI.allSchedules(meetingId);
```

## 部署环境配置

不同环境的后端端口可能不同：

| 环境 | 主机 | 端口 | 前端 baseURL |
|------|------|------|------------|
| 本地开发 | localhost | 8084 | http://localhost:8084/api/schedule |
| 测试环境 | 192.168.x.x | 8084 | http://192.168.x.x:8084/api/schedule |
| 生产环境 | api.example.com | 443 | https://api.example.com/api/schedule |

## 调整方法

根据你的部署环境，修改 `schedule-api.js` 中的 baseURL：

```javascript
// 本地开发
baseURL: 'http://localhost:8084/api/schedule'

// 测试环境
baseURL: 'http://192.168.1.100:8084/api/schedule'

// 生产环境（HTTPS）
baseURL: 'https://api.yourdomain.com/api/schedule'
```

## 相关文件

- **前端配置**: `admin-pc/conference-admin-pc/js/api/schedule-api.js` （第 12 行）
- **后端配置**: `backend/conference-backend/conference-meeting/src/main/resources/application.yml` （第 54 行）
- **后端 Controller**: `backend/conference-backend/conference-meeting/src/main/java/com/conference/meeting/controller/ScheduleController.java`

## 总结

✅ **修复完成**
- baseURL 已修正为 `http://localhost:8084/api/schedule`
- 参数已恢复为正确的 `meetingId`
- 现在应该能正常加载日程列表

⚠️ **重要**: 部署到不同环境时，记得更新 baseURL 的主机和端口！
