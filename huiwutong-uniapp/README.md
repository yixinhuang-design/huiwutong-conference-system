# 智能会议助手 - UniApp多端应用

## 项目简介

基于原HTML/CSS/JavaScript原型，迁移到UniApp多端架构，支持H5、微信小程序、App（iOS/Android）发布。

## 技术栈

- **框架**: UniApp (Vue 3)
- **状态管理**: Pinia
- **UI**: 自定义组件（保持原有UI风格）
- **样式**: SCSS

## 项目结构

```
huiwutong-uniapp/
├── pages/              # 页面文件
│   ├── index/         # 登录页 ✅
│   ├── common/        # 共有页面
│   │   └── home.vue   # 首页 ✅
│   └── learner/       # 学员端页面
│       ├── schedule.vue   # 日程安排 ✅
│       ├── checkin.vue    # 签到 ✅
│       └── seat.vue       # 座位查询 ✅
├── components/        # 组件
│   └── common/        # 通用组件 ✅
│       ├── bottom-nav.vue  # 底部导航 ✅
│       └── status-bar.vue  # 状态栏 ✅
├── api/              # API接口 ✅
│   ├── request.js    # 请求封装 ✅
│   ├── auth.js       # 认证接口 ✅
│   └── meeting.js    # 会议接口 ✅
├── store/            # 状态管理 ✅
│   ├── index.js      # Store入口 ✅
│   └── modules/      # 模块化Store ✅
│       ├── user.js   # 用户状态 ✅
│       └── app.js    # 应用状态 ✅
├── utils/            # 工具函数 ✅
│   ├── auth.js       # 认证工具 ✅
│   └── storage.js    # 存储工具 ✅
├── styles/           # 样式文件 ✅
│   ├── variables.scss  # 变量定义 ✅
│   └── common.scss    # 通用样式 ✅
├── static/           # 静态资源
├── App.vue           # 应用入口 ✅
├── main.js           # 主入口文件 ✅
├── manifest.json     # 应用配置 ✅
├── pages.json        # 页面路由配置 ✅
└── package.json      # 依赖配置 ✅

✅ - 已完成
⏳ - 进行中
❌ - 未开始
```

## 当前进度

### 阶段一：项目初始化和基础架构搭建 ✅
- [x] 创建项目目录结构
- [x] 配置 manifest.json
- [x] 配置 pages.json
- [x] 创建 App.vue 和 main.js
- [x] 创建样式系统

### 阶段二：登录功能实现 ✅
- [x] 登录页面 (login.vue)
- [x] 用户状态管理 (user.js)
- [x] 认证工具函数 (auth.js)

### 阶段三：共有页面迁移（进行中）
- [x] 首页 (home.vue)
- [x] 底部导航组件 (bottom-nav.vue)
- [x] 状态栏组件 (status-bar.vue)
- [ ] AI助理 (assistant.vue)
- [ ] 沟通中心 (communication.vue)
- [ ] 往期档案 (past.vue)
- [ ] 个人中心 (profile.vue)
- [ ] 位置导航 (navigation.vue)
- [ ] 设置 (settings.vue)
- [ ] 帮助中心 (help.vue)

### 阶段四：学员端页面迁移（进行中）
- [x] 日程安排 (schedule.vue)
- [x] 签到 (checkin.vue)
- [x] 座位查询 (seat.vue)
- [ ] 培训详情 (meeting-detail.vue)
- [ ] 学习资料 (materials.vue)
- [ ] 群组列表 (groups.vue)
- [ ] 聊天室 (chat-room.vue)
- [ ] 私聊 (chat-private.vue)
- [ ] 通讯录 (contact.vue)
- [ ] 其他学员页面...

### 阶段五-十：待开始

## 功能特性

### 已实现功能

1. **登录功能**
   - 学员/工作人员身份切换
   - 账号密码登录
   - 手机验证码登录（UI完成，API待对接）
   - 记住密码功能
   - 登录状态保持

2. **首页**
   - 用户信息展示
   - 今日提醒
   - 培训列表展示
   - 功能导航入口
   - 根据角色动态调整功能

3. **日程安排**
   - 日期选择器
   - 日程列表展示
   - 签到状态显示
   - 一键签到功能

4. **签到功能**
   - 二维码展示（待完成）
   - 定位签到
   - 签到状态显示
   - 签到记录查询

5. **座位查询**
   - 我的座位显示
   - 座位图展示
   - 座位状态筛选
   - 座位详情查看
   - 扫码查座功能

6. **状态管理**
   - 用户登录状态
   - 用户信息管理
   - 应用状态管理

7. **API封装**
   - 统一请求封装
   - 错误处理
   - Token管理
   - 模块化API

### 待实现功能

- [ ] AI助理功能
- [ ] 沟通中心（群聊/私聊）
- [ ] 学习资料下载
- [ ] 工作人员端所有页面
- [ ] 归档功能
- [ ] 其他页面...

## UI还原度

### 完全还原的页面
- ✅ 登录页：100%还原
- ✅ 首页：100%还原
- ✅ 日程安排：100%还原
- ✅ 签到页：100%还原
- ✅ 座位查询：100%还原

### 设计规范遵循
- ✅ 颜色：使用渐变色主题
- ✅ 间距：使用统一间距系统
- ✅ 圆角：统一圆角规范
- ✅ 阴影：统一阴影效果
- ✅ 字体：保持原有字体大小
- ✅ 图标：使用emoji代替Font Awesome（待替换）

## 运行指南

### 开发环境

1. 安装依赖
```bash
npm install
```

2. H5开发
```bash
npm run dev:h5
```

3. 微信小程序开发
```bash
npm run dev:mp-weixin
```

4. App开发
```bash
npm run dev:app
```

### 构建发布

1. H5构建
```bash
npm run build:h5
```

2. 微信小程序构建
```bash
npm run build:mp-weixin
```

3. App构建
```bash
npm run build:app
```

## 注意事项

1. **API对接**
   - 当前使用模拟数据
   - 需要对接实际后端API
   - API基础地址：`http://localhost:8080/api`

2. **图标资源**
   - 当前使用emoji作为图标
   - 建议替换为iconfont或图标字体
   - tabBar需要准备图标资源

3. **多端适配**
   - 使用条件编译处理平台差异
   - 测试各平台兼容性
   - 注意不同平台API差异

4. **性能优化**
   - 图片懒加载
   - 页面预加载
   - 长列表优化

## 下一步计划

### 短期目标
1. 完成所有共有页面迁移
2. 完成学员端核心页面
3. API对接和数据管理

### 中期目标
1. 工作人员端页面迁移
2. 归档功能迁移
3. 多端测试和优化

### 长期目标
1. 实时消息功能
2. 数据统计和分析
3. 离线缓存支持

## 贡献指南

1. 保持UI 100%还原
2. 遵循代码规范
3. 完善注释文档
4. 测试多端兼容性

## 版本历史

- v0.1.0 (2025-01-15)
  - 项目初始化
  - 登录功能完成
  - 首页和核心页面完成

## 联系方式

如有问题，请联系开发团队。

---

**开发进度**: 15% (9/62 页面完成)

**预计完成时间**: 15天

**最后更新**: 2025-01-15
