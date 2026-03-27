# 智能排座系统 - 优化模块库

这个目录包含了从 `seating-mgr.html` 拆分出来的优化模块，这些模块提高了代码的复用性和可维护性。

## 📦 模块概览

### 1. seating-numbering-system.js
**座位编号系统 - 生成座位号**

支持7种编号模式：
- `leftToRight` - 从左到右
- `rightToLeft` - 从右到左
- `topToBottom` - 从上到下
- `bottomToTop` - 从下到上
- `centerOutward` - 中心向外（舞台）
- `alternatingLTRStart` - 左右交替（左开始）
- `alternatingRTLStart` - 左右交替（右开始）

**主要导出**:
```javascript
import { 
  generateOptimizedSeatNumber,
  getNumberingModeLabel,
  NUMBERING_MODES
} from './seating-numbering-system.js';

// 生成座位号
const number = generateOptimizedSeatNumber(seat, area);

// 获取编号方式标签
const label = getNumberingModeLabel('leftToRight');  // "从左到右"
```

---

### 2. seating-layout-grid.js
**Grid多块布局系统 - 包含P1-P7缺陷修复**

这是最重要的模块，包含了对Grid布局的7个关键缺陷的修复：

| 缺陷 | 修复函数 | 说明 |
|------|---------|------|
| P1 | `getResponsiveGridCols()` | 响应式列数（4-20列自适应） |
| P2 | `calculateRequiredRows()` | 动态行数扩展 |
| P3 | `calculateAreaGridSize()` | 改进的座位区映射 |
| P4 | `getResponsiveGap()` | 响应式间距（6-15px自适应） |
| P5 | (内置) | Grid保持（小屏幕不改flex） |
| P6 | `initializeGridLayout()` | 完整初始化 |
| P7 | `reapplyLayoutAfterEdit()` | 编辑后重新应用布局 |

**主要导出**:
```javascript
import seatingLayoutGrid from './seating-layout-grid.js';

// P1: 获取响应式列数
const cols = seatingLayoutGrid.getResponsiveGridCols();

// P4: 获取响应式间距
const gap = seatingLayoutGrid.getResponsiveGap();

// P2: 计算所需行数
const rows = seatingLayoutGrid.calculateRequiredRows(areas, gridCols);

// P3: 计算座位区Grid占用
const gridSize = seatingLayoutGrid.calculateAreaGridSize(area);

// 自动布局
seatingLayoutGrid.autoLayoutAreas(areas, gridCols, gridRows);

// 应用布局配置
seatingLayoutGrid.applyLayoutConfig(layoutConfigs, currentLayoutMode, seatingAreas, layoutName);

// P7: 编辑后重新应用布局
seatingLayoutGrid.reapplyLayoutAfterEdit(layoutConfigs, currentLayoutMode, seatingAreas, area);

// P6: 初始化Grid布局
seatingLayoutGrid.initializeGridLayout(seatingAreas);
```

---

### 3. seating-schedule-manager.js
**多日程管理系统 - 日程隔离存储和切换**

提供了多日程管理的完整解决方案，包括日程列表、日程切换、数据隔离存储等功能。

**主要导出**:
```javascript
import { useScheduleManager } from './seating-schedule-manager.js';

// 初始化
const scheduleManager = useScheduleManager({ conference });

// 状态
scheduleManager.schedulesList              // 日程列表数组
scheduleManager.currentScheduleId          // 当前日程ID
scheduleManager.currentSchedule            // 当前日程对象
scheduleManager.scheduleChangeConfirm      // 切换确认对话框

// 方法
scheduleManager.loadSchedules()             // 加载日程列表
scheduleManager.selectSchedule(schedule)    // 选择日程
scheduleManager.confirmScheduleChange()     // 确认切换
scheduleManager.saveScheduleSeatingResult() // 保存排座结果
scheduleManager.loadScheduleSeatingData()   // 加载日程数据
scheduleManager.openScheduleSelector()      // 打开选择对话框
scheduleManager.closeScheduleSelector()     // 关闭选择对话框
```

---

## 🚀 使用示例

### 示例1: 在seating-mgr.html中使用

```javascript
<script type="module">
  // 导入所有模块
  import { generateOptimizedSeatNumber, getNumberingModeLabel } 
    from '../modules/seating-numbering-system.js';
  import seatingLayoutGrid from '../modules/seating-layout-grid.js';
  import { useScheduleManager } from '../modules/seating-schedule-manager.js';

  const { createApp, ref, reactive } = Vue;

  createApp({
    setup() {
      // 初始化scheduleManager
      const scheduleManager = useScheduleManager({ 
        conference: ref(null) 
      });

      // 使用scheduleManager的状态和方法
      const { schedulesList, currentScheduleId } = scheduleManager;

      // 在setup中返回，供模板和其他方法使用
      return {
        schedulesList,
        currentScheduleId,
        ...scheduleManager
      };
    }
  }).mount('#app');
</script>
```

### 示例2: 生成座位号

```javascript
// 获取一个座位
const seat = seatingArea.rows[0].seats[0];

// 生成座位号（根据编号模式）
const seatNumber = generateOptimizedSeatNumber(seat, seatingArea);

// 显示在模板中
console.log(`座位号: ${seatNumber}`);
```

### 示例3: 应用Grid布局

```javascript
// 定义布局配置
const layoutConfigs = {
  flowLayout: { name: '流式布局', ... },
  conferenceHall: { name: '会议厅多块布局', ... }
};

const currentLayoutMode = ref('flowLayout');

// 切换到Grid多块布局
seatingLayoutGrid.switchLayout(layoutConfigs, currentLayoutMode, seatingAreas, 'conferenceHall');

// Grid会自动计算响应式列数和行数
// P1: 根据屏幕宽度确定列数
// P2: 根据座位区数量确定行数
// P4: 根据屏幕宽度确定间距
```

### 示例4: 日程切换

```javascript
// 加载日程列表
await scheduleManager.loadSchedules();

// 用户选择日程
scheduleManager.selectSchedule(scheduleManager.schedulesList[0]);

// 用户点击确认按钮时
await scheduleManager.confirmScheduleChange(
  seatingAreas,      // 座位区ref
  attendees,         // 参会人员ref
  updateStatistics   // 更新统计函数
);

// 新日程的数据已自动加载
// 旧日程的数据已自动保存
```

---

## 📊 响应式配置参考

### Grid列数自适应 (P1)
```javascript
const width = window.innerWidth;

if (width < 600) return 4;           // 手机竖屏
if (width < 768) return 6;           // 平板竖屏
if (width < 1024) return 10;         // 平板横屏
if (width < 1440) return 16;         // 笔记本
return 20;                            // 桌面
```

### 间距自适应 (P4)
```javascript
const width = window.innerWidth;

if (width < 600) return 6;           // 手机: 6px
if (width < 768) return 8;           // 小平板: 8px
if (width < 1024) return 10;         // 大平板: 10px
if (width < 1440) return 12;         // 笔记本: 12px
return 15;                            // 桌面: 15px
```

---

## 🔧 常见用法

### 1. 只使用座位编号系统

```javascript
import { generateOptimizedSeatNumber } from './seating-numbering-system.js';

// 在模板中显示座位号
<span>{{ generateOptimizedSeatNumber(seat, area) }}</span>
```

### 2. 只使用Grid布局系统

```javascript
import seatingLayoutGrid from './seating-layout-grid.js';

// 在mounted钩子中初始化
onMounted(() => {
  seatingLayoutGrid.initializeGridLayout(seatingAreas);
});

// 用户点击"应用Grid布局"时
const switchToGridLayout = () => {
  seatingLayoutGrid.switchLayout(
    layoutConfigs, 
    currentLayoutMode, 
    seatingAreas, 
    'conferenceHall'
  );
};
```

### 3. 只使用多日程管理系统

```javascript
import { useScheduleManager } from './seating-schedule-manager.js';

const scheduleManager = useScheduleManager({ conference });

// 在页面加载时
onMounted(() => {
  scheduleManager.loadSchedules();
});

// 用户选择日程时
const handleScheduleSelect = (schedule) => {
  scheduleManager.selectSchedule(schedule);
};

// 用户点击确认时
const handleConfirm = () => {
  scheduleManager.confirmScheduleChange(seatingAreas, attendees, updateStats);
};
```

---

## ⚠️ 注意事项

### 1. 模块导入
确保使用 `<script type="module">` 标签，才能使用 ES6 import 语法。

```html
<!-- ❌ 错误：无法使用import -->
<script>
  import { ... } from './modules/...';  // 会报错
</script>

<!-- ✅ 正确：支持import -->
<script type="module">
  import { ... } from './modules/...';  // 正常
</script>
```

### 2. 路径问题
根据当前文件的位置调整导入路径：

```javascript
// 如果在pages/seating-mgr.html中
import { ... } from '../modules/seating-numbering-system.js';

// 如果在其他位置，调整相对路径
import { ... } from './modules/seating-numbering-system.js';
```

### 3. 响应式函数
Grid的响应式函数需要在浏览器环境中运行，不能在Node.js中运行。

```javascript
// ❌ 在Node.js中会出错（无window对象）
seatingLayoutGrid.getResponsiveGridCols();

// ✅ 在浏览器中正常运行
```

### 4. 依赖关系
这些模块相对独立，但如果要完整使用Grid功能，需要：
- Vue 3+ (ref, reactive等)
- 需要初始化layoutConfigs配置
- 需要传入seatingAreas等数据引用

---

## 📚 相关文档

- 📄 [优先级1代码拆分完成报告](../文档/优先级1代码拆分完成报告.md)
- 📋 [拆分后浏览器测试指南](../文档/拆分后浏览器测试指南.md)
- 🔍 [代码拆分快速参考](../文档/代码拆分快速参考.md)
- 🎉 [代码拆分完成成果展示](../文档/代码拆分完成成果展示.md)

---

## 🆘 故障排查

### 模块无法导入
```
错误: Failed to load module from ...
解决:
1. 检查文件路径是否正确
2. 确保<script type="module">
3. 检查文件是否存在
```

### 函数未定义
```
错误: Cannot read property 'autoLayoutAreas' of undefined
解决:
1. 检查import是否成功
2. 在console中验证: console.log(seatingLayoutGrid)
3. 检查模块导出是否正确
```

### 日程数据为空
```
现象: schedulesList为空
解决:
1. 调用loadSchedules()加载日程
2. 等待异步操作完成
3. 检查localStorage是否被清空
```

---

## 📞 支持

如有问题，请参考：
1. 对应模块的源代码注释
2. 相关文档中的详细说明
3. 浏览器控制台的错误信息

---

**版本**: 1.0
**创建时间**: 2024年
**状态**: ✅ 生产就绪

*这些模块是从seating-mgr.html优先级1拆分中提取的，经过代码审查和语法检查。*
