# 慧悟通 uniapp UI优化 - 完整页面清单（70个）

## 📊 页面统计

| 类别 | 数量 | 说明 |
|:---|:---:|:---|
| **通用页面 common/** | 10 | 登录、导航、设置等 |
| **学员端 learner/** | 25 | 学员功能页面 |
| **协管员端 staff/** | 28 | 协管员管理页面 |
| **其他 archive/index/test/** | 7 | 归档、测试页面 |
| **总计** | **70** | 完整项目页面 |

---

## 🎯 优化优先级分类

### P0 - 核心页面（必须）- 10个

#### 通用页面（2个）
- [ ] `common/home.vue` - 首页
- [ ] `common/navigation.vue` - 底部导航

#### 学员端（5个）
- [x] `learner/meeting-detail.vue` - **已完成**
- [ ] `learner/schedule.vue` - 日程安排
- [ ] `learner/seat.vue` - 座位图
- [ ] `learner/checkin.vue` - 签到
- [ ] `learner/groups.vue` - 学员群组

#### 协管员端（3个）
- [ ] `staff/dashboard.vue` - 仪表盘
- [ ] `staff/seat-manage.vue` - 座位管理
- [ ] `staff/task-list.vue` - 任务列表

---

### P1 - 重要页面（应该）- 20个

#### 学员端（10个）
- [ ] `learner/contact.vue` - 通讯录
- [ ] `learner/guide.vue` - 参会须知
- [ ] `learner/notifications.vue` - 通知
- [ ] `learner/task-list.vue` - 任务列表
- [ ] `learner/materials.vue` - 资料
- [ ] `learner/handbook.vue` - 学员手册
- [ ] `learner/evaluation.vue` - 评估
- [ ] `learner/feedback.vue` - 反馈
- [ ] `learner/arrangements.vue` - 会议安排
- [ ] `learner/chat-room.vue` - 聊天室

#### 协管员端（10个）
- [ ] `staff/notice-manage.vue` - 通知管理
- [ ] `staff/registration-manage.vue` - 报名管理
- [ ] `staff/grouping-manage.vue` - 分组管理
- [ ] `staff/user-manage.vue` - 用户管理
- [ ] `staff/department-manage.vue` - 部门管理
- [ ] `staff/training.vue` - 培训管理
- [ ] `staff/data-analysis.vue` - 数据分析
- [ ] `staff/handbook-generate.vue` - 手册生成
- [ ] `staff/alert-handle.vue` - 预警处理
- [ ] `staff/role-manage.vue` - 角色管理

---

### P2 - 辅助页面（可以）- 25个

#### 学员端（10个）
- [ ] `learner/guestbook.vue` - 留言板
- [ ] `learner/highlights.vue` - 精彩瞬间
- [ ] `learner/messages.vue` - 消息
- [ ] `learner/scan-seat.vue` - 扫码入座
- [ ] `learner/scan-register.vue` - 扫码报到
- [ ] `learner/registration-form.vue` - 报名表单
- [ ] `learner/registration-status.vue` - 报名状态
- [ ] `learner/seat-detail.vue` - 座位详情
- [ ] `learner/task-detail.vue` - 任务详情
- [ ] `learner/chat-private.vue` - 私聊

#### 协管员端（10个）
- [ ] `staff/notice-edit.vue` - 通知编辑
- [ ] `staff/mobile-notice.vue` - 移动端通知
- [ ] `staff/mobile-alert.vue` - 移动端预警
- [ ] `staff/alert-config.vue` - 预警配置
- [ ] `staff/task-detail.vue` - 任务详情
- [ ] `staff/task-feedback.vue` - 任务反馈
- [ ] `staff/tenant-manage.vue` - 租户管理
- [ ] `staff/tenant-detail.vue` - 租户详情
- [ ] `staff/system-settings.vue` - 系统设置
- [ ] `staff/api-config.vue` - API配置

#### 其他页面（5个）
- [ ] `common/profile.vue` - 个人中心
- [ ] `common/settings.vue` - 设置
- [ ] `common/assistant.vue` - 助手
- [ ] `common/guide.vue` - 指南
- [ ] `common/system-intro.vue` - 系统介绍

---

### P3 - 低优先级（延后）- 14个

#### 归档页面（5个）
- [ ] `archive/feedback.vue` - 归档反馈
- [ ] `archive/highlights.vue` - 归档精彩
- [ ] `archive/materials.vue` - 归档资料
- [ ] `archive/messages.vue` - 归档消息
- [ ] `archive/reports.vue` - 归档报告

#### 通用页面（4个）
- [ ] `common/past.vue` - 过往会议
- [ ] `common/help.vue` - 帮助
- [ ] `common/communication.vue` - 通讯
- [ ] `common/communication.vue` - 通讯

#### 测试/登录（5个）
- [ ] `index/login.vue` - 登录页
- [ ] `test/test-input.vue` - 测试输入
- [ ] `learner/evaluation-result.vue` - 评估结果
- [ ] `learner/feedback-result.vue` - 反馈结果
- [ ] `staff/logs.vue` - 日志
- [ ] `staff/database-backup.vue` - 数据库备份
- [ ] `staff/chat-room.vue` - 聊天室（staff）
- [ ] `staff/training-create.vue` - 培训创建
- [ ] `staff/user-detail.vue` - 用户详情

---

## ⏱️ 时间估算

### 优化速度
- **熟练后：** 5分钟/页
- **初期：** 10分钟/页
- **平均：** 8分钟/页

### 分阶段时间
- **P0核心页面（10个）：** 80分钟（1.3小时）
- **P1重要页面（20个）：** 160分钟（2.7小时）
- **P2辅助页面（25个）：** 200分钟（3.3小时）
- **P3低优先级（14个）：** 112分钟（1.9小时）

### 总计
- **页面总数：** 70个（已完成1个）
- **剩余页面：** 69个
- **预计总时间：** 约9.2小时

---

## 📅 建议执行计划

### 第1阶段：核心页面（1.5小时）
**目标：** 完成最常用的10个核心页面
- 完成后基本可用
- 覆盖80%的用户场景

### 第2阶段：重要页面（3小时）
**目标：** 完成重要的20个页面
- 提升整体体验
- 覆盖95%的用户场景

### 第3阶段：辅助页面（3.5小时）
**目标：** 完成辅助的25个页面
- 完善细节体验
- 达到100%覆盖

### 第4阶段：低优先级（2小时）
**目标：** 完成剩余14个页面
- 归档、测试等页面
- 锦上添花

---

## 🎯 当前进度

```
完成度: ▱▱▱▱▱▱▱▱▱▱▱▱▱▱ 1/70 (1.4%)

P0核心: █▱▱▱▱▱▱▱▱▱▱ 1/10 (10%)
P1重要: ▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱ 0/20 (0%)
P2辅助: ▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱ 0/25 (0%)
P3低优: ▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱▱ 0/14 (0%)
```

---

## 🚀 立即开始

我建议按以下顺序优化：

### 第1批（现在开始）- P0核心页面
1. common/home.vue
2. common/navigation.vue
3. learner/schedule.vue
4. learner/seat.vue
5. learner/checkin.vue
6. learner/groups.vue
7. staff/dashboard.vue
8. staff/seat-manage.vue
9. staff/task-list.vue

### 第2批 - P1重要页面
contact.vue, guide.vue, notifications.vue 等20个

### 第3批 - P2辅助页面
剩余25个

### 第4批 - P3低优先级
剩余14个

---

**确认：** 现在开始优化P0的剩余9个核心页面吗？
