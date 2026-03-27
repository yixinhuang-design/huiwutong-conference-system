# 管理后台登录页 - 功能测试说明

**创建日期**: 2026-02-26  
**页面位置**: `/admin-pc/conference-admin-pc/pages/login.html`  
**API服务**: `http://localhost:8081/auth/login`

---

## ✅ 已完成功能

### 1. 登录页设计
- ✅ 现代化玻璃态(Glassmorphism)设计风格
- ✅ 响应式布局（桌面/平板/手机适配）
- ✅ 动画效果和过渡动画
- ✅ 国产浏览器兼容性支持

### 2. 两种登录方式
- **系统管理员登录**: 直接输入用户名和密码
- **租户管理员登录**: 选择租户后输入用户名和密码

### 3. 用户交互
- ✅ 密码显示/隐藏切换
- ✅ Enter键快速登录
- ✅ 记住用户名/租户选择
- ✅ 租户选择后显示租户信息
- ✅ 登录加载状态显示
- ✅ 成功/失败提示信息

### 4. 后端API集成
- ✅ 连接到真实的认证服务 (http://localhost:8081)
- ✅ 标准JWT Token处理
- ✅ 自动Token过期检查
- ✅ 刷新Token支持
- ✅ 错误处理和网络故障检测

### 5. 本地存储管理
- ✅ 认证Token持久化
- ✅ 用户信息缓存
- ✅ 租户信息记忆
- ✅ Token过期时间跟踪

---

## 🧪 测试步骤

### 前置条件
1. 确保认证服务已启动: `http://localhost:8081`
2. 数据库中存在用户:
   - 用户名: `admin`
   - 密码: `123456`
   - 租户: `default`

### 测试用例1: 系统管理员登录
```
1. 打开登录页: file:///admin-pc/conference-admin-pc/pages/login.html
2. 选择"系统管理员"标签
3. 输入用户名: admin
4. 输入密码: 123456
5. 点击"登录"按钮
6. 预期: 显示"登录成功！"，2秒后跳转到首页
```

### 测试用例2: 租户管理员登录
```
1. 打开登录页: file:///admin-pc/conference-admin-pc/pages/login.html
2. 选择"租户管理员"标签
3. 从下拉列表选择"默认租户 (default)"
4. 输入用户名: admin
5. 输入密码: 123456
6. 点击"登录"按钮
7. 预期: 显示"登录成功！"，2秒后跳转到首页
```

### 测试用例3: 错误密码
```
1. 选择登录方式
2. 输入用户名: admin
3. 输入密码: 错误密码
4. 点击"登录"按钮
5. 预期: 显示错误提示 "账号或密码错误"
```

### 测试用例4: 记住用户名/租户
```
1. 勾选"记住我" 或 "记住租户选择"
2. 完成登录
3. 再次打开登录页
4. 预期: 用户名/租户会自动填充
```

### 测试用例5: 密码显示/隐藏
```
1. 在密码输入框中输入内容
2. 点击密码框右侧的眼睛图标
3. 预期: 密码从"●●●●"变为明文，再点一次变回"●●●●"
```

### 测试用例6: Enter键快速登录
```
1. 填写所有必要信息
2. 在密码框中按 Enter 键
3. 预期: 直接登录，无需点击按钮
```

### 测试用例7: 网络错误处理
```
1. 关闭认证服务 (停止 http://localhost:8081)
2. 尝试登录
3. 预期: 显示 "无法连接到服务器，请检查服务是否启动"
```

---

## 📊 API响应格式

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "tenantId": 1,
      "username": "admin",
      "realName": "管理员",
      "userType": "admin",
      "tenantName": "默认租户",
      "roles": []
    }
  },
  "success": true
}
```

### 失败响应 (401)
```json
{
  "code": 2001,
  "message": "登录失败，用户名或密码错误",
  "data": null,
  "success": false
}
```

---

## 💾 本地存储数据

登录成功后，以下数据会保存到 localStorage:

| Key | 说明 | 示例 |
|-----|------|------|
| `authToken` | 访问令牌 | eyJhbGciOiJIUzM4NCJ9... |
| `refreshToken` | 刷新令牌 | eyJhbGciOiJIUzM4NCJ9... |
| `tokenType` | Token类型 | Bearer |
| `expiresIn` | 过期时间(秒) | 86400 |
| `tokenExpireTime` | 过期时间戳(ms) | 1772187168000 |
| `userId` | 用户ID | 1 |
| `username` | 用户名 | admin |
| `realName` | 真实姓名 | 管理员 |
| `userType` | 用户类型 | admin/staff |
| `tenantId` | 租户ID | 1 |
| `tenantName` | 租户名称 | 默认租户 |
| `tenantCode` | 租户编码 | default |

---

## 🔧 API服务配置

### 文件位置
`/admin-pc/conference-admin-pc/js/auth-service.js`

### 主要方法
```javascript
// 登录
AuthService.login(username, password, tenantCode)

// 刷新Token
AuthService.refreshToken(refreshToken)

// 获取当前用户
AuthService.getCurrentUser()

// 登出
AuthService.logout()

// 检查是否已登录
AuthService.isAuthenticated()

// 检查Token是否过期
AuthService.isTokenExpired()
```

---

## 🐛 常见问题

### Q: 点击登录后没有反应
A: 检查以下几点:
1. 认证服务是否启动在 http://localhost:8081
2. 浏览器控制台是否有错误信息
3. 网络连接是否正常

### Q: 显示"无法连接到服务器"
A: 需要启动认证服务:
```bash
cd "G:\huiwutong新版合集\backend\conference-backend"
mvn -s maven-settings.xml -f conference-auth\pom.xml spring-boot:run
```

### Q: Token过期后如何处理
A: 系统会在登录页自动检查Token有效期，如果过期会清除并要求重新登录。主页面应该实现Token自动刷新机制。

### Q: 如何修改API服务地址
A: 编辑 `/admin-pc/conference-admin-pc/js/auth-service.js` 第5行:
```javascript
this.baseURL = 'http://localhost:8081'; // 修改这里
```

---

## 📝 浏览器兼容性

| 浏览器 | 版本 | 兼容性 |
|--------|------|--------|
| Chrome | 90+ | ✅ 完全支持 |
| Firefox | 88+ | ✅ 完全支持 |
| Safari | 14+ | ✅ 完全支持 |
| Edge | 90+ | ✅ 完全支持 |
| 360浏览器 | 13+ | ✅ 完全支持 |
| QQ浏览器 | 10+ | ✅ 完全支持 |
| 搜狗浏览器 | 12+ | ✅ 完全支持 |

---

## 🚀 后续改进计划

1. 添加"忘记密码"功能
2. 支持短信/邮箱验证
3. 集成单点登录(SSO)
4. 添加二次验证(2FA)
5. 登录失败次数限制和账户锁定
6. 支持LDAP/ActiveDirectory集成

---

**页面已就绪，可以进行测试！** 🎉
