# 智能排座系统 - 多日程场景PRD

## 📋 文档基本信息

- **产品名称**: 多日程智能排座系统
- **版本**: 2.0（整合版 - 包含便捷化优化）
- **编制日期**: 2026年2月27日
- **最后更新**: 2026年2月27日
- **文档类型**: 产品需求文档（PRD）
- **适用范围**: 会议管理后台排座模块
- **相关功能**: 日程确认、座位自适应、场景管理、排座复用、参会人员导入
- **包含内容**: 多日程管理、三种排座模式、决策树、参会人员导入流程、排座复用全功能说明
- **状态**: 📋 设计完成 → 🔄 待开发 → ✅ 待测试

---

## 一、需求概述

### 1.1 需求背景

在多日程会议中，同一个会议可能因为日程安排的不同而涉及不同的会场、不同的参会人数和不同的座位配置。例如：

- **开幕式**：全体参会者在大会场（800席）
- **分组讨论（Day 2-1）**：分3个小分会场（各200席）
- **闭幕式**：全体参会者在大会场（800席）

在这种场景下，**同一个参会人员在不同日程中可能需要坐在不同的座位**，甚至在**不同的会场**。

### 1.2 用户痛点

当前系统存在的问题：

1. **单日程限制** - 系统只支持单一会场配置，无法处理多日程多会场场景
2. **座位冲突** - 多日程中的座位分配没有明确的关联关系，容易产生混淆
3. **无日程切换** - 排座人员需要为每个日程重新配置会场和排座，效率低下
4. **数据隔离不足** - 不同日程的座位数据混在一起，难以维护和查询

### 1.3 解决方案概述

在执行智能排座之前，**添加日程选择和确认流程**，确保：

- 用户明确指定本次排座操作针对哪个日程
- 自动加载该日程对应的会场配置和座位信息
- 排座结果与特定日程绑定
- 支持多日程的独立管理和对比

---

## 二、现有功能分析

### 2.1 当前排座系统的核心功能

#### 2.1.1 会场配置
```
┌─ 会议 (Conference)
│   ├─ 日程 (Schedule) 【新增】
│   ├─ 会场类型选择 (normal/utype/round)
│   ├─ 座位区域管理 (SeatingArea)
│   │   ├─ 基本信息 (名称、类型、容量)
│   │   ├─ 尺寸配置 (行数、列数、角度)
│   │   ├─ 座位生成 (Seats)
│   │   │   ├─ 座位状态 (available/occupied/reserved)
│   │   │   ├─ VIP座位标记
│   │   │   └─ 特殊需求 (轮椅、同传、视线)
│   │   └─ 过道配置 (行级/列级过道)
│   └─ 参会人员管理 (Attendee)
│       ├─ 基本信息
│       ├─ VIP标记
│       └─ 特殊需求
```

#### 2.1.2 排座算法
- **智能推荐**: VIP优先 + 部门集中 + 职级排序
- **按部门排座**: 部门内集中 + 自动区域分配
- **按职级排座**: 高职级前排 + 等级判断
- **按分区排座**: 均匀分配到各区域

#### 2.1.3 座位调整能力
- 拖拽交换座位
- 手动分配人员
- 批量清空/填充
- 插入/删除座位
- 变更历史记录

#### 2.1.4 数据导入导出
- Excel导入（名单+座位号）
- 座位表导出
- 变更历史导出

---

## 三、新增功能设计

### 3.1 日程管理体系

#### 3.1.1 数据结构设计

```javascript
// 日程数据结构
{
  id: 'sched_001',                    // 日程ID
  conferenceId: 'conf_001',           // 所属会议ID
  name: '开幕式',                      // 日程名称
  description: '全体参会者出席',       // 日程描述
  date: '2026-06-15',                 // 日程日期
  timeStart: '09:00',                 // 开始时间
  timeEnd: '11:30',                   // 结束时间
  venueId: 'venue_a',                 // 会场ID（关键！）
  status: 'draft',                    // 日程状态：draft/approved/executed
  
  // 座位配置快照
  seatingSnapshot: {
    seatingAreas: [],                 // 该日程对应的座位区配置
    attendees: [],                    // 该日程参会的人员列表
    seatingResult: {}                 // 该日程的排座结果
  },
  
  // 出席人员
  attendeeList: [
    {
      id: 'att_001',
      name: '张三',
      department: '市政府办',
      position: '主任',
      attendance: 'confirm',          // confirm/pending/cancel
      seatId: null                    // 本日程的座位ID
    }
  ],
  
  // 元数据
  createdAt: '2026-02-27',
  updatedAt: '2026-02-27',
  creator: 'admin',
  notes: '备注'
}

// 会场数据结构
{
  id: 'venue_a',
  name: '大会场',
  capacity: 800,
  layout: 'normal',
  seatingAreas: []                    // 该会场的座位区配置
}
```

#### 3.1.2 日程与排座的关系图

```
会议 (Conference)
├─ 日程1: 开幕式 (Day 1 上午)
│   ├─ 会场: 大会场 (800席)
│   ├─ 参会人数: 750
│   ├─ 座位配置: [配置A]
│   └─ 排座结果: [结果A]
│
├─ 日程2: 分组讨论-分会场1 (Day 2 上午)
│   ├─ 会场: 小会场1 (200席)
│   ├─ 参会人数: 180
│   ├─ 座位配置: [配置B1]
│   └─ 排座结果: [结果B1]
│
├─ 日程3: 分组讨论-分会场2 (Day 2 上午)
│   ├─ 会场: 小会场2 (200席)
│   ├─ 参会人数: 190
│   ├─ 座位配置: [配置B2]
│   └─ 排座结果: [结果B2]
│
└─ 日程4: 闭幕式 (Day 3 上午)
    ├─ 会场: 大会场 (800席)
    ├─ 参会人数: 740
    ├─ 座位配置: [配置A]
    └─ 排座结果: [结果C]
```

### 3.2 交互流程设计

#### 3.2.1 排座操作流程图

```
用户打开排座页面
        ↓
【新增】加载日程列表
        ↓
【新增】展示日程选择器（模态框/下拉菜单）
        ↓
        ├─ 未选择日程状态 ────→ 显示"请选择日程"提示
        │                        禁用排座按钮
        │
        └─ 用户选择日程 ────→ 【新增】确认日程对话框
                                  ├─ 显示日程基本信息
                                  ├─ 显示会场信息和座位容量
                                  ├─ 显示该日程的参会人数
                                  ├─ 显示该日程的已排座人数
                                  └─ 【确认】或【取消】
                                         ↓
                                   确认后加载配置
                                         ↓
                                   加载会场布局
                                         ↓
                                   加载该日程的座位配置
                                         ↓
                                   加载该日程的参会人员名单
                                         ↓
                                   显示该日程的已排座状态
                                         ↓
                                   启用排座算法选择和执行
```

#### 3.2.2 详细交互步骤

**步骤1: 日程选择器**
```
┌─ 日程选择卡片（页面顶部）
│  ├─ 【当前日程标识】
│  │   显示: "当前选择：开幕式 (2026-06-15 09:00~11:30)"
│  │
│  ├─ 【切换日程按钮】
│  │   按钮: "📅 选择日程" 或 "🔄 切换日程"
│  │   点击后打开日程选择模态框
│  │
│  └─ 【日程快速导航】(可选)
│      显示: [开幕式] [分会场1] [分会场2] [闭幕式]
│      点击快速切换
```

**步骤2: 日程选择模态框**
```
┌─────────────────────────────────────────────────┐
│ 📅 选择日程                                  [×]  │
├─────────────────────────────────────────────────┤
│                                                 │
│ 请选择本次排座的日程：                          │
│                                                 │
│ ☐ 开幕式                                       │
│   📅 2026-06-15 09:00~11:30                    │
│   📍 大会场 (800席)                            │
│   👥 参会: 750人 | 已排: 620人                │
│   ═════════════════════════════════════      │
│                                                 │
│ ☐ 分组讨论-分会场1                             │
│   📅 2026-06-16 09:00~11:30                    │
│   📍 小会场1 (200席)                           │
│   👥 参会: 180人 | 已排: 150人                │
│   ═════════════════════════════════════      │
│                                                 │
│ ☐ 分组讨论-分会场2                             │
│   📅 2026-06-16 09:00~11:30                    │
│   📍 小会场2 (200席)                           │
│   👥 参会: 190人 | 已排: 145人                │
│   ═════════════════════════════════════      │
│                                                 │
│ ☐ 闭幕式                                       │
│   📅 2026-06-17 14:00~16:00                    │
│   📍 大会场 (800席)                            │
│   👥 参会: 740人 | 已排: 0人                  │
│   ═════════════════════════════════════      │
│                                                 │
├─────────────────────────────────────────────────┤
│                    [取消]  [确认选择]           │
└─────────────────────────────────────────────────┘
```

**步骤3: 日程确认对话框**
```
┌─────────────────────────────────────────────────┐
│ ⚠️ 确认日程切换                             [×]  │
├─────────────────────────────────────────────────┤
│                                                 │
│ 您即将切换到以下日程进行排座：                  │
│                                                 │
│ 📌 日程名称：开幕式                             │
│ 📅 日期时间：2026-06-15 09:00~11:30           │
│ 📍 会场名称：大会场                             │
│ 🪑 座位容量：800席                              │
│ 👥 参会人数：750人                              │
│ ✅ 已排座位：620人                              │
│ ⏳ 待排座位：130人                              │
│                                                 │
│ 💡 提示：                                       │
│ • 切换日程后，座位图和参会人员名单会更新       │
│ • 这个日程之前的排座结果会被保存                │
│ • 不同日程的排座是独立的                        │
│                                                 │
├─────────────────────────────────────────────────┤
│            [取消]  [确认切换]                   │
└─────────────────────────────────────────────────┘
```

### 3.3 页面UI变化

#### 3.3.1 排座页面布局调整

```
[返回] [保存]
═════════════════════════════════════════════════════════
                    
┌─ 页面顶部 - 日程选择区域 ─────────────────────────────┐
│                                                        │
│  当前日程：开幕式  📅 2026-06-15 09:00~11:30         │
│  会场：大会场  |  容量：800席  |  参会：750人         │
│  已排：620人 (82.7%)  待排：130人                     │
│                                                        │
│  [📅 切换日程] [🎯 日程概览] [📊 对比分析]           │
│                                                        │
└────────────────────────────────────────────────────────┘
                          ↓
    ┌──────────────────────────────────────────┐
    │ 第一步：选择会场布局                      │
    │ [普通式] [U型] [圆形]                    │
    └──────────────────────────────────────────┘
                          ↓
    ┌──────────────────────── 座位布局管理 ───────────────┐
    │                                                    │
    │ [添加区域] [智能推荐▼] [执行排座]               │
    │                                                    │
    │ 搜索座位... [拖拽模式] [快捷键]                   │
    │                                                    │
    │  ┌─ A区 (VIP区域) ──────────────────────────┐   │
    │  │  座位布局（配置+图表）                    │   │
    │  │  [已编辑▼] [删除]                          │   │
    │  └────────────────────────────────────────────┘   │
    │                                                    │
    │  ┌─ B区 (普通区域) ──────────────────────────┐   │
    │  │  座位布局（配置+图表）                    │   │
    │  │  [已编辑▼] [删除]                          │   │
    │  └────────────────────────────────────────────┘   │
    │                                                    │
    │  主席台                                           │
    │  座位图表                                        │
    │                                                    │
    └────────────────────────────────────────────────────┘
                          ↓
    ┌─ 底部面板区域 ─────────────────────────────────────┐
    │ [统计] [参会人员] [快捷操作] [辅助安排] [通知]    │
    └────────────────────────────────────────────────────┘
```

#### 3.3.2 日程选择器的三种UI形式

**形式A: 页头集成式（推荐用于日程较少的场景）**
```
┌────────────────────────────────────────────────────────┐
│ 🪑 智能排座系统                    [已批准] [保存]     │
├────────────────────────────────────────────────────────┤
│                                                        │
│ 会议名称：年度工作会议 | 会议ID: conf_001             │
│ 举办日期：2026-06-15~17 | 地点：国家会议中心          │
│                                                        │
│ ⚡ 日程: [开幕式 ✓] [分会场1] [分会场2] [闭幕式]      │
│          👆 当前选择 | 点击快速切换                   │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**形式B: 卡片式（推荐用于日程较多或信息较多的场景）**
```
┌────────────────────────────────────────────────────────┐
│ 📅 日程选择                                             │
├────────────────────────────────────────────────────────┤
│                                                        │
│ ┌─ 当前日程 ────────────────────────────────────────┐ │
│ │ 开幕式                                              │ │
│ │ 📅 2026-06-15 09:00~11:30                         │ │
│ │ 📍 大会场 (800席) | 参会: 750人 | 已排: 620人    │ │
│ │ [切换日程 ▼]                                      │ │
│ └──────────────────────────────────────────────────┘ │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**形式C: 侧边栏式（推荐用于需要频繁对比的场景）**
```
┌─────────────────────┬────────────────────────────────┐
│ 📅 日程列表          │                                │
├─────────────────────┤         座位图                  │
│ ☑ 开幕式            │                                │
│   📅 Day1 09:00~11:30│                                │
│   👥 750/800        │                                │
│                     │                                │
│ ○ 分会场1           │                                │
│   📅 Day2 09:00~11:30│                                │
│   👥 180/200        │                                │
│                     │                                │
│ ○ 分会场2           │                                │
│   📅 Day2 09:00~11:30│                                │
│   👥 190/200        │                                │
│                     │                                │
│ ○ 闭幕式            │                                │
│   📅 Day3 14:00~16:00│                                │
│   👥 740/800        │                                │
│                     │                                │
└─────────────────────┴────────────────────────────────┘
```

### 3.4 核心功能模块

#### 3.4.1 日程管理模块

**功能清单：**
```
1. 日程列表加载
   - 获取当前会议的所有日程
   - 显示日程基本信息和状态
   - 按时间顺序排序

2. 日程选择
   - 单选日程
   - 显示日程的关键信息
   - 点击【确认】触发切换流程

3. 日程切换
   - 保存当前日程的排座结果（自动或手动）
   - 卸载当前日程的座位配置和参会人员数据
   - 加载新日程的座位配置和参会人员数据
   - 更新页面显示信息

4. 日程概览
   - 查看所有日程的排座进度
   - 对比不同日程的座位使用率
   - 导出日程排座统计报告

5. 日程对标对比 (可选高级功能)
   - 对比两个日程的座位分配差异
   - 识别人员在不同日程中的座位变化
```

**数据流程：**
```
用户选择日程
    ↓
API: GET /api/conferences/{conferenceId}/schedules
    ↓
返回日程列表 [{ 开幕式 }, { 分会场1 }, ...]
    ↓
用户点击【切换日程】
    ↓
确认对话框显示日程详情
    ↓
用户点击【确认】
    ↓
触发 onScheduleChange() 事件
    ↓
API: GET /api/schedules/{scheduleId}/seating
    ↓
返回日程的座位配置、参会人员、排座结果
    ↓
更新Vue的reactive数据
    ↓
刷新座位图和参会人员列表
```

#### 3.4.2 座位配置自适应模块

**功能清单：**
```
1. 会场配置加载
   - 获取日程对应的会场信息
   - 获取该会场的座位区配置
   - 支持配置继承（从上次配置复用）

2. 座位动态调整 (可选功能)
   - 日程切换时自动调整座位区尺寸
   - 根据参会人数自动优化布局
   - 保留用户自定义的过道配置

3. 参会人员同步
   - 获取本日程的参会人员列表
   - 过滤出本日程实际出席的人员
   - 标记已排座和待排座的人员

4. 排座结果恢复
   - 如果该日程有历史排座结果，加载显示
   - 支持撤销/重新排座
```

#### 3.4.3 【新增】排座结果复用模块

**适用场景：**
```
场景1：相同会场的多个日程
├─ 开幕式 (Day 1) → 800席
├─ 闭幕式 (Day 3) → 800席
└─ 两个日程可以复用相同的座位配置和排座结果

场景2：分组讨论的对称分配
├─ 分会场1 + 分会场2 (同时进行)
└─ 参会人数相同，可复用排座算法和结果模板

场景3：多地区分会场
├─ 北京主会场、上海分会场、深圳分会场
├─ 同样规格的会场可复用配置
└─ 相同的参会人数可批量应用排座
```

**功能清单：**
```
1. 座位配置复用
   - 从已配置的日程复制座位区配置
   - 直接应用到新日程（免去重复创建）
   - 支持微调（行数、列数、过道位置）

2. 排座结果复用（带智能自适应）
   - 复制排座结果，自动处理参会人数变化
   - 参会人数减少：自动清空缺席者座位
   - 参会人数增加：在空座位中为新增人员排座
   - 时间冲突检测：警告同一时间段的多会场冲突
   - 从已排座的日程复制排座结果
   - 智能映射座位编号（如果会场尺寸相同）
   - 自动适配参会人员变化（增减人员时重新排座）
   - 支持批量应用到多个日程

3. 排座模板
   - 基于高频日程创建排座模板
   - 保存最佳实践（"开幕式推荐排座"等）
   - 快速套用，减少重复工作

4. 批量操作
   - 一次排座应用到多个相同规格日程
   - 批量复制座位配置
   - 批量复制排座结果（可选是否覆盖已有结果）

5. 智能冲突检测
   - 检查人员是否在同一时间段分配到多个会场
   - 警告容量不足的情况
   - 提示人员转移的机会
```

**操作流程：**
```
┌─ 用户在日程A排座完成
│
├─ 点击【共享到其他日程】
│
├─ 系统自动识别：
│  ├─ 相同会场的日程列表
│  ├─ 相似规格的日程列表
│  └─ 合适的复用候选
│
├─ 显示复用选项对话框
│  ├─ [复制座位配置] → 只复制座位区结构
│  ├─ [复制排座结果] → 复制人员分配
│  ├─ [批量应用] → 应用到多个日程
│  └─ [创建模板] → 保存为可重用模板
│
├─ 选择目标日程
│
├─ 智能适配：
│  ├─ 容量检查 (目标会场 >= 参会人数?)
│  ├─ 人员检查 (是否存在重复分配?)
│  └─ 时间检查 (是否时间冲突?)
│
└─ 应用或询问如何处理冲突
```

#### 3.4.4 三种排座操作模式（便捷化设计）

为平衡便捷性和可控性，系统提供**三种操作模式**，满足不同用户的需求：

##### 模式A：自动推荐模式 ⚡ (最快 - 2步)

**适合用户**：经验丰富，追求效率，信任系统判断

**操作流程**：
```
排座完成 
   ↓
【点击】"🚀 一键排座" 按钮
   ↓
系统自动：
├─ 识别最优排座方式
├─ 检测冲突并自动解决
├─ 自动应用到指定日程
└─ 显示结果摘要
   ↓
【完成】(总耗时：2步 + 3秒处理)
```

**特点**：
- 操作步骤少 (2步)
- 时间最快 (<5秒)
- 风险中等（系统判断可能不完全符合意图）
- 适合重复性、标准化的排座任务

##### 模式B：引导模式 🎯 (推荐 - 3步，**系统默认**)

**适合用户**：大多数用户，希望看到建议但保持控制权

**操作流程**：
```
排座完成
   ↓
【自动弹出】排座建议对话框
   ↓
系统推荐：
├─ ✅ 完全兼容的日程（100%）
│  └─ 可直接复用
├─ ⚠️ 部分兼容的日程（80-95%）
│  └─ 需要智能适配
└─ ❌ 不兼容的日程（<80%）
   └─ 不建议复用
   ↓
用户选择要排座的日程
   ↓
确认排座方式
   ↓
【完成】(总步数：3步，总耗时：~1分钟)
```

**特点**：
- 用户可见决策过程
- 显示系统推荐和兼容度
- 风险低（用户可见再做决定）
- **推荐指数：⭐⭐⭐⭐⭐ 最适合大多数场景**

##### 模式C：高级模式 ⚙️ (完全控制 - 5-6步)

**适合用户**：需要完全控制，复杂场景，VIP人员调整

**操作流程**：
```
排座完成
   ↓
【点击】"⚙️ 高级模式" 按钮
   ↓
【步骤1】手动选择目标日程（支持多选）
   ├─ 显示所有日程及兼容度指标
   └─ 预计影响：XX人，节省时间
   ↓
【步骤2】针对每个日程选择排座模式
   ├─ 复制座位配置 / 复制排座结果 / 智能适配 / 模板应用
   └─ 显示预期结果
   ↓
【步骤3】配置冲突处理策略
   ├─ L1严重冲突：自动拒绝/手动审核
   ├─ L2中等冲突：显示警告继续/暂停
   └─ L3轻度提示：显示/隐藏
   ↓
【步骤4】预览结果
   ├─ 显示每个日程的详细适配摘要
   ├─ 列出所有冲突和警告
   └─ 支持【返回修改】或【继续】
   ↓
【步骤5】确认并应用
   ├─ 显示处理进度
   └─ 支持【暂停】和【取消】
   ↓
【步骤6】查看报告
   ├─ 每个日程的排座统计
   ├─ 时间节省计算
   └─ 需要人工审查的项目列表
   ↓
【完成】(总步数：6步，总耗时：3-5分钟，完全可控)
```

**特点**：
- 完全透明和可控
- 用户可看到每一步的细节
- 风险最低（全面的验证和审核）
- 推荐指数：⭐⭐⭐⭐☆ 复杂场景必选

##### 三种模式对比表

| 维度 | 自动模式 | 引导模式(推荐) | 高级模式 |
|------|--------|---------|--------|
| **操作步骤** | 2步 | 3步 | 6步 |
| **时间消耗** | <5秒 | ~1分钟 | 3-5分钟 |
| **用户可控性** | 低 | 中（推荐+选择） | 高 |
| **适合人群** | 专家用户 | 普通用户 | 高级用户 |
| **出错风险** | 中 | 低 | 最低 |
| **推荐指数** | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐☆ |
| **应用场景** | 紧急/标准化 | 日常使用 | 复杂/涉及VIP |

**用户偏好设置**：系统默认使用【引导模式】，用户可在【设置】中修改默认模式，支持"记住本次选择"。

---

#### 3.4.5 排座快速选择指南（决策树）

当用户选择排座模式时，以下决策树可以快速判断：

```
Q1: 您是否第一次使用排座功能？
├─ 是 → 【强烈推荐】选择【引导模式】
│      系统会自动推荐，您只需点击确认
│
└─ 否，我经验丰富
   └─ Q2: 您现在的排座场景有多复杂？
      ├─ 简单（同一会场，参会人数基本相同）
      │   └─ Q3: 您信任系统的自动判断吗？
      │       ├─ 是，追求速度 → 【自动推荐模式】(2步)
      │       │   ✅ 最快：<5秒完成
      │       │
      │       └─ 不，要看建议 → 【引导模式】(3步)
      │           ✅ 推荐：看到兼容度，选择日程
      │
      └─ 复杂（多会场、多地区、涉及VIP、多个冲突）
          └─ 【高级模式】(6步)
             ✅ 完全可控：逐步细调每个环节
```

**快速场景判断表**：

| 场景 | 推荐模式 | 原因 |
|------|--------|------|
| 首次使用，不确定操作 | 引导模式 | 看到推荐+用户可控 |
| 每周固定相同排座 | 自动模式 | 配置标准，追求快速 |
| 多地区分会场首次协调 | 高级模式 | 需要完全掌控 |
| VIP重要人员座位调整 | 高级模式 | 谨慎起见，逐步验证 |
| 时间紧急需快速完成 | 自动模式 | 让系统自动判断 |
| 不确定哪个日程可复用 | 引导模式 | 系统自动识别兼容日程 |
| 想保存标准排座流程 | 创建模板 | 跨会议复用配置 |

---

#### 3.4.6 排座执行流程升级

**基础流程图：**
```
用户选择排座算法 (智能推荐/按部门/按职级/按分区)
    ↓
系统检查前置条件：
├─ ✓ 日程已选择
├─ ✓ 会场已配置
├─ ✓ 座位区已创建
├─ ✓ 参会人员已导入
└─ ✓ 座位容量 >= 参会人数 (警告：不检查失败)
    ↓
点击【执行排座】
    ↓
显示排座进度对话框
    ↓
执行排座算法
    ↓
更新座位数据
    ↓
显示排座结果摘要
    ↓
询问用户：
├─ [确认排座] → 保存结果到该日程
├─ [调整排座] → 显示手动调整工具
└─ [重新排座] → 清空结果，重新执行
```

**排座完成后的后续操作**：
```
排座完成
   ↓
显示【排座结果摘要】
   ├─ 已排人数、空座位、排座率
   ├─ 时间消耗、使用算法
   └─ 冲突警告（如果有）
   ↓
展示【复用建议】
   ├─ 【一键复用】→ 自动推荐模式
   ├─ 【选择复用】→ 引导模式
   ├─ 【高级设置】→ 高级模式
   └─ 【保存模板】 → 创建可重用模板
```

---

#### 3.4.7 参会人员导入流程设计（便捷化优化）

##### 场景1：新日程的首次导入

**触发条件**：用户创建新日程，尚未导入参会人员

**操作流程**：
```
日程创建完成
   ↓
【系统自动】提示导入参会人员
   ↓
【导入方式选择】
   
选项1：从Excel导入 ⭐⭐⭐⭐⭐（推荐，支持批量）
├─ 【选择文件】→ 上传Excel
├─ 【数据预览】→ 确认导入数据
├─ 【字段映射】→ 关联表头到系统字段
│  ├─ 必填：姓名、部门
│  └─ 可选：职级、特殊需求、备注
├─ 【去重和验证】→ 检查：
│  ├─ 重复人员警告
│  ├─ 必填字段缺失提示
│  └─ 部门和职级合法性检查
└─ 【导入】→ 保存到该日程

选项2：从其他日程复制 ⭐⭐⭐⭐（快速，支持过滤）
├─ 【选择源日程】→ 选择要复制的日程
├─ 【预览人员列表】→ 显示源日程参会人
├─ 【过滤选择】→ 支持按部门、职级过滤
└─ 【复制】→ 复制选中人员到新日程

选项3：手工逐个添加 ⭐⭐⭐（适合小人数）
├─ 显示快速添加表单
├─ 支持【批量粘贴】(Ctrl+V粘贴Excel数据)
└─ 【完成】→ 保存所有人员

选项4：暂不导入，稍后再说 ⭐⭐（临时跳过）
└─ 直接进入排座界面，之后可手动添加人员
```

**结果显示**：
```
✅ 导入成功！

📊 导入统计：
├─ 新增人员：750人
├─ 部门分布：市政府办(180人)、发改委(150人)、财政局(120人)、其他(300人)
└─ 职级分布：正处级及以上(50人)、副处级(120人)、科员及以下(580人)

【下一步】
├─ 【开始排座】→ 进入排座界面
└─ 【查看人员列表】→ 验证导入数据
```

##### 场景2：日程中期新增参会人员

**触发条件**：某日程已有参会人员，现在需要新增部分人员

**操作流程**：
```
【当前日程】已导入750人，已排座620人
   ↓
【用户操作】点击"➕ 新增参会人员"
   ↓
【导入方式选择】（支持Excel导入、从其他日程复制、手工添加）
   ↓
【系统自动】检查新增人员是否已存在
   ├─ 人员已存在：显示警告并跳过
   └─ 人员不存在：直接添加到日程
   ↓
【导入结果】
├─ 新增人员：50人 ✅
└─ 跳过（已存在）：0人
   ↓
【后续操作】
├─ 【立即为新人排座】→ 显示排座对话框
└─ 【先不排，继续导入】→ 返回导入界面
```

##### 场景3：批量更新参会人员信息

**触发条件**：需要更新多个人员的信息（部门、职级、特殊需求等）

**操作流程**：
```
【当前日程】已导入750人
   ↓
【用户操作】点击"✎️ 编辑参会人员"
   ↓
【编辑界面】显示人员列表表格
   ├─ 列：姓名、部门、职级、特殊需求、已排座、操作
   ├─ 支持：
   │  ├─ 单行编辑：点击单元格直接修改
   │  ├─ 多行批量编辑：勾选多行，批量修改部门/职级
   │  ├─ 搜索和过滤：快速定位要修改的人
   │  └─ 导入替换：上传Excel覆盖指定字段
   └─ 保存修改

【修改保存后】
   ├─ 如果该人员已排座，追问：
   │  ├─ 【保持现有座位】→ 座位不变，信息更新
   │  └─ 【重新排座】→ 根据新信息重新分配
   └─ 保存修改
```

##### 导入便捷性对比表

| 操作 | 便捷度 | 耗时 | 推荐场景 |
|------|-------|------|--------|
| Excel批量导入 | ⭐⭐⭐⭐⭐ | 1-2分钟 | 大规模导入(>100人) |
| 从其他日程复制 | ⭐⭐⭐⭐⭐ | 30秒 | 相同参会人群 |
| 手工逐个添加 | ⭐⭐⭐ | 5-10分钟 | 小规模新增(<10人) |
| 批量编辑更新 | ⭐⭐⭐⭐ | 2-3分钟 | 更新特定字段 |

**自动化建议**：
- 第一次使用时，显示"导入向导"
- 记住用户上次使用的导入方式，下次直接推荐
- 支持"导入模板"，预制字段映射规则
- 提供常见部门和职级的自动识别和纠正

#### 3.5.1 数据结构

```javascript
// LocalStorage或Backend存储结构
{
  conferenceId: 'conf_001',
  schedules: [
    {
      id: 'sched_001',
      name: '开幕式',
      venueId: 'venue_a',
      seatingAreas: [...],              // 该日程的座位区配置快照
      seatingResult: {                  // 该日程的排座结果
        seatAssignments: [
          {
            seatId: 'A1_1',
            attendeeId: 'att_001',
            timestamp: '2026-02-27 14:30:00'
          }
        ],
        generatedAt: '2026-02-27 14:30:00',
        algorithm: 'smart'              // 使用的排座算法
      },
      metadata: {
        status: 'draft',
        changeCount: 5,
        lastModified: '2026-02-27 14:35:00'
      }
    }
  ]
}
```

#### 3.5.2 API端点设计（后端需求）

```
GET /api/conferences/{conferenceId}/schedules
  获取会议的所有日程列表
  返回: { schedules: [...], total: 4 }

GET /api/schedules/{scheduleId}
  获取特定日程的详细信息
  返回: { id, name, date, time, venueId, ... }

GET /api/schedules/{scheduleId}/seating-config
  获取该日程的座位配置
  返回: { seatingAreas: [...] }

GET /api/schedules/{scheduleId}/attendees
  获取该日程的参会人员列表
  返回: { attendees: [...], total: 750 }

GET /api/schedules/{scheduleId}/seating-result
  获取该日程的历史排座结果
  返回: { seatAssignments: [...], metadata: {...} }

POST /api/schedules/{scheduleId}/seating-result
  保存该日程的排座结果
  Body: { seatAssignments: [...], algorithm: 'smart' }

PUT /api/schedules/{scheduleId}/activate
  激活（切换到）特定日程
  返回: { success: true, message: '已切换到开幕式' }

【新增API端点 - 排座复用功能】

GET /api/schedules/{scheduleId}/reusable-schedules
  查询可复用该日程座位配置的其他日程
  返回: {
    configReusable: [ {...schedule info...} ],  // 座位配置兼容的日程
    resultReusable: [ {...schedule info...} ],  // 排座结果可复用的日程
    recommendations: [...]                       // 推荐复用方案
  }

POST /api/schedules/{sourceScheduleId}/copy-config/{targetScheduleId}
  复制源日程的座位配置到目标日程
  Body: { overwrite: true }
  返回: { success: true, copiedAreas: [...] }

POST /api/schedules/{sourceScheduleId}/copy-result/{targetScheduleId}
  复制源日程的排座结果到目标日程（带智能适配）
  Body: { 
    adaptMode: 'smart|keep|reassign',  // 适配策略
    overwrite: false                     // 是否覆盖已有排座
  }
  返回: {
    success: true,
    appliedAssignments: [...],
    adaptedCount: 45,
    conflictCount: 0,
    notes: "已清空5个缺席者座位，为40个新增人员排座"
  }

POST /api/schedules/batch-copy-result
  批量复制排座结果到多个日程
  Body: {
    sourceScheduleId: 'sched_001',
    targetScheduleIds: ['sched_002', 'sched_003', 'sched_004'],
    adaptMode: 'smart'
  }
  返回: {
    success: true,
    results: [
      { scheduleId: 'sched_002', applied: true, notes: '...' },
      { scheduleId: 'sched_003', applied: true, notes: '...' },
      { scheduleId: 'sched_004', applied: false, reason: '容量不足' }
    ]
  }

POST /api/seating-templates
  创建排座模板（基于已排座的日程）
  Body: {
    name: '大会场标准排座',
    sourceScheduleId: 'sched_001',
    description: '适用于800席会场，800人全体大会的推荐排座'
  }
  返回: { templateId: 'tpl_001' }

GET /api/seating-templates
  获取所有排座模板
  返回: { templates: [...] }

POST /api/seating-templates/{templateId}/apply/{targetScheduleId}
  应用模板到指定日程
  Body: { adaptMode: 'smart' }
  返回: { success: true, appliedAssignments: [...] }
```

#### 3.5.3 前端排座复用功能实现

```javascript
// 排座完成后显示的复用选项
const showSeatingReuseOptions = async (sourceScheduleId) => {
  try {
    // 1. 查询可复用的日程列表
    const response = await fetch(
      `/api/schedules/${sourceScheduleId}/reusable-schedules`
    );
    const data = await response.json();
    
    // 2. 显示复用方案对话框
    displayReuseModal({
      sourceScheduleId,
      configReusable: data.configReusable,      // 可复用座位配置
      resultReusable: data.resultReusable,      // 可复用排座结果
      recommendations: data.recommendations     // 推荐方案
    });
  } catch (error) {
    console.error('加载可复用日程失败', error);
  }
};

// 复制座位配置
const copySeatingConfig = async (sourceId, targetId) => {
  try {
    const response = await fetch(
      `/api/schedules/${sourceId}/copy-config/${targetId}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ overwrite: true })
      }
    );
    const result = await response.json();
    
    if (result.success) {
      toast.success(`座位配置已复制到${getScheduleName(targetId)}`);
      // 刷新目标日程的座位配置
      await loadScheduleSeatingConfig(targetId);
    }
  } catch (error) {
    console.error('复制座位配置失败', error);
    toast.error('复制座位配置失败');
  }
};

// 智能复制排座结果（带自适应）
const copySeatingResult = async (sourceId, targetId, adaptMode = 'smart') => {
  try {
    const response = await fetch(
      `/api/schedules/${sourceId}/copy-result/${targetId}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          adaptMode,     // 'smart'=智能适配，'keep'=保持不变，'reassign'=重新排座
          overwrite: false
        })
      }
    );
    const result = await response.json();
    
    if (result.success) {
      // 显示适配结果摘要
      showAdaptationSummary({
        targetScheduleName: getScheduleName(targetId),
        appliedCount: result.appliedAssignments.length,
        adaptedCount: result.adaptedCount,
        conflictCount: result.conflictCount,
        notes: result.notes
      });
      
      // 刷新目标日程的排座结果
      await loadScheduleSeatingResult(targetId);
    } else {
      toast.warning(result.message);
    }
  } catch (error) {
    console.error('复制排座结果失败', error);
    toast.error('复制排座结果失败');
  }
};

// 批量复制到多个日程
const batchCopySeatingResult = async (sourceId, targetIds, adaptMode = 'smart') => {
  try {
    const response = await fetch(
      `/api/schedules/batch-copy-result`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sourceScheduleId: sourceId,
          targetScheduleIds: targetIds,
          adaptMode
        })
      }
    );
    const result = await response.json();
    
    if (result.success) {
      // 显示批量复制结果
      showBatchReuseResults(result.results);
      
      // 刷新所有目标日程
      for (const targetId of targetIds) {
        await loadScheduleSeatingResult(targetId);
      }
    }
  } catch (error) {
    console.error('批量复制排座结果失败', error);
    toast.error('批量复制排座结果失败');
  }
};

// 创建排座模板
const createSeatingTemplate = async (sourceScheduleId, templateName, description) => {
  try {
    const response = await fetch(
      `/api/seating-templates`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: templateName,
          sourceScheduleId,
          description
        })
      }
    );
    const result = await response.json();
    
    if (result.templateId) {
      toast.success(`模板"${templateName}"创建成功`);
      // 刷新模板列表
      await loadSeatingTemplates();
    }
  } catch (error) {
    console.error('创建模板失败', error);
    toast.error('创建模板失败');
  }
};

// 应用模板到日程
const applySeatingTemplate = async (templateId, targetScheduleId, adaptMode = 'smart') => {
  try {
    const response = await fetch(
      `/api/seating-templates/${templateId}/apply/${targetScheduleId}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ adaptMode })
      }
    );
    const result = await response.json();
    
    if (result.success) {
      toast.success(`模板已应用到${getScheduleName(targetScheduleId)}`);
      // 刷新排座结果
      await loadScheduleSeatingResult(targetScheduleId);
    }
  } catch (error) {
    console.error('应用模板失败', error);
    toast.error('应用模板失败');
  }
};
```

---

## 四、应用场景分析

### 4.1 场景一：全国代表大会（3日程）

**会议背景：**
- 举办单位：全国会议
- 参会人数：800人
- 会议形式：全体会议 + 分组讨论 + 闭幕会
- 会场数量：1个主会场 + 4个分会场

**场景描述：**

```
Day 1 - 上午 09:00-11:30: 开幕式
├─ 会场: 主会场
├─ 座位: 800席
├─ 参会人数: 800人
├─ 排座方式: 智能推荐
│  └─ VIP前排/居中 + 按部门集中
└─ 结果: 全部安排完毕 (800/800)

Day 2 - 上午 09:00-11:30: 分组讨论（分4个分会场同时进行）
├─ 分会场1 (主题：技术创新)
│  ├─ 会场: 分会场A
│  ├─ 座位: 200席
│  ├─ 参会人数: 180人 (从Day1的800人中按部门分配)
│  ├─ 排座方式: 按职级
│  └─ 结果: 180/200 (20个空座)
├─ 分会场2 (主题：产业升级)
│  ├─ 会场: 分会场B
│  ├─ 座位: 200席
│  ├─ 参会人数: 210人
│  ├─ 排座方式: 按职级
│  └─ 结果: 210/200 (超座！需要调整)
├─ 分会场3 (主题：可持续发展)
│  ├─ 会场: 分会场C
│  ├─ 座位: 200席
│  ├─ 参会人数: 200人
│  ├─ 排座方式: 按职级
│  └─ 结果: 200/200 (满座)
└─ 分会场4 (主题：对外合作)
   ├─ 会场: 分会场D
   ├─ 座位: 200席
   ├─ 参会人数: 210人
   ├─ 排座方式: 按职级
   └─ 结果: 210/200 (超座！需要调整)

Day 3 - 下午 14:00-16:00: 闭幕式
├─ 会场: 主会场
├─ 座位: 800席
├─ 参会人数: 790人 (有人早退)
├─ 排座方式: 智能推荐
│  └─ 基于Day1的排座结果进行微调
└─ 结果: 790/800 (10个空座)
```

**系统应对方式：**
1. ✅ 用户选择【开幕式】→ 加载800席主会场配置
2. ✅ 排座800人 → 保存为Day1的结果
3. ✅ 用户选择【分会场1】→ 卸载主会场，加载分会场A配置
4. ✅ 自动过滤参会人员为"分会场1"组别的180人
5. ✅ 排座180人 → 保存为Day2-分会场1的结果
6. ✅ 用户切换到【分会场2】→ 重复步骤3-5
7. ✅ 用户选择【闭幕式】→ 加载主会场配置
8. ✅ 自动加载790人 + 参考Day1的座位分配
9. ✅ 执行微调算法 → 保存为Day3的结果

### 4.2 场景二：跨地域分会场会议（地区+场景二分）

**会议背景：**
- 举办单位：全国性企业年会
- 参会人数：2000人
- 会议形式：统一线上/线下混合
- 场景：北京主会场 + 上海、深圳、西安、成都分会场

**场景描述：**

```
【一级分类：地区】
├─ 北京主会场 (600人)
│  ├─ 全体大会 (09:00-11:00) - 600人 - 800席
│  ├─ 区域分论坛 (14:00-16:00) - 600人 - 3个分会场(各200席)
│  └─ 闭幕会 (16:30-17:30) - 600人 - 800席
│
├─ 上海分会场 (400人)
│  ├─ 全体大会 (09:00-11:00) - 400人 - 400席
│  ├─ 区域分论坛 (14:00-16:00) - 400人 - 2个分会场(各200席)
│  └─ 闭幕会 (16:30-17:30) - 400人 - 400席
│
├─ 深圳分会场 (350人)
│  └─ ...
│
├─ 西安分会场 (350人)
│  └─ ...
│
└─ 成都分会场 (300人)
   └─ ...
```

**数据结构变化：**
```javascript
schedule: {
  id: 'sched_bj_01',
  conferenceId: 'conf_001',
  name: '北京主会场-全体大会',
  region: 'beijing',              // 【新增】地区标识
  venueId: 'venue_bj_main',
  organizedBy: 'bj_manager_001',  // 【新增】负责部门/人员
  seatingAreas: [...],
  attendeeList: [
    {
      id: 'att_001',
      name: '张三',
      region: 'beijing',           // 【新增】人员对应地区
      department: '北京公司',
      attendance: 'confirm'
    }
  ]
}
```

**系统应对方式：**
1. ✅ 支持地区级别的日程筛选
2. ✅ 支持按地区查看排座进度
3. ✅ 支持地区间的数据对比（座位利用率、排座效率等）
4. ✅ 支持批量导出各地区的排座结果
5. ✅ 支持地区间的参会人员转移（人员变动场景）

### 4.3 场景三：相同会场多日程的排座复用

**会议背景：**
- 会议类型：年度工作会议
- 参会人数：800人（全体参会）
- 会场：同一个大会场（800席）
- 日程：开幕式、主题演讲、闭幕式（3个日程，同一会场）

**场景描述：**

```
Day 1 - 开幕式 (800席会场)
├─ 参会人数：800人
├─ 排座结果：按智能推荐算法排座
│  └─ VIP前排/居中 + 按部门集中 + 职级排序
├─ 座位分配：800/800 完成
└─ 状态：✅ 排座完成

Day 2 - 主题演讲 (800席会场，参会人数减少)
├─ 参会人数：750人（部分人员因故不参加）
├─ 场景：
│  └─ 前一天已排座的800个座位配置可以直接复用
├─ 操作流程：
│  ├─ 【复用开幕式的座位配置】
│  ├─ 系统自动识别"开幕式和主题演讲使用同一会场"
│  ├─ 复用座位区配置（行、列、过道等完全相同）
│  ├─ 【复用开幕式的排座结果】
│  ├─ 系统自动适配参会人数（800→750）
│  ├─ 对新增缺席的50人座位进行清空
│  ├─ 其余750人保持原有座位不变（推荐）或重新排座（可选）
│  └─ 完成时间：< 1分钟
└─ 结果：✅ 座位配置 100% 复用，排座结果 93.75% 复用

Day 3 - 闭幕式 (800席会场，参会人数恢复)
├─ 参会人数：790人（有人返回，1人提前离场）
├─ 场景：再次使用相同会场
├─ 操作流程：
│  ├─ 【复用开幕式的座位配置】（或【复用主题演讲的配置】）
│  ├─ 【选择复用排座结果】
│  ├─ 系统推荐：
│  │  ├─ 方案A：完全复用开幕式排座（800人中750人保持原座）
│  │  ├─ 方案B：为新增40人增量排座，其他保持不变
│  │  └─ 方案C：基于闭幕式的人员构成重新排座
│  └─ 用户选择方案后完成
└─ 结果：✅ 座位配置复用，排座结果可选复用或微调
```

**系统应对方式 - 排座复用核心逻辑：**

```javascript
1️⃣ 用户在开幕式排座完成后
   ↓
2️⃣ 用户点击【共享到其他日程】
   ↓
3️⃣ 系统识别可复用的日程：
   ├─ ✅ 主题演讲（同会场、800席、参会人数750 < 容量）
   ├─ ✅ 闭幕式（同会场、800席、参会人数790 < 容量）
   └─ ❌ 分组讨论（不同会场、200席）← 无法复用（容量不足）
   ↓
4️⃣ 显示复用方案选择：
   ├─ [复制座位配置] → 只复制座区结构、行列、过道
   │  └─ 适用：需要保留用户自定义配置（过道、VIP区等）
   │
   ├─ [复制排座结果] → 复制人员座位分配
   │  └─ 适用：人数相同或略少时（自动清空缺席者座位）
   │
   ├─ [智能适配] → 根据参会人数自动调整
   │  ├─ 容量充足：保持座位配置，智能处理人员变化
   │  ├─ 人数减少：清空缺席者座位，其他保持不变
   │  └─ 人数增加：在空座位中为新增人员排座
   │
   └─ [批量应用] → 同时应用到主题演讲+闭幕式
      └─ 一次操作完成多个日程配置
   ↓
5️⃣ 选择【智能适配】→【批量应用】
   ↓
6️⃣ 系统执行：
   ├─ 主题演讲：复用配置，清空50个缺席者座位 ✅
   ├─ 闭幕式：复用配置，清空10个缺席者座位，为40个新增人员排座 ✅
   └─ 完成：3个日程排座全部完成（耗时 < 2分钟）
```

**技术细节 - 智能适配算法：**

```
输入：
  sourceSchedule  = 开幕式排座结果 { seatAssignments: [...] }
  targetSchedule  = 主题演讲 { attendees: 750人 }

逻辑：
  1. 容量检查
     targetCapacity >= targetAttendeeCount?
     → 800 >= 750? ✅ YES

  2. 座位配置匹配
     targetSeatingConfig == sourceSeatingConfig?
     → 行数、列数、过道位置都相同? ✅ YES

  3. 参会人员映射
     对sourceSchedule中的每个seatAssignment：
     │
     ├─ 如果attendeeId在targetAttendees中：
     │  └─ 保留该座位分配 (KEEP)
     │
     ├─ 如果attendeeId不在targetAttendees中：
     │  └─ 清空该座位 (CLEAR)
     │
     └─ 对于targetAttendees中新增的人员：
        └─ 为其排座到清空的座位 (ASSIGN_NEW)

  4. 结果
     newSeatAssignments = 智能适配后的座位分配
     conflictCount = 0（无冲突）
     
输出：
  可直接应用到targetSchedule，无需重新排座！
```

### 4.4 场景四：会中人员变动场景

**场景描述：**
```
初始状态 (Day 1)：
- 开幕式日程：800人排座完成
- 座位表已打印、APP已推送

突发情况 1：临时增加50人参会
- 操作：在当前日程(开幕式)增加50人名单
- 系统自动检测：座位不足（800+50=850 > 800）
- 提示用户：① 调整会场(升级到更大场地) ② 减少参会人数 ③ 增加座位
- 处理：执行快速排座，为新增50人安排座位

突发情况 2：部分参会人取消
- 操作：将100人标记为"取消参加"
- 系统自动检测：该100人的座位变为"预留空座"
- 选项：① 保留空座(预期会有后续人员补充) ② 清空座位(给新增参会人使用)

突发情况 3：参会人员转移到不同分会场
- 操作：将30人从"分会场1"转移到"分会场2"
- 系统自动：
  ├─ 清空分会场1中这30人的座位
  ├─ 更新分会场2的参会人名单（+30）
  ├─ 对分会场2执行增量排座

突发情况 4：多个日程的排座结果同时调整
- 场景：开幕式、闭幕式都已排座完成
- 突发：需要为30人调换会场
- 操作：
  ├─ 在开幕式中将30人转移出去（清空座位）
  ├─ 选择【同步更新相关日程】
  ├─ 系统自动：
  │  ├─ 检查闭幕式中这30人的座位
  │  ├─ 一并清空（因为两个日程数据相关）
  │  └─ 提示用户需要重新排座
  └─ 最后为这30人在目标会场排座
```

---

## 五、技术实现方案

### 5.1 前端架构升级

#### 5.1.1 Vue.js 响应式数据结构

```javascript
const { ref, reactive, computed } = Vue;

// 【核心新增】日程相关的响应式数据
const currentScheduleId = ref(null);              // 当前选中的日程ID
const currentSchedule = reactive({                // 当前日程的详细信息
  id: null,
  name: '',
  date: '',
  timeStart: '',
  timeEnd: '',
  venueId: null,
  venueName: '',
  capacity: 0,
  status: 'draft'
});

const schedulesList = ref([]);                    // 日程列表
const scheduleHistories = reactive({});           // 多日程的排座历史快照
  // 结构：{ sched_001: { seatingAreas, seatingResult }, ... }

// 【既有改进】支持多日程的参会人员数据
const attendeesBySchedule = reactive({});         // 按日程分类的参会人员
  // 结构：{ sched_001: [...attendees], ... }
const currentAttendees = computed(() => {
  return attendeesBySchedule[currentScheduleId.value] || [];
});

// 【既有改进】支持多日程的座位数据
const seatingAreasBySchedule = reactive({});      // 按日程分类的座位配置
  // 结构：{ sched_001: [...seatingAreas], ... }
const currentSeatingAreas = computed(() => {
  return seatingAreasBySchedule[currentScheduleId.value] || [];
});

// UI控制状态
const showScheduleModal = ref(false);             // 日程选择对话框显示状态
const scheduleChangeConfirm = reactive({          // 日程切换确认对话框
  show: false,
  scheduleId: null,
  scheduleName: '',
  venueName: '',
  capacity: 0,
  attendeeCount: 0,
  assignedCount: 0
});
```

#### 5.1.2 核心方法实现

```javascript
// 1. 加载日程列表
const loadSchedules = async () => {
  try {
    const response = await fetch(
      `/api/conferences/${conference.value.id}/schedules`
    );
    schedulesList.value = await response.json();
  } catch (error) {
    console.error('加载日程失表示失败', error);
  }
};

// 2. 打开日程选择对话框
const openScheduleSelector = () => {
  showScheduleModal.value = true;
};

// 3. 选择日程（点击对话框中的某个日程）
const selectSchedule = (schedule) => {
  // 显示确认对话框
  scheduleChangeConfirm.show = true;
  scheduleChangeConfirm.scheduleId = schedule.id;
  scheduleChangeConfirm.scheduleName = schedule.name;
  scheduleChangeConfirm.venueName = schedule.venueName;
  scheduleChangeConfirm.capacity = schedule.capacity;
  scheduleChangeConfirm.attendeeCount = schedule.attendeeCount;
  scheduleChangeConfirm.assignedCount = schedule.assignedCount;
};

// 4. 确认切换日程
const confirmScheduleChange = async () => {
  const newScheduleId = scheduleChangeConfirm.scheduleId;
  
  // 步骤1：保存当前日程的排座结果
  if (currentScheduleId.value) {
    await saveScheduleSeatingResult(currentScheduleId.value);
  }
  
  // 步骤2：切换日程ID
  currentScheduleId.value = newScheduleId;
  
  // 步骤3：加载新日程的座位配置
  await loadScheduleSeatingConfig(newScheduleId);
  
  // 步骤4：加载新日程的参会人员
  await loadScheduleAttendees(newScheduleId);
  
  // 步骤5：加载新日程的历史排座结果（如果存在）
  await loadScheduleSeatingResult(newScheduleId);
  
  // 步骤6：更新currentSchedule对象
  const schedule = schedulesList.value.find(s => s.id === newScheduleId);
  Object.assign(currentSchedule, schedule);
  
  // 步骤7：关闭对话框
  scheduleChangeConfirm.show = false;
  showScheduleModal.value = false;
  
  // 步骤8：显示切换成功提示
  toast.success(`已切换到日程：${currentSchedule.name}`);
};

// 5. 保存当前日程的排座结果
const saveScheduleSeatingResult = async (scheduleId) => {
  const result = {
    scheduleId,
    seatingAreas: seatingAreas.value,  // 当前座位区配置
    seatAssignments: extractSeatAssignments(),  // 座位分配详情
    algorithm: selectedAlgorithm.value,
    timestamp: new Date().toISOString()
  };
  
  // 存储到历史记录
  scheduleHistories[scheduleId] = result;
  
  // 同时保存到后端
  await fetch(`/api/schedules/${scheduleId}/seating-result`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(result)
  });
};

// 6. 加载日程的座位配置
const loadScheduleSeatingConfig = async (scheduleId) => {
  try {
    const response = await fetch(
      `/api/schedules/${scheduleId}/seating-config`
    );
    const config = await response.json();
    seatingAreasBySchedule[scheduleId] = config.seatingAreas;
    
    // 更新当前座位区
    seatingAreas.value = JSON.parse(JSON.stringify(config.seatingAreas));
  } catch (error) {
    console.error('加载座位配置失败', error);
  }
};

// 7. 加载日程的参会人员
const loadScheduleAttendees = async (scheduleId) => {
  try {
    const response = await fetch(
      `/api/schedules/${scheduleId}/attendees`
    );
    const data = await response.json();
    attendeesBySchedule[scheduleId] = data.attendees;
    
    // 更新当前参会人员
    attendees.value = JSON.parse(JSON.stringify(data.attendees));
  } catch (error) {
    console.error('加载参会人员失败', error);
  }
};

// 8. 加载日程的历史排座结果
const loadScheduleSeatingResult = async (scheduleId) => {
  try {
    const response = await fetch(
      `/api/schedules/${scheduleId}/seating-result`
    );
    const result = await response.json();
    
    if (result && result.seatAssignments) {
      // 恢复座位分配
      applySeatAssignments(result.seatAssignments);
      
      // 显示提示
      toast.info(`已加载该日程的历史排座结果（${result.timestamp}）`);
    }
  } catch (error) {
    console.error('加载排座结果失败', error);
  }
};

// 9. 应用座位分配
const applySeatAssignments = (assignments) => {
  // 清空所有座位
  seatingAreas.value.forEach(area => {
    area.rows.forEach(row => {
      row.seats.forEach(seat => {
        seat.occupant = null;
        seat.status = 'available';
      });
    });
  });
  
  // 应用分配
  assignments.forEach(({ seatId, attendeeId }) => {
    const seat = findSeatById(seatId);
    const attendee = attendees.value.find(a => a.id === attendeeId);
    if (seat && attendee) {
      seat.occupant = attendee;
      seat.status = 'occupied';
    }
  });
};
```

### 5.2 交互设计细节

#### 5.2.1 日程选择器组件

```vue
<template>
  <!-- 日程选择对话框 -->
  <div v-if="showScheduleModal" class="modal-overlay">
    <div class="modal-content large-modal">
      <div class="modal-header">
        <h3>📅 选择日程</h3>
        <button class="close-btn" @click="closeScheduleModal">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="schedule-list">
          <div 
            v-for="schedule in schedulesList"
            :key="schedule.id"
            class="schedule-item"
            :class="{ 
              active: schedule.id === currentScheduleId,
              selected: selectedScheduleForChange?.id === schedule.id
            }"
            @click="selectSchedule(schedule)"
          >
            <div class="schedule-info">
              <div class="schedule-name">
                {{ schedule.name }}
                <span v-if="schedule.id === currentScheduleId" class="badge-current">
                  当前
                </span>
              </div>
              <div class="schedule-time">
                📅 {{ schedule.date }} {{ schedule.timeStart }}~{{ schedule.timeEnd }}
              </div>
              <div class="schedule-venue">
                📍 {{ schedule.venueName }} ({{ schedule.capacity }}席)
              </div>
              <div class="schedule-stats">
                👥 参会: {{ schedule.attendeeCount }}人 | 
                ✅ 已排: {{ schedule.assignedCount }}人 | 
                📊 {{ Math.round(schedule.assignedCount / schedule.attendeeCount * 100) }}%
              </div>
            </div>
            <div class="schedule-progress">
              <div class="progress-bar">
                <div 
                  class="progress-fill"
                  :style="{ 
                    width: (schedule.assignedCount / schedule.attendeeCount * 100) + '%'
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-outline" @click="closeScheduleModal">
          取消
        </button>
        <button 
          class="btn-primary" 
          @click="confirmScheduleChange"
          :disabled="!selectedScheduleForChange"
        >
          确认选择
        </button>
      </div>
    </div>
  </div>

  <!-- 日程切换确认对话框 -->
  <div v-if="scheduleChangeConfirm.show" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <h3>⚠️ 确认日程切换</h3>
        <button class="close-btn" @click="scheduleChangeConfirm.show = false">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="confirm-info">
          <p>您即将切换到以下日程进行排座：</p>
          <div class="info-box">
            <div class="info-row">
              <span class="label">📌 日程名称</span>
              <span class="value">{{ scheduleChangeConfirm.scheduleName }}</span>
            </div>
            <div class="info-row">
              <span class="label">📍 会场名称</span>
              <span class="value">{{ scheduleChangeConfirm.venueName }}</span>
            </div>
            <div class="info-row">
              <span class="label">🪑 座位容量</span>
              <span class="value">{{ scheduleChangeConfirm.capacity }}席</span>
            </div>
            <div class="info-row">
              <span class="label">👥 参会人数</span>
              <span class="value">{{ scheduleChangeConfirm.attendeeCount }}人</span>
            </div>
            <div class="info-row">
              <span class="label">✅ 已排座位</span>
              <span class="value">
                {{ scheduleChangeConfirm.assignedCount }}人
                ({{ Math.round(scheduleChangeConfirm.assignedCount / scheduleChangeConfirm.attendeeCount * 100) }}%)
              </span>
            </div>
          </div>
          <div class="confirm-tips">
            <p>💡 <strong>提示：</strong></p>
            <ul>
              <li>切换日程后，座位图和参会人员名单会自动更新</li>
              <li>前一个日程的排座结果将被自动保存</li>
              <li>不同日程的排座是完全独立的</li>
              <li>您可以随时回到前一个日程查看其排座结果</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-outline" @click="scheduleChangeConfirm.show = false">
          取消
        </button>
        <button class="btn-primary" @click="performScheduleChange">
          确认切换
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 60vh;
  overflow-y: auto;
}

.schedule-item {
  padding: 16px;
  border: 2px solid var(--gray-200);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.schedule-item:hover {
  border-color: var(--primary-color);
  background: rgba(102, 126, 234, 0.05);
}

.schedule-item.selected {
  border-color: var(--primary-color);
  background: rgba(102, 126, 234, 0.1);
}

.schedule-item.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.schedule-info {
  margin-bottom: 12px;
}

.schedule-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge-current {
  background: var(--success-color);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.schedule-time,
.schedule-venue,
.schedule-stats {
  font-size: 12px;
  color: var(--gray-600);
  margin: 4px 0;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: var(--gray-200);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  transition: width 0.3s ease;
}

.confirm-info {
  padding: 20px;
}

.confirm-info p {
  margin: 0 0 16px 0;
}

.info-box {
  background: var(--gray-50);
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--gray-200);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-weight: 500;
  color: var(--gray-600);
}

.info-row .value {
  font-weight: 600;
  color: var(--gray-800);
}

.confirm-tips {
  background: rgba(34, 197, 94, 0.1);
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid var(--success-color);
}

.confirm-tips p {
  margin: 0;
  color: var(--success-color);
}

.confirm-tips ul {
  margin: 8px 0 0 20px;
  padding: 0;
}

.confirm-tips li {
  color: var(--gray-700);
  margin: 4px 0;
  font-size: 12px;
}
</style>
```

#### 5.2.2 日程切换的页面头部展示

```vue
<template>
  <!-- 日程选择区域 -->
  <div class="schedule-selector-bar" v-if="currentSchedule.id">
    <div class="schedule-info-display">
      <div class="schedule-badge">
        <span class="badge-label">📅 当前日程</span>
        <span class="badge-value">{{ currentSchedule.name }}</span>
      </div>
      <div class="schedule-details">
        <span class="detail-item">
          <i class="fas fa-calendar"></i>
          {{ currentSchedule.date }} {{ currentSchedule.timeStart }}~{{ currentSchedule.timeEnd }}
        </span>
        <span class="detail-item">
          <i class="fas fa-map-marker-alt"></i>
          {{ currentSchedule.venueName }}
        </span>
        <span class="detail-item">
          <i class="fas fa-chair"></i>
          容量: {{ currentSchedule.capacity }}席
        </span>
        <span class="detail-item">
          <i class="fas fa-users"></i>
          参会: {{ currentAttendees.length }}人
        </span>
        <span class="detail-item">
          <i class="fas fa-check-circle"></i>
          已排: {{ getAssignedCount() }}人
          <span class="percentage">({{ Math.round(getAssignedCount() / currentAttendees.length * 100) }}%)</span>
        </span>
      </div>
    </div>

    <div class="schedule-actions">
      <button class="btn-sm btn-outline" @click="showScheduleOverview">
        <i class="fas fa-chart-bar"></i>
        日程概览
      </button>
      <button class="btn-sm btn-primary" @click="openScheduleSelector">
        <i class="fas fa-exchange-alt"></i>
        切换日程
      </button>
    </div>

    <!-- 日程快速导航（可选） -->
    <div class="schedule-quick-nav" v-if="schedulesList.length <= 5">
      <div class="quick-nav-label">快速切换:</div>
      <div class="quick-nav-buttons">
        <button
          v-for="schedule in schedulesList"
          :key="schedule.id"
          class="quick-nav-btn"
          :class="{ active: schedule.id === currentScheduleId }"
          @click="selectSchedule(schedule)"
          :title="schedule.name"
        >
          {{ schedule.name }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.schedule-selector-bar {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(102, 126, 234, 0.05) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 16px;
  align-items: center;
}

.schedule-info-display {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.schedule-badge {
  background: var(--primary-color);
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.badge-label {
  opacity: 0.8;
  font-size: 12px;
}

.badge-value {
  font-size: 16px;
}

.schedule-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--gray-700);
}

.detail-item i {
  color: var(--primary-color);
}

.percentage {
  color: var(--primary-color);
  font-weight: 600;
}

.schedule-actions {
  display: flex;
  gap: 8px;
}

.quick-nav-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--gray-600);
  margin-bottom: 8px;
}

.quick-nav-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-nav-btn {
  padding: 6px 12px;
  border: 1px solid var(--gray-300);
  background: white;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-nav-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.quick-nav-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

@media (max-width: 768px) {
  .schedule-selector-bar {
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .schedule-details {
    gap: 8px;
  }

  .detail-item {
    font-size: 11px;
  }
}
</style>
---

## 六、工作量评估与实施计划

### 6.1 功能点分解

| 功能模块 | 功能点 | 复杂度 | 工作量 | 优先级 |
|---------|--------|--------|--------|--------|
| **日程管理** | 日程列表加载 | ⭐ | 2h | P0 |
| | 日程选择器UI | ⭐⭐ | 4h | P0 |
| | 日程切换确认 | ⭐ | 2h | P0 |
| | 日程快速导航 | ⭐ | 2h | P1 |
| **座位数据管理** | 多日程座位配置隔离 | ⭐⭐⭐ | 6h | P0 |
| | 参会人员按日程过滤 | ⭐⭐ | 4h | P0 |
| | 座位分配历史保存 | ⭐⭐ | 4h | P0 |
| **排座流程升级** | 排座前日程检查 | ⭐ | 2h | P0 |
| | 日程特定的算法参数 | ⭐⭐ | 4h | P1 |
| **排座结果复用** 【新增】 | 座位配置复用 | ⭐⭐ | 4h | P0 |
| | 排座结果智能适配 | ⭐⭐⭐ | 8h | P0 |
| | 排座模板系统 | ⭐⭐ | 6h | P1 |
| | 批量复用到多日程 | ⭐⭐ | 6h | P1 |
| | 复用冲突检测 | ⭐⭐⭐ | 6h | P1 |
| **数据持久化** | LocalStorage扩展 | ⭐ | 2h | P0 |
| | 后端API设计 | ⭐⭐⭐ | 10h | P0 |
| | 数据版本管理 | ⭐⭐ | 4h | P1 |
| **高级功能** | 日程进度概览 | ⭐⭐ | 4h | P1 |
| | 日程对标对比 | ⭐⭐⭐ | 8h | P2 |
| | 日程间人员转移 | ⭐⭐⭐ | 8h | P2 |
| | 批量日程操作 | ⭐⭐ | 4h | P2 |
| **测试与文档** | 单元测试 | ⭐⭐ | 8h | P0 |
| | 集成测试 | ⭐⭐⭐ | 12h | P0 |
| | 用户文档 | ⭐ | 4h | P0 |
| | **合计** | | **115h** | |

### 6.2 实施分阶段计划

**第1阶段：MVP核心功能 (1周)**
- ✅ 日程列表加载与选择UI
- ✅ 日程切换确认流程
- ✅ 多日程座位配置隔离
- ✅ 参会人员按日程过滤
- ✅ 基础的排座历史保存

**第2阶段：排座复用核心功能 (1周)**
- ✅ 座位配置复用机制
- ✅ 排座结果智能适配算法
- ✅ 智能冲突检测
- ✅ 排座复用UI界面
- ✅ 相关API实现

**第3阶段：完善与优化 (1周)**
- ✅ 日程快速导航
- ✅ 排座前日程检查
- ✅ 排座模板系统
- ✅ 批量复用功能
- ✅ 单元测试与集成测试

**第4阶段：高级功能 (可选，2周)**
- ✅ 日程进度概览
- ✅ 日程间数据对比
- ✅ 人员转移功能
- ✅ 批量操作工具

---

## 七、成功标准

### 7.1 功能测试用例

| 测试用例 | 场景 | 验证点 | 预期结果 |
|---------|------|--------|---------|
| TC001 | 用户打开排座页面 | 是否正确加载日程列表 | ✅ 显示所有日程的基本信息 |
| TC002 | 用户点击"切换日程"按钮 | 是否打开日程选择对话框 | ✅ 显示可选日程列表 |
| TC003 | 用户选择某个日程并点击确认 | 是否执行日程切换 | ✅ 加载该日程的座位配置和人员名单 |
| TC004 | 日程切换后执行排座 | 排座结果是否仅关联当前日程 | ✅ 其他日程的排座结果不受影响 |
| TC005 | 用户返回前一个日程 | 是否恢复之前的排座结果 | ✅ 显示该日程之前保存的排座状态 |
| TC006 | 多日程同时进行排座 | 数据是否完全隔离 | ✅ 每个日程独立维护 |
| TC007 | 日程容量不足 | 系统是否给出提示 | ✅ 显示"座位不足，请调整会场或人员"提示 |
| TC008 | 日程中人员发生变动 | 是否自动更新参会人员列表 | ✅ 新增/删除人员后重新排座 |
| TC009 | 浏览器刷新 | 日程数据是否保存 | ✅ 页面刷新后恢复之前的日程上下文 |
| **排座复用相关** | | | |
| TC010 | 用户在日程A排座完成后 | 是否出现【共享到其他日程】选项 | ✅ 显示复用选项按钮 |
| TC011 | 用户点击【共享到其他日程】 | 系统是否识别可复用的日程 | ✅ 列出同会场日程或相似规格日程 |
| TC012 | 复制座位配置 | 目标日程是否获得相同的座位区配置 | ✅ 行数、列数、过道位置完全相同 |
| TC013 | 复制排座结果（参会人数相同） | 目标日程是否直接获得排座结果 | ✅ 所有座位分配结果直接复用 |
| TC014 | 复制排座结果（参会人数减少） | 系统是否自动清空缺席者座位 | ✅ 清空缺席人员座位，其他保持不变 |
| TC015 | 复制排座结果（参会人数增加） | 系统是否为新增人员排座 | ✅ 在空座位中为新增人员排座，无冲突 |
| TC016 | 容量检查：目标会场容量不足 | 系统是否检测到冲突 | ✅ 显示"容量不足，无法复用"提示 |
| TC017 | 人员检查：同一时间段多会场冲突 | 系统是否警告人员转移 | ✅ 显示冲突提示，询问用户如何处理 |
| TC018 | 批量复用到多个日程 | 系统是否同时应用到所有日程 | ✅ 多个日程同时完成配置和排座 |
| TC019 | 创建排座模板 | 系统是否保存模板 | ✅ 模板保存到数据库，可后续重用 |
| TC020 | 应用排座模板 | 目标日程是否应用模板配置 | ✅ 模板配置应用到日程，支持微调 |

### 7.2 用户体验标准

- ✅ 日程切换响应时间 < 1秒
- ✅ 座位图加载时间 < 2秒
- ✅ 排座算法执行时间 < 5秒（1000人+场景）
- ✅ 排座复用操作时间 < 3秒
- ✅ 智能适配计算时间 < 2秒
- ✅ UI界面清晰，日程信息一目了然
- ✅ 操作流程直观，日程切换不超过3步，排座复用不超过4步
- ✅ 错误提示明确，复用冲突信息准确无误
- ✅ 系统自动识别可复用日程，减少用户选择操作

---

## 八、附录：相关资源

### 8.1 参考文档
- [当前排座系统技术方案](./智能排座完整功能实现技术方案.md)
- [排座系统工作总结](./智能排座系统-完整工作总结.md)
- [排座功能诊断报告](./admin-pc/conference-admin-pc/pages/排座功能诊断报告.md)

### 8.2 排座复用场景参考

#### 场景A：相同会场 - 参会人数一致
```
开幕式 (Day 1)
├─ 会场：大会场 (800席)
├─ 参会人数：800人
├─ 排座完成：✅
└─ 关键配置：4个座位区、每区200席、统一过道设置

闭幕式 (Day 3)
├─ 会场：大会场 (800席)
├─ 参会人数：800人
├─ 操作：【复用开幕式排座结果】
└─ 结果：100% 复用，无任何调整 ✅

复用优势：
- 保持座位一致性（参会人从开幕式到闭幕式座位不变）
- 减少排座时间 < 1秒
- 无数据冲突
```

#### 场景B：相同会场 - 参会人数减少
```
开幕式 (Day 1)
├─ 会场：大会场 (800席)
├─ 参会人数：800人
├─ 排座完成：✅

主题演讲 (Day 2)
├─ 会场：大会场 (800席)
├─ 参会人数：750人（50人因故缺席）
├─ 操作：【复用开幕式排座+智能适配】
├─ 系统自动：
│  ├─ 复用座位配置（4个座位区、过道设置等）
│  ├─ 保留750人原有座位
│  └─ 清空50个缺席者座位
└─ 结果：93.75% 复用，微调5个座位 ✅

复用优势：
- 750名参会人座位不变，保持舒适度
- 新增/缺席人员灵活处理
- 排座时间 < 2秒
```

#### 场景C：相同会场 - 参会人数增加
```
开幕式 (Day 1)
├─ 会场：大会场 (800席)
├─ 参会人数：750人
├─ 排座完成：✅
└─ 空座位：50个

闭幕式 (Day 3)
├─ 会场：大会场 (800席)
├─ 参会人数：790人（新增40人）
├─ 操作：【复用开幕式排座+智能适配】
├─ 系统自动：
│  ├─ 保留750人原有座位（100% 不变）
│  ├─ 在50个空座位中为40人排座
│  └─ 警告：10个座位存在冲突风险
└─ 结果：94.9% 复用，新排40个座位 ✅

复用优势：
- 94.9% 的参会人座位位置保持不变
- 自动识别可用空座位
- 提前预警人员冲突
- 排座时间 < 2秒
```

#### 场景D：批量复用 - 多地区分会场
```
北京主会场 (Day 1 - 开幕式)
├─ 800席，800人排座完成 ✅

上海分会场 (Day 1 - 同步) + 深圳分会场 (Day 1 - 同步)
├─ 都是800席，分别有800人参会
├─ 操作：【批量复用到上海、深圳】
├─ 系统自动：
│  ├─ 识别三个会场配置相同
│  ├─ 同时复用座位配置到上海、深圳
│  ├─ 根据各地参会人员自动调整
│  └─ 三个会场同时完成排座
└─ 结果：3个会场全部完成，耗时 < 3秒 ✅

复用优势：
- 批量操作，减少重复配置
- 统一排座标准，保证公平性
- 大幅提升效率（1天 → 1小时）
```

#### 场景E：排座模板 - 标准会议格式
```
保存模板：
├─ 模板名称：「大会场标准排座」
├─ 会场规格：800席 (4个200席区域)
├─ 排座方式：智能推荐（VIP优先+部门集中）
├─ 应用场景：全体会议、全员参会
└─ 保存成功 ✅

后续会议应用：
├─ 新会议创建 → 选择模板【大会场标准排座】
├─ 系统自动：
│  ├─ 创建座位配置（行、列、过道完全相同）
│  ├─ 应用标准排座参数
│  └─ 用户只需导入参会人员即可
└─ 排座完成，无需重复配置 ✅

模板优势：
- 标准化排座流程
- 减少配置时间 80%
- 保证排座质量一致性
- 可重复使用，长期价值
```

### 8.3 数据库变更需求
```sql
-- 会议表新增字段
ALTER TABLE conferences ADD COLUMN enable_multi_schedule BOOLEAN DEFAULT FALSE;

-- 日程表
CREATE TABLE schedules (
  id VARCHAR(50) PRIMARY KEY,
  conference_id VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  date DATE NOT NULL,
  time_start TIME NOT NULL,
  time_end TIME NOT NULL,
  venue_id VARCHAR(50),
  venue_name VARCHAR(100),
  capacity INT,
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (conference_id) REFERENCES conferences(id)
);

-- 日程出席人员表
CREATE TABLE schedule_attendees (
  id VARCHAR(50) PRIMARY KEY,
  schedule_id VARCHAR(50) NOT NULL,
  attendee_id VARCHAR(50) NOT NULL,
  attendance_status VARCHAR(20),
  seat_id VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (schedule_id) REFERENCES schedules(id)
);

-- 日程排座结果表
CREATE TABLE schedule_seating_results (
  id VARCHAR(50) PRIMARY KEY,
  schedule_id VARCHAR(50) NOT NULL UNIQUE,
  seating_areas JSON,
  seat_assignments JSON,
  algorithm VARCHAR(50),
  generated_at TIMESTAMP,
  saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (schedule_id) REFERENCES schedules(id)
);

【新增】排座模板表
CREATE TABLE seating_templates (
  id VARCHAR(50) PRIMARY KEY,
  conference_id VARCHAR(50),
  name VARCHAR(100) NOT NULL,
  description TEXT,
  source_schedule_id VARCHAR(50),
  seating_areas JSON,
  default_algorithm VARCHAR(50),
  usage_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (conference_id) REFERENCES conferences(id)
);

【新增】排座复用历史表
CREATE TABLE seating_reuse_logs (
  id VARCHAR(50) PRIMARY KEY,
  source_schedule_id VARCHAR(50) NOT NULL,
  target_schedule_id VARCHAR(50) NOT NULL,
  reuse_type VARCHAR(50),      -- 'config'|'result'|'template'
  adapt_mode VARCHAR(50),       -- 'smart'|'keep'|'reassign'
  applied_count INT,
  adapted_count INT,
  conflict_count INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (source_schedule_id) REFERENCES schedules(id),
  FOREIGN KEY (target_schedule_id) REFERENCES schedules(id)
);
```

---

## 九、总结

本PRD详细阐述了如何在现有智能排座系统的基础上，通过引入**日程管理体系**和**便捷化优化**，使其能够支持**多日程、多会场、动态变化**的会议排座场景。

### 核心功能创新点：

**基础功能**：
1. **日程隔离** - 每个日程拥有独立的座位配置和排座结果
2. **智能切换** - 自动保存和恢复日程数据，无缝切换
3. **场景适配** - 同一系统支持开幕式、分组讨论、闭幕式等多种场景
4. **数据完整性** - 完整记录每个日程的排座过程和变更历史

**便捷化优化**：
5. **三种排座模式** - 自动/引导/高级，满足不同用户群体
6. **智能决策树** - 快速判断推荐使用哪种模式
7. **排座结果复用** - 座位配置和结果智能复用到其他日程
8. **参会人员导入** - 4种导入方式，3种应用场景
9. **排座模板** - 保存标准排座配置供长期复用
10. **批量操作** - 一次操作应用到多个日程

### 便捷化改进指标：

| 指标 | 改进前 | 改进后 | 提升 |
|------|--------|--------|------|
| 新用户友好度 | ⭐⭐ | ⭐⭐⭐⭐⭐ | 150% |
| 默认操作步骤 | 6步 | 3步 | 50%↓ |
| 新用户首次排座 | 20分钟 | 3分钟 | 85%↓ |
| 用户决策时间 | 5-10分钟 | 5秒 | 99%↓ |
| 多日程排座总耗时 | 20小时 | 4-5小时 | 75%↓ |
| 座位配置复用率 | 0% | 70%+ | ∞ |
| 用户便捷化评分 | 72% | 85%+ | 18%↑ |

### 预期收益：
- 📈 系统适用场景扩大 **3-5倍**
- ⚡ 排座效率提升 **40-75%**（减少重复配置和操作）
- 📊 数据完整性提升 **100%**（历史可追溯）
- 👥 用户满意度提升 **显著**（操作更直观、更便捷）
- 💡 用户成功率提升 **30%**（更多用户能独立完成排座）
- 🎯 新用户学习曲线降低 **70%**（更易上手）

---

**文档版本**: 2.0（整合版）  
**最后更新**: 2026年2月27日 15:00  
**包含内容**: 多日程管理 + 便捷化优化 + 参会人员导入 + 排座复用 + 三种操作模式  
**文档状态**: 📋 设计完成，准备开发  
**下一步**: 将PRD提交给开发团队进行代码实现（预计44小时）  
**作者**: AI Assistant  
**审核**: Pending
