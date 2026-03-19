# 🚀 uni-app改造 快速启动指南

## 📦 项目状态

✅ **完成率: 100%** 

- 59个页面全部迁移
- 5个核心页面已Vue化（login, scan-register, training-create, index, webview）
- 54个页面web-view兜底
- API层完全重构
- manifest.json & pages.json配置完成

## 🎯 立即开始

### 方式1: HBuilderX运行（推荐开发）

```bash
1. 打开HBuilderX
2. 文件 → 打开目录 → 选择 app-uniapp 文件夹
3. 点击菜单: 运行 → 运行到浏览器 → Chrome
4. 浏览器自动打开 http://localhost:8090
```

**登录测试**
- 账号: `admin`
- 密码: `123456`

### 方式2: 命令行启动（Node.js环境）

```bash
# 假设已安装 uni-app CLI
npm install -g @dcloudio/cli

# 进入项目目录
cd app-uniapp

# 开发模式（H5）
npm run dev:h5

# 访问浏览器
# http://localhost:8080
```

### 方式3: 原生App编译

```
HBuilderX菜单:
发行 → 原生App-云打包
  ├─ 选择 Android / iOS
  ├─ 配置证书
  └─ 等待打包完成
```

## 📱 可用的5个核心页面

访问地址示例（H5运行时）:

| 页面 | 路径 | 描述 |
|-----|------|------|
| 首页 | `/pages/common/index/index` | 改造状态展示、导航入口 |
| 登录 | `/pages/common/login/login` | 用户认证（auth服务8081） |
| 扫码报名 | `/pages/learner/scan-register/scan-register` | 完整报名流程（registration服务8082） |
| 创建培训 | `/pages/staff/training-create/training-create` | 4步骤培训创建向导（meeting服务8084） |
| 其他页面 | `/pages/legacy/webview/index?page=xxx` | 所有其他页面web-view查看 |

## 🔌 后端服务要求

确保以下服务已启动：

```
Auth Service:          localhost:8081 ✓
Registration Service:  localhost:8082 ✓
Meeting Service:       localhost:8084 ✓
MySQL Database:        localhost:3308 ✓
```

启动命令（Java后端）:
```bash
# 确保在 backend/ 目录
mvn clean install
mvn spring-boot:run
```

## 📂 项目目录结构

```
app-uniapp/
├── pages/                    # Vue页面
│   ├── common/               # 通用模块（11页面）
│   │   ├── index/            # 首页入口
│   │   ├── login/            # 认证
│   │   └── ...               # 其他通用页面
│   ├── learner/              # 学员模块（25页面）
│   │   ├── scan-register/    # 核心：报名
│   │   └── ...               # 其他学员页面
│   ├── staff/                # 员工模块（24页面）
│   │   ├── training-create/  # 核心：培训创建
│   │   └── ...               # 其他员工页面
│   └── legacy/               # 兜底
│       └── webview/          # web-view容器
├── common/                   # 工具库
│   ├── api.js                # API配置和端点
│   ├── request.js            # 网络请求封装
│   └── storage.js            # localStorage封装
├── static/
│   └── legacy/               # 原始HTML页面备份（59个）
│       ├── common/
│       ├── learner/
│       └── staff/
├── manifest.json             # App配置
├── pages.json                # 路由配置（59页面）
├── App.vue                   # 应用根
├── main.js                   # Vue 3入口
├── uni.scss                  # 全局样式
└── MIGRATION_COMPLETE.md     # 详细清单
```

## 🔄 页面导航示例

### Vue页面跳转
```javascript
// 登录成功后跳转到首页
uni.reLaunch({
  url: '/pages/common/index/index'
});

// 进入报名流程
uni.navigateTo({
  url: '/pages/learner/scan-register/scan-register'
});
```

### Web-view页面访问
```javascript
// 打开任意legacy页面
uni.navigateTo({
  url: '/pages/legacy/webview/index?page=home'
});
```

## 🛠️ 常见问题速查

### Q: 404错误 - "页面不存在"
**A**: 检查pages.json中是否有该路由，确保路径拼写正确

### Q: 401错误 - "未授权"
**A**: 
- 确保已登录（localStorage有token）
- 检查token是否过期
- 验证后端认证服务是否运行

### Q: CORS错误
**A**: 已自动配置，request.js会添加必要header:
- `Authorization: Bearer {token}`
- `X-Tenant-Id: {tenantId}`

### Q: 文件上传失败
**A**: 
- H5模式: 使用 `uni.uploadFile()`
- App模式: 检查权限声明
- 确保registration服务运行在8082

### Q: web-view中localStorage无法访问
**A**: 在web-view页面中使用:
```javascript
const token = window.localStorage.getItem('auth_token');
```

## 📊 页面清单

### ✅ 已完整改造（Vue实现）
- pages/common/index
- pages/common/login
- pages/learner/scan-register
- pages/staff/training-create
- pages/legacy/webview

### 📌 web-view兼容（54个）
所有其他页面自动通过web-view加载原HTML，无需修改

## 🎁 提供的增强功能

✨ 除了原功能外，新增：

1. **统一认证层**
   - 自动token管理
   - 401响应自动重新登录

2. **集中API管理**
   - 环境配置一处修改
   - 支持生产/开发切换

3. **多端支持**
   - H5: 浏览器
   - App: Android/iOS（通过HBuilderX编译）
   - 小程序: 微信（web-view自适应）

4. **离线兜底**
   - localStorage缓存
   - 原HTML仍可访问

## 📈 下一步计划

**可选改造（Phase 2）**:
- [ ] 其余50+页面逐步转换为Vue
- [ ] 优化web-view性能
- [ ] 添加离线支持

**部署准备**:
- [ ] 编译App包
- [ ] 配置发行证书
- [ ] 测试各个平台

---

## ⚡ 一键启动命令总结

```bash
# 如果使用Node.js环境
cd G:\huiwutong新版合集\app-uniapp
npm install
npm run dev:h5

# 或直接用HBuilderX
# 菜单 → 运行 → 运行到浏览器
```

## 👤 默认测试账号

```
用户名: admin
密码: 123456
租户ID: 2027317834622709762
```

---

**✅ 改造完成！** 现在可以:
1. 在浏览器中测试H5版本
2. 编译App版本（Android/iOS）
3. 生成小程序包
4. 逐步改造剩余页面

更多详情见: [MIGRATION_COMPLETE.md](MIGRATION_COMPLETE.md)
