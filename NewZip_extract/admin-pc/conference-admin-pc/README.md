# 智能会议系统 - 管理端PC

## 🎯 系统简介

管理端PC是智能会议系统的核心管控平台，提供完整的会议全生命周期管理功能。

## ⚡ 快速开始

### 方式一：直接打开
```
双击打开: index.html
```

### 方式二：本地服务器
```bash
# Python
python -m http.server 8080

# Node.js
npx http-server -p 8080

# 浏览器访问
http://localhost:8080
```

## 📂 目录结构

```
conference-admin-pc/
├── index.html                           # 主入口 - 会议列表
├── css/
│   ├── glassmorphism.css                # 玻璃态设计系统
│   ├── conference-theme.css             # 会议主题样式
│   └── components.css                   # 组件样式库
├── js/
│   ├── conference-context.js            # 会议上下文管理
│   └── group-system.js                  # 群组系统管理
└── pages/
    ├── conference-dashboard.html        # 数据指挥中心
    ├── registration-mgr.html            # 报名汇总系统
    ├── notification-mgr.html            # 精准通知系统
    ├── collaboration-center.html        # 会务协同系统
    ├── seating-mgr.html                 # 智能排座系统
    └── ai-assistant-console.html        # AI助教系统
```

## 🎨 核心功能

### 1. 会议列表总览 (index.html)
- 多会议并行管理
- 会议卡片展示
- 状态筛选
- 快捷操作

### 2. 数据指挥中心 (pages/conference-dashboard.html)
- 6个实时指标卡片
- 4个数据可视化图表
- 预警信息面板
- 快捷操作面板

### 3. 报名汇总系统 (pages/registration-mgr.html)
- 多渠道报名分析（网页/小程序/移动端）
- 智能证件审核（OCR识别）
- 学员名册管理（Excel/PDF导出）
- 智能分组系统（5种分组依据）
- 一键催报功能（3种模板）

### 4. 精准通知系统 (pages/notification-mgr.html)
- 4渠道通知配置（短信/邮件/微信/企业微信）
- 智能提醒设置（会前1天/2小时/30分钟）
- 多维度受众筛选（状态/部门/职级/分组）
- 7种个性化变量
- 富文本编辑器
- 发送控制（立即/定时/跟踪/重试）

### 5. 会务协同系统 (pages/collaboration-center.html)
- 4种群组管理（协管员群/全员群/学习小组）
- 6类任务类型
- 看板/列表双视图
- 拖拽式任务管理
- 3种签到方式（二维码/人脸识别/NFC）
- 考勤统计和报表

### 6. 智能排座系统 (pages/seating-mgr.html)
- 4种排座算法（智能推荐/部门/职级/随机）
- 可视化座位图
- 拖拽交互
- VIP座位管理
- 冲突检测
- 部门分布统计

### 7. AI助教系统 (pages/ai-assistant-console.html)
- AI对话界面
- 4种AI功能（智能问答/实时翻译/内容总结/智能推荐）
- 对话历史管理
- Markdown渲染
- 快捷提示词
- 知识库管理

## 🔧 技术栈

### 前端框架
- **Vue.js 3** - 响应式框架
- **Element UI** - UI组件库
- **Chart.js** - 数据可视化
- **Font Awesome 6** - 图标库
- **Marked.js** - Markdown渲染
- **Sortable.js** - 拖拽功能

### 设计风格
- **Glassmorphism** - 玻璃态设计
- **CSS3** - 现代化样式
- **渐变色彩** - 紫色渐变系列
- **响应式布局** - 适配多种设备

## 💡 核心概念

### 会议关联机制
所有功能都基于具体会议ID进行操作：

```javascript
// URL参数传递会议ID
pages/conference-dashboard.html?conferenceId=conf_20240615_001

// 全局会议上下文
const conferenceContext = window.conferenceContext;
await conferenceContext.loadConference('conf_20240615_001');
```

### 会议上下文管理
```javascript
// 获取当前会议
const conference = conferenceContext.getConferenceData();

// 同步数据
await conferenceContext.syncData('registration', payload);

// 检查权限
if (conferenceContext.hasPermission('registration:manage')) {
    // 执行操作
}
```

### 群组系统
```javascript
// 初始化群组系统
const groupSystem = await initializeGroupSystem(conferenceId);

// 发送群消息
await groupSystem.sendMessage(groupId, {
    content: '会议即将开始',
    senderId: 'admin_001'
});

// 发送任务到群
await groupSystem.sendTaskToGroup(groupId, task);
```

## 🎯 使用指南

### 1. 查看会议列表
1. 打开 `index.html`
2. 查看所有会议卡片
3. 使用筛选器查找会议
4. 点击"进入管理"进入会议详情

### 2. 管理会议数据
1. 进入数据指挥中心
2. 查看实时指标和图表
3. 处理预警信息
4. 使用快捷操作

### 3. 管理报名信息
1. 进入报名汇总系统
2. 查看多渠道报名数据
3. 使用OCR审核证件
4. 执行智能分组
5. 发送催报通知

### 4. 发送通知
1. 进入精准通知系统
2. 选择通知渠道
3. 配置智能提醒
4. 筛选受众
5. 编辑通知内容
6. 发送或定时发送

### 5. 协同工作
1. 进入会务协同系统
2. 查看群组状态
3. 创建和分配任务
4. 管理考勤签到
5. 生成报表

### 6. 安排座位
1. 进入智能排座系统
2. 选择排座算法
3. 配置排座规则
4. 执行自动排座
5. 手动调整座位
6. 导出座位表

### 7. AI助教
1. 进入AI助教系统
2. 开始对话
3. 使用快捷提示词
4. 管理知识库
5. 查看使用统计

## 🎨 设计特色

### 玻璃态设计
- 半透明背景
- 毛玻璃模糊效果
- 柔和的阴影
- 流畅的动画

### 渐变色彩
- 主色：#667eea → #764ba2
- 辅色：#f093fb → #f5576c
- 强调色：#4facfe → #00f2fe

### 专业图标
- Font Awesome 6线性图标
- 统一的视觉风格
- 清晰的语义表达

### 响应式布局
- 桌面端优化 (1200px+)
- 平板适配 (768px-1199px)
- 移动端支持 (< 768px)

## 📊 数据说明

### 模拟数据
当前系统使用模拟数据进行演示：

```javascript
// 会议数据
{
  id: 'conf_20240615_001',
  name: '2024年度技术峰会',
  status: 'preparing',
  capacity: { total: 500, registered: 356 }
}

// 统计数据
{
  registration: { total: 500, registered: 356, rate: 71.2 },
  tasks: { total: 58, completed: 42, rate: 72.4 },
  attendance: { total: 356, checkedIn: 328, rate: 94.2 }
}
```

### 实际接入
在实际环境中，需要配置后端API地址：

```javascript
// 在 conference-context.js 中配置
const API_BASE_URL = 'https://api.conference.com';
const API_ENDPOINTS = {
  conferences: `${API_BASE_URL}/api/conferences`,
  registration: `${API_BASE_URL}/api/registration`,
  // ...
};
```

## 🔒 权限控制

### 角色权限
```javascript
// 会议管理员权限
permissions: [
  'conference:create',
  'conference:edit',
  'registration:manage',
  'task:assign',
  'notification:send',
  'seating:arrange',
  'ai:manage'
]
```

### 权限检查
```javascript
// 检查权限
if (conferenceContext.hasPermission('registration:manage')) {
    // 允许操作
} else {
    // 拒绝访问
}
```

## 🐛 常见问题

### Q1: 页面样式显示不正常
**A**: 确保所有CSS文件正确加载，检查浏览器控制台。

### Q2: 图表不显示
**A**: 确保Chart.js库正确加载，检查网络连接。

### Q3: 数据不更新
**A**: 当前使用模拟数据，实时更新需要后端支持。

### Q4: 跨域问题
**A**: 使用本地服务器运行，避免file://协议。

## 📱 浏览器支持

### 推荐浏览器
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Edge 90+
- ✅ Safari 14+

### 不支持
- ❌ IE 11及以下版本

## 🚀 性能优化

### 已实现
- 数据缓存
- 事件防抖
- 懒加载图片
- 代码分割

### 待优化
- 虚拟滚动
- Service Worker
- PWA支持
- 离线缓存

## 📚 相关文档

- [项目主文档](../../README.md)
- [开发指南](../../DEVELOPMENT_GUIDE.md)
- [快速启动](../../QUICK_START.md)
- [项目概览](../../PROJECT_OVERVIEW.md)

## 🤝 贡献指南

欢迎提交问题和改进建议！

## 📄 许可证

本项目采用 MIT 许可证。

---

**开发完成**: 2024年10月9日
**当前版本**: 0.4.0-alpha
**系统状态**: 核心功能完成，可立即体验！
