# 报名系统租户ID问题修复 - 快速验证清单

## 问题总结
**症状**: 报名提交成功，但无法在报名列表或查询页面找到报名信息
**根因**: 前端 localStorage 中租户ID键名不统一导致提交和查询时使用了不同的租户ID

## 核心修复

### 修改文件清单

| 文件 | 修改位置 | 修改内容 | 状态 |
|------|----------|----------|------|
| scan-register.vue | 第102行 | `doUpload` 方法中的tenantId优先级改为：`tenant_id` → `tenantId` → `current_tenant_id` → DEFAULT | ✅ |
| scan-register.vue | 第164行 | `submit` 方法中的tenantId优先级改为：`tenant_id` → `tenantId` → `current_tenant_id` → DEFAULT | ✅ |
| registration-status.vue | 第81行 | `queryStatus` 方法中的tenantId优先级改为：`tenant_id` → `tenantId` → `current_tenant_id` → 默认值 | ✅ |
| RegistrationServiceImpl.java | queryByConferenceAndPhone 方法 | 添加详细日志输出查询条件和结果 | ✅ |

## 快速测试步骤

### 1. 前端环境检查 (5分钟)
```javascript
// 在浏览器 F12 Console 中执行

// 检查 localStorage 中的租户ID
console.log('tenant_id:', localStorage.getItem('tenant_id') || 'NOT SET')
console.log('tenantId:', localStorage.getItem('tenantId') || 'NOT SET')
console.log('current_tenant_id:', localStorage.getItem('current_tenant_id') || 'NOT SET')

// 预期输出：
// tenant_id: 2027317834622709762
// tenantId: NOT SET
// current_tenant_id: NOT SET
```

### 2. 前端功能测试 (10分钟)
**测试场景 A - 报名提交**

1. 打开报名页面：`pages/learner/scan-register/scan-register.vue`
2. F12 打开开发者工具 → Console 标签
3. 填写报名表单：
   - 姓名：测试用户001
   - 手机号：13800000001
   - 单位：测试部门
   - 职位：测试职位
4. 点击提交
5. 观察 Console 日志，应该看到：
```
=== 报名提交信息 ===
会议ID: 2030309010523144200
租户ID: 2027317834622709762    ← 关键！应该是这个值
姓名: 测试用户001
手机号: 13800000001

=== 报名提交返回结果 ===
返回的报名ID: (数字ID)
返回的会议ID: 2030309010523144200
返回的租户ID: 2027317834622709762  ← 关键！与提交时一致
```

**测试场景 B - 报名查询**

1. 打开报名状态查询页面：`pages/learner/registration-status/registration-status.vue`
2. F12 打开开发者工具 → Console 标签
3. 输入刚才提交时用的手机号：13800000001
4. 点击"查询报名状态"按钮
5. 应该能成功查到报名信息

### 3. 后端日志检查 (5分钟)
查看后端日志文件，应该看到：

**提交时的日志**:
```
[INFO] === 报名提交开始 ===
[INFO] 会议ID: 2030309010523144200, 租户ID: 2027317834622709762, 用户ID: null
[INFO] 报名人: 测试用户001, 手机号: 13800000001
...
[INFO] === 报名提交成功 ===
[INFO] 最终保存的数据 - ID: (数字), phone: 13800000001, conferenceId: 2030309010523144200, tenantId: 2027317834622709762
```

**查询时的日志**:
```
[INFO] === 查询报名信息开始 ===
[INFO] 查询条件 - 会议ID: 2030309010523144200, 手机号: 13800000001, 租户ID: 2027317834622709762
[INFO] 查询成功 - 找到报名记录，ID: (数字), 报名人: 测试用户001
```

### 4. 数据库验证 (5分钟)
在数据库客户端执行以下SQL：

```sql
-- 查询该租户下的报名记录
SELECT id, meeting_id, phone, real_name, tenant_id, registration_time 
FROM conf_registration 
WHERE tenant_id = '2027317834622709762' 
  AND meeting_id = 2030309010523144200
  AND phone = '13800000001'
ORDER BY registration_time DESC;

-- 应该能看到刚才提交的记录
```

## 成功标志

✅ 前端 Console 中租户ID与提交时一致
✅ 报名查询能成功找到记录
✅ 后端日志显示查询条件正确且找到了记录
✅ 数据库中能查到该条记录，租户ID为 2027317834622709762

## 如果问题仍然存在

### 检查项 1: 后端未启用日志
- 修改后端配置文件，启用 INFO 级别日志
- 重启后端服务
- 重新测试

### 检查项 2: localStorage 中没有正确的租户ID
```javascript
// 在 Console 中手动设置（临时测试）
localStorage.setItem('tenant_id', '2027317834622709762')
```

### 检查项 3: 租户ID的值不同
检查您的系统中默认租户ID是否真的是 `2027317834622709762`

查询方式：
```sql
-- 查看系统中有哪些租户
SELECT id, name FROM conf_tenant LIMIT 10;

-- 查看是否存在这个租户
SELECT * FROM conf_tenant WHERE id = '2027317834622709762';
```

### 检查项 4: 后端从请求头读不到租户ID
确保后端的请求拦截器正确处理了 X-Tenant-Id 请求头：

```java
// 应该在某个拦截器或过滤器中类似这样
String tenantId = request.getHeader("X-Tenant-Id");
TenantContextHolder.setTenantId(Long.parseLong(tenantId));
```

## 回滚步骤（如果需要）

如果修复后出现新的问题，可以按以下步骤回滚：

### 回滚代码变更
1. 恢复 scan-register.vue 第 102 行和 164 行（优先级改回原值）
2. 恢复 registration-status.vue 第 81 行（优先级改回原值）
3. 恢复 RegistrationServiceImpl.java queryByConferenceAndPhone 方法（移除日志）
4. 重新编译部署

### 回滚指令
```bash
# 如果使用git
git checkout -- app-uniapp/pages/learner/scan-register/scan-register.vue
git checkout -- app-uniapp/pages/learner/registration-status/registration-status.vue
git checkout -- backend/.../RegistrationServiceImpl.java

# 重新编译
npm run build  # 前端
mvn clean package  # 后端
```

## 修复原理总结

### 修复前的问题流程
```
登录: setStorageSync('tenant_id', '2027317834622709762')
       ↓
报名: getStorageSync('tenantId')  // 读不到!
        || getStorageSync('current_tenant_id')  // 也读不到!
        || DEFAULT_TENANT_ID  // 可能不是正确的值
       ↓ 使用了错误的租户ID提交
后端保存: tenantId = 错误的值
       ↓
查询: getStorageSync('tenant_id')  // 读到正确值
       ↓ 使用正确的租户ID查询
后端查询: WHERE tenant_id = 正确值 AND ...  // 查不到!
                // 因为保存时用的是错误的值
```

### 修复后的正确流程
```
登录: setStorageSync('tenant_id', '2027317834622709762')
       ↓
报名: getStorageSync('tenant_id')  // 读到正确值!
       ↓ 使用正确的租户ID提交
后端保存: tenantId = 2027317834622709762
       ↓
查询: getStorageSync('tenant_id')  // 读到正确值
       ↓ 使用相同的租户ID查询
后端查询: WHERE tenant_id = 2027317834622709762 AND ...  // 查到!
```

## 相关 Issue 链接

- **前端报名页面**: [scan-register.vue](../../app-uniapp/pages/learner/scan-register/scan-register.vue)
- **查询状态页面**: [registration-status.vue](../../app-uniapp/pages/learner/registration-status/registration-status.vue)
- **登录页面**: [login.vue](../../app-uniapp/pages/common/login/login.vue)
- **后端服务**: [RegistrationServiceImpl.java](../../backend/conference-backend/conference-registration/src/main/java/com/conference/registration/service/impl/RegistrationServiceImpl.java)
- **修复方案文档**: [报名租户ID不一致问题修复方案.md](../报名租户ID不一致问题修复方案.md)

## 常见问题解答

### Q: 为什么前后两个页面使用不同的租户ID键名？
A: 这是系统演进过程中的遗留问题。早期开发使用了不同的键名约定，后来没有统一修改。修复后统一优先使用 `tenant_id`。

### Q: 如果用户手动改过 localStorage，会怎样？
A: 
- 如果改成了错误的租户ID，报名会保存到错误的租户下
- 解决方案是清除 localStorage 后重新登录，登录页面会设置正确的 tenant_id

### Q: 生产环境中的真实租户ID是什么？
A: 需要从您的数据库中查询：
```sql
SELECT id FROM conf_tenant WHERE 1=1 LIMIT 10;
```
确认正确的租户ID值，然后在本地 localStorage 中设置相同的值进行测试。

### Q: 后端从哪里获取租户ID？
A: 从请求头 `X-Tenant-Id` 中获取，前端发送该请求头时需要带上正确的租户ID。

---

**修复时间**: 2024-10-XX
**修复人**: 系统开发团队
**状态**: 已实施 ✅
