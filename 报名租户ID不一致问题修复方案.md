# 报名系统租户ID不一致问题修复方案

## 问题描述
用户提交报名成功，但无法在数据库中查看报名信息。错误提示："报名提交成功后，看不到报名信息啊，会议id我在数据库查询都没有报名页面显示的id"。

## 根本原因分析

### 问题来源：localStorage租户ID键值不统一

#### 1. 登录时设置的租户ID
**文件**: `app-uniapp/pages/common/login/login.vue`
**代码** (第 147, 216 行):
```javascript
uni.setStorageSync('tenant_id', '2027317834622709762')  // 使用键：tenant_id
```

#### 2. 报名提交时读取的租户ID（**错误**）
**文件**: `app-uniapp/pages/learner/scan-register/scan-register.vue`
**代码** (第 102, 164 行，修复前):
```javascript
// 优先级：tenantId → current_tenant_id → DEFAULT_TENANT_ID
const tenantId = uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
```
**问题**：
- 优先从 `tenantId` 读取（实际不存在）
- 再从 `current_tenant_id` 读取（也不存在）
- 最后使用 `DEFAULT_TENANT_ID = '2027317834622709762'`
- 但登录时设置的是 `tenant_id`（带下划线），导致读取不到登录时设置的值

#### 3. 查询时读取的租户ID（**部分正确**）
**文件**: `app-uniapp/pages/learner/registration-status/registration-status.vue`
**代码** (第 81 行，修复前):
```javascript
// 优先级：tenant_id → current_tenant_id → 默认值
const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || '2027317834622709762'
```
**问题**：
- 正确地从 `tenant_id` 读取（与登录时一致）
- 但这与报名时的读取逻辑不一致

### 数据流过程中的断裂点

```
登录页面
↓
uni.setStorageSync('tenant_id', '2027317834622709762')  // 存储为 tenant_id

报名提交
↓
try {
  const tenantId = uni.getStorageSync('tenantId')  // 读不到！
    || uni.getStorageSync('current_tenant_id')    // 也读不到！
    || API_CONFIG.DEFAULT_TENANT_ID;               // 最终使用默认值
  
  // 提交时的租户ID可能是 DEFAULT_TENANT_ID（不一定是 2027317834622709762）
}

后端保存
↓
registration.setTenantId(currentTenantId());  // 保存时使用从X-Tenant-Id请求头读取的租户ID
// 实际保存的租户ID = 从X-Tenant-Id头读取的值 = 前端发送的值

报名查询
↓
const tenantId = uni.getStorageSync('tenant_id')  // 正确读到 2027317834622709762
  || uni.getStorageSync('current_tenant_id')
  || '2027317834622709762'

queryByConferenceAndPhone() 
↓
// 查询条件：conferenceId + phone + tenantId(从currentTenantId()读取)
// 如果后端的currentTenantId()返回的值与前端提交时发送的tenantId不同
// 就会查询不到数据！
```

## 修复方案

### 修复 1: scan-register.vue 租户ID获取顺序
**文件**: `app-uniapp/pages/learner/scan-register/scan-register.vue`

#### 修改1.1 - doUpload 方法 (第 102 行)
**修改前**:
```javascript
const tenantId = uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
```

**修改后**:
```javascript
const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
```

#### 修改1.2 - submit 方法 (第 164 行)
**修改前**:
```javascript
const tenantId = uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
```

**修改后**:
```javascript
const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
```

### 修复 2: registration-status.vue 租户ID获取顺序统一
**文件**: `app-uniapp/pages/learner/registration-status/registration-status.vue`

#### 修改 - queryStatus 方法 (第 81 行)
**修改前**:
```javascript
const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || '2027317834622709762'
```

**修改后**:
```javascript
const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || '2027317834622709762'
```

### 修复 3: 后端添加租户ID校验日志
**文件**: `RegistrationServiceImpl.java`

#### 修改 - queryByConferenceAndPhone 方法
**添加详细日志**:
```java
public Registration queryByConferenceAndPhone(Long conferenceId, String phone) {
    Long tenantId = currentTenantId();
    
    logger.info("=== 查询报名信息开始 ===");
    logger.info("查询条件 - 会议ID: {}, 手机号: {}, 租户ID: {}", conferenceId, phone, tenantId);

    Registration result = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
            .eq(Registration::getConferenceId, conferenceId)
            .eq(Registration::getPhone, phone)
            .eq(Registration::getTenantId, tenantId)
            .eq(Registration::getDeleted, 0)
            .orderByDesc(Registration::getRegistrationTime)
            .last("LIMIT 1"));
    
    if (result != null) {
        logger.info("查询成功 - 找到报名记录，ID: {}, 报名人: {}", result.getId(), result.getRealName());
    } else {
        logger.warn("查询无结果 - 未找到对应报名记录，会议ID: {}, 手机号: {}, 租户ID: {}", conferenceId, phone, tenantId);
    }
    
    return result;
}
```

## 修复效果验证

### 前端日志验证
修改后的前端代码包含以下日志输出：

**报名提交时** (scan-register.vue 第 179-230 行):
```
=== 报名提交信息 ===
会议ID: 2030309010523144200
租户ID: 2027317834622709762  (现在能正确从 tenant_id 读取)
姓名: 张三
手机号: 13800138001

=== 报名提交返回结果 ===
返回的报名ID: 123456
返回的会议ID: 2030309010523144200
返回的租户ID: 2027317834622709762
```

**报名查询时** (registration-status.vue):
```
console 输出：
- 会议ID: 2030309010523144200
- 租户ID: 2027317834622709762  (与提交时一致)
- 手机号: 13800138001
```

### 后端日志验证
启用后端日志后，将输出：

**提交时**:
```
[INFO] === 报名提交开始 ===
[INFO] 会议ID: 2030309010523144200, 租户ID: 2027317834622709762, 用户ID: null
[INFO] 报名人: 张三, 手机号: 13800138001
...
[INFO] === 报名提交成功 ===
[INFO] 最终保存的数据 - ID: 123456, phone: 13800138001, conferenceId: 2030309010523144200, tenantId: 2027317834622709762
```

**查询时**:
```
[INFO] === 查询报名信息开始 ===
[INFO] 查询条件 - 会议ID: 2030309010523144200, 手机号: 13800138001, 租户ID: 2027317834622709762
[INFO] 查询成功 - 找到报名记录，ID: 123456, 报名人: 张三
```

## 数据库验证 SQL

```sql
-- 1. 查看该租户下的所有报名
SELECT id, meeting_id, phone, real_name, tenant_id, registration_time 
FROM conf_registration 
WHERE tenant_id = '2027317834622709762' 
  AND meeting_id = 2030309010523144200 
ORDER BY registration_time DESC;

-- 2. 验证数据一致性
SELECT DISTINCT tenant_id 
FROM conf_registration 
WHERE meeting_id = 2030309010523144200;

-- 3. 查看特定手机号的所有报名记录
SELECT id, meeting_id, phone, real_name, tenant_id, registration_time 
FROM conf_registration 
WHERE phone = '13800138001' 
ORDER BY registration_time DESC;
```

## 实施步骤

### 1. 代码修改（已完成）
- ✅ scan-register.vue 两处修改：doUpload 和 submit 方法
- ✅ registration-status.vue 一处修改：queryStatus 方法
- ✅ RegistrationServiceImpl.java 添加查询日志

### 2. 编译和部署
```bash
# 前端
npm run build

# 后端
mvn clean package
# 重启应用
```

### 3. 测试流程
1. **登录系统** → 观察 localStorage 中的 tenant_id 值
2. **打开报名页面** → F12 查看 console 日志，验证租户ID是否正确
3. **提交报名** → 查看 console 中的提交日志，确认租户ID与登录时一致
4. **后台查询** → 检查后端日志，验证查询时的租户ID与提交时一致
5. **数据库查询** → 执行上述SQL查询，确认数据确实存在
6. **查询报名状态** → 使用 registration-status.vue 查询，应该能成功查到

## 预期结果

修复后，报名系统的租户ID流程将完全一致：

```
登录系统
  ↓
localStorage['tenant_id'] = '2027317834622709762'
  ↓
报名提交
  ↓
读取 localStorage['tenant_id'] = '2027317834622709762'
发送到后端，X-Tenant-Id: 2027317834622709762
  ↓
后端保存
  ↓
conf_registration.tenant_id = 2027317834622709762
  ↓
报名查询
  ↓
读取 localStorage['tenant_id'] = '2027317834622709762'
后端查询：WHERE tenant_id = 2027317834622709762 AND meeting_id = 2030309010523144200
  ↓
成功找到报名记录！
```

## 注意事项

1. **多个localStorage键值统一**: 系统中应该统一使用 `tenant_id` 作为租户ID的存储键
2. **后端租户获取**: 确保后端的 `currentTenantId()` 方法能正确从 X-Tenant-Id 请求头读取租户ID
3. **日志级别**: 生产环境应该将详细日志设置为 DEBUG 级别，避免日志过多
4. **容错处理**: 即使租户ID未设置，系统也应该有合理的默认值和错误提示

## 相关文件清单

### 前端文件
- [scan-register.vue](app-uniapp/pages/learner/scan-register/scan-register.vue) - 第 102, 164 行
- [registration-status.vue](app-uniapp/pages/learner/registration-status/registration-status.vue) - 第 81 行
- [login.vue](app-uniapp/pages/common/login/login.vue) - 第 147, 216 行

### 后端文件
- [RegistrationServiceImpl.java](backend/conference-backend/conference-registration/src/main/java/com/conference/registration/service/impl/RegistrationServiceImpl.java) - queryByConferenceAndPhone 方法

### 数据库
- 表: `conf_registration`
- 关键字段: `tenant_id`, `meeting_id`, `phone`
- 关键索引: `idx_tenant_meeting` (tenant_id, meeting_id)

## 总结

这个问题的根本原因是前端对 localStorage 租户ID键值的不统一使用：
- 登录时保存为 `tenant_id`（带下划线）
- 报名时读取 `tenantId`（驼峰命名）
- 导致报名提交时使用了错误的租户ID或默认值
- 最终导致数据保存和查询使用不同的租户ID过滤条件

修复方案统一了所有前端页面和后端服务的租户ID获取逻辑，确保数据流从登录到提交到查询的整个过程中租户ID保持一致。
