# 🔍 seating-mgr.html 完整功能审计报告

> **审计范围**: `seating-mgr.html`（12013行）+ `seating-optimization-patch.js`（872行）+ `conference-context.js`（643行）  
> **技术栈**: Vue 3 Composition API (`createApp` + `setup()`) / XLSX.js / html2canvas / jsPDF  
> **审计日期**: 2025年  

---

## 📐 文件结构总览

| 区间 | 内容 | 行数 |
|------|------|------|
| 1–3430 | CSS 样式（座位、弹窗、布局、拖拽、右键菜单、过道等） | ~3430 |
| 3431–5241 | HTML 模板（导航、日程选择器、弹窗、页头、座位图、面板、上下文菜单、区域配置弹窗） | ~1810 |
| 5242–11990 | `<script type="module">` — Vue setup()（数据声明、方法、computed、onMounted、return） | ~6750 |
| 11991–12013 | 闭合标签 `</script></body></html>` | ~23 |

### 外部依赖引入

| 类型 | 文件 | 用途 |
|------|------|------|
| CSS | `glassmorphism.css`, `conference-theme.css`, `components.css`, `browser-compatibility.css`, `interaction-enhancements.css`, `stat-cards-standard.css`, `seating-function-modal.css`, `schedule-switcher-enhanced.css` | 视觉样式 |
| Icon | Font Awesome 6.4.2 (CDN) | 图标 |
| JS 工具 | `interaction-utils.js`, `auth-service.js`, `app-init.js` | 通用工具/鉴权/初始化 |
| JS 核心 | `seating-optimization-patch.js` | Toast/确认框/进度条/Excel验证/快捷键/批量操作/撤销重做 |
| JS 核心 | `conference-context.js` | 全局会议上下文管理 |
| ES Module | `seating-numbering-system.js` | 座位编号生成（5种模式） |
| ES Module | `seating-layout-grid.js` | 座位区 Grid 布局引擎 |
| ES Module | `seating-schedule-manager.js` | 日程管理器 (useScheduleManager) |
| ES Module | `seating-import-attendees.js` | 参会人员导入模块 |
| ES Module | `seating-interactions.js` | 座位交互模块 |
| ES Module | `seating-arrangements.js` | 辅助安排模块 |
| 第三方 | `xlsx.full.min.js` | Excel 读写 |
| 按需加载 | `html2canvas` (CDN bootcdn) | LED 导示图生成 |
| 按需加载 | `jsPDF` (CDN cdnjs) | PDF 导出 |

---

## 一、会议初始化与数据加载

### 1.1 方法清单

| 方法名 | 行号(约) | 功能 | API调用 |
|--------|----------|------|---------|
| `initializeConference()` | ~5400 | 从 URL 参数获取 `conferenceId`，调用 `conferenceContext.loadConferenceData()` 加载会议信息 | 通过 conference-context.js 间接调用 `GET /api/meeting/{id}` |
| `loadSeatingData()` | ~5470 | 先尝试从后端数据库加载完整座位布局，失败后回退到 localStorage | `GET /api/seating/layout/load?conferenceId=...&scheduleId=...` |
| `loadSeatingDataFromLocalStorage()` | ~6000 | 从 localStorage 读取 `seating_{confId}` 或 `schedule_seating_{confId}_{schedId}`，恢复区域/参会人员/辅助安排 | 无 |
| `initializeDefaultData()` | ~6080 | 创建默认参会人员（3组共14人模拟数据） | 无 |
| `initializeDefaultAttendees()` | ~6130 | 从 registration API 加载，失败时调用 `initializeDefaultData()` | `GET /api/registration/list?conferenceId=...&page=1&pageSize=1000` |
| `reloadAttendeesFromRegistration()` | ~6200 | 重新从报名系统拉取参会人员，合并已有座位分配 | `GET /api/registration/list?conferenceId=...&page=1&pageSize=1000` |

### 1.2 数据加载流程

```
onMounted
  ├── initializeConference()         → 加载会议基本信息
  ├── loadSchedules()                → 加载日程列表
  └── loadSeatingData()              → 从后端加载座位布局
       ├── 成功 → 恢复 seatingAreas + attendees
       └── 失败 → loadSeatingDataFromLocalStorage()
            ├── 有缓存 → 恢复数据
            └── 无缓存 → initializeDefaultAttendees()
                 ├── API成功 → 使用注册数据
                 └── API失败 → initializeDefaultData() (模拟数据)
```

### 1.3 数据声明 (ref/reactive) — 约 5242–5400

| 变量名 | 类型 | 用途 |
|--------|------|------|
| `loading` | ref(true) | 加载状态 |
| `conference` | ref({}) | 会议信息 |
| `seatingAreas` | ref([]) | 座位区域数组（核心数据） |
| `attendees` | ref([]) | 参会人员数组 |
| `selectedSeats` | ref([]) | 当前选中的座位集合 |
| `currentScheduleId` | ref(null) | 当前日程ID |
| `schedulesList` | ref([]) | 日程列表 |
| `seatingModeSelection` | reactive | 排座模式选择状态 |
| `importModalState` | reactive | 导入弹窗状态 |
| `searchKeyword` | ref('') | 搜索关键词 |
| `editMode` | ref(false) | 编辑模式开关 |
| `seatDetailPanel` | reactive | 座位详情面板 |
| `assignMode` | reactive | 指定分配模式 |
| `attendeeFilterKeyword` | ref('') | 人员筛选关键词 |
| `statistics` | reactive | 统计信息（总座位/已分配/VIP/可用） |
| `areaConfig` | reactive | 区域配置弹窗数据（含完整字段约40个） |
| `layoutConfigs` | reactive | 多块布局配置 |
| `currentLayoutMode` | ref('auto') | 当前布局模式 |
| `seatChangeHistory` | ref([]) | 变更历史记录 |
| `notificationStatus` | ref(null) | 通知发送状态 |
| `contextMenu` | reactive | 右键菜单状态 |
| `transportList` | ref([]) | 乘车安排列表 |
| `accommodationList` | ref([]) | 住宿安排列表 |
| `discussionList` | ref([]) | 讨论室安排列表 |
| `diningList` | ref([]) | 用餐安排列表 |
| `airportTransferList` | ref([]) | 接送机安排列表 |
| `newTransport/newAccommodation/newDiscussion` | reactive | 新增/编辑表单对象 |
| 各种 `show*ModalFlag` | ref(false) | 弹窗显示开关（共约15个） |

---

## 二、座位区域管理（CRUD）

### 2.1 方法清单

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `createSeatingArea(config)` | ~6260 | 根据配置创建座位区域（支持 normal/ushape/round 三种类型），生成行列、座位编号、过道 |
| `addArea()` | ~8410 | 打开区域配置弹窗（新增模式），预设默认值 |
| `editArea(area)` | ~8450 | 打开区域配置弹窗（编辑模式），加载已有区域配置 |
| `deleteArea(areaId)` | ~8520 | 确认后删除指定区域，更新统计，保存数据 |
| `saveAreaConfig()` | ~9360 | 保存区域配置：调用 `createSeatingArea()` 生成新区域或替换已有区域，支持 Grid 布局定位 |
| `closeAreaConfigModal()` | 模板内 | 关闭弹窗 |
| `adjustRows(delta)` | ~8550 | 调整区域行数（±1），限制范围 1-50 |
| `adjustCols(delta)` | ~8570 | 调整区域列数（±1），限制范围 1-50 |

### 2.2 区域配置参数（areaConfig reactive 对象）

| 字段 | 说明 |
|------|------|
| `name` | 区域名称 |
| `type` | 区域类型：regular/vip |
| `venueType` | 会场类型：normal(标准)/ushape(U型)/round(圆桌) |
| `rows/cols` | 行列数 |
| `seatWidth/seatHeight` | 座位尺寸 (px) |
| `rowSpacing/colSpacing` | 间距 (px) |
| `angle` | 旋转角度 (°) |
| `numberingMode` | 编号模式：leftToRight/rightToLeft/centerOut/oddEven/snakePattern |
| `startNumber` | 起始编号 |
| `fixedSeats` | 固定座位文本（格式："排号-列号:姓名"） |
| `gridRow/gridCol/gridSpanRow/gridSpanCol` | Grid 定位参数 |
| `aisleMode` | 过道模式：none/left/right/center/double/triple/cross/custom |
| `aisleConfig` | 过道配置对象（columns/rows 数组） |

### 2.3 createSeatingArea 座位区生成逻辑

- **普通 (normal)**: 标准行列矩阵，支持5种编号模式、过道配置、固定座位
- **U型 (ushape)**: 生成3组行（左侧/底部/右侧），模拟U型会场
- **圆桌 (round)**: 使用三角函数 `Math.cos/sin` 计算圆周座位坐标

---

## 三、座位操作（点击、拖拽、分配）

### 3.1 座位点击

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `handleSeatClick(area, row, seat)` | ~6800 | 核心点击处理：①指定分配模式下分配座位 ②Ctrl+Click 多选 ③普通单击显示详情面板 ④移动/交换座位 |
| `getSeatClass(seat)` | ~6700 | 返回座位 CSS 类名（available/occupied/vip/selected/aisle/highlight） |
| `getSeatTooltip(seat)` | ~6740 | 返回座位悬浮提示文本 |

### 3.2 指定分配模式

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `startAssignMode(attendee)` | 模板内 | 开始分配模式 — 选定参会者后点击座位分配 |
| `cancelAssignMode()` | 模板内 | 取消分配模式 |
| `executeAssignToSeat(seat)` | 模板内 | 执行分配：将选定参会者放入点击的座位 |

### 3.3 拖拽操作

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `handleDragStart(seat)` | 模板内 | 记录被拖拽的座位 |
| `handleDragOver(seat, event)` | 模板内 | 允许放置（`preventDefault`），添加视觉反馈 |
| `handleDragLeave(seat)` | 模板内 | 移除视觉反馈 |
| `handleDrop(targetSeat)` | 模板内 | 执行座位交换/移动 |

### 3.4 座位详情面板

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `openSeatDetailPanel(seat)` | 模板内 | 打开右侧座位详情面板，显示座位/人员信息 |
| `closeSeatDetailPanel()` | 模板内 | 关闭面板 |
| `saveSeatDetails()` | 模板内 | 保存座位详情修改 |
| `getSeatAreaName(seat)` | 模板内 | 获取座位所属区域名 |
| `getUnassignedAttendees()` | 模板内 | 获取未分配座位的参会者列表 |
| `quickClearSeat(seat)` | 模板内 | 快速清空座位 |
| `quickToggleVip(seat)` | 模板内 | 快速切换VIP状态 |

### 3.5 批量操作

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `batchSwapSeats()` | ~7310 | 批量交换两个选中座位的占用者 |
| `batchClearSeats()` | ~7360 | 清空所有选中座位 |
| `clearSelection()` | ~7350 | 取消所有选中 |
| `fillRemainingSeats()` | ~7380 | 将未分配参会者自动填充到空余座位 |
| `insertSeat()` | ~7430 | 在选中座位后插入新座位 |
| `removeSeat()` | ~7470 | 删除选中座位 |
| `clearAllSeats()` | ~9870 | 确认后清空所有座位分配（调用 `clearAllSeatsInternal()` + `saveData()` + `updateStatistics()`） |
| `clearAllSeatsInternal()` | ~9880 | 内部方法：遍历所有区域/行/座位，重置为 available |

### 3.6 右键上下文菜单

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showSeatContextMenu(event, area, row, seat)` | ~8640 | 在座位上右键显示上下文菜单（坐标计算，菜单项动态生成） |
| `hideContextMenu()` | ~8680 | 隐藏上下文菜单 |

---

## 四、自动排座算法

### 4.1 入口与选择

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `executeAutoArrange()` | ~9520 | 排座主入口：确认后清空现有分配，根据 `selectedAlgorithm` 调用对应算法 |
| `getAlgorithmName(key)` | 模板内 | 返回算法中文名称 |

**算法选项** (`selectedAlgorithm`):
- `smart` — 智能排座
- `department` — 按部门+职级排座
- `position` — 按职级排座
- `zone` — 按区域排座

### 4.2 算法实现

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `executeSmartAlgorithm(attendees)` | ~9560 | 智能排座：①VIP优先前排 ②按部门分组+职级排序 ③自动分配到各区域 |
| `executeDepartmentAlgorithm(attendees)` | ~9580 | 按部门排座：按部门分组→按人数排序→均匀分配到各区域 |
| `executePositionAlgorithm(attendees)` | ~9590 | 按职级排座：全员按职级排序→从前到后依次分配 |
| `executeZoneAlgorithm(attendees)` | ~9600 | 按区域排座：VIP分到VIP区→其余人平均分配各区 |
| `assignByDepartmentWithPosition(attendees)` | ~9600 | 部门+职级组合排座：部门分组→部门内按职级排序→按区域分配 |
| `assignByDepartmentOnly(attendees)` | ~9640 | 仅按部门排座：部门分组→按人数排序→依次分配 |
| `assignByPosition(attendees)` | ~9670 | 仅按职级排座：全员按职级降序→顺序分配 |
| `getPositionLevel(position)` | ~9690 | 获取职级等级数值（书记/市长=100 → 职员=5） |
| `assignAttendeesToArea(attendees, area)` | ~9740 | 将参会者列表分配到指定区域的空闲座位 |
| `assignVipSeats(vipAttendees)` | ~9780 | 分配VIP座位：优先VIP区→溢出到普通区 |
| `assignRegularSeats(attendees)` | ~9830 | 分配普通座位：遍历所有区域的空闲座位 |

### 4.3 职级等级表 (`getPositionLevel`)

| 职级关键词 | 等级值 |
|-----------|-------|
| 书记/市长/主席 | 100 |
| 副主席/副主任/副部长 | 90 |
| 秘书长 | 85 |
| 副秘书长 | 80 |
| 局长/部长 | 75 |
| 处长 | 60 |
| 副处长 | 50 |
| 科长/主任 | 40 |
| 经理 | 35 |
| 副科长/副主任 | 30 |
| 主管 | 25 |
| 专员 | 10 |
| 职员 | 5 |
| 无匹配 | 0 |

> ⚠️ **BUG**: `getPositionLevel` 中 `'部长': 75` 和 `'部长': 60` 存在重复key，后者覆盖前者；`'副部长': 90` 和 `'副部长': 50` 同理。JavaScript 对象不允许重复 key，实际生效的是最后一个。

---

## 五、过道配置

### 5.1 方法清单

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `onAisleModeChange(mode)` | ~9050 | 过道模式切换处理，根据 mode 生成对应的 aisleConfig（支持8种模式） |
| `togglePreviewSeatAisle(seat)` | ~9120 | 切换预览区座位的过道状态（自定义模式下） |
| `isColumnAisle(colIndex)` | ~9140 | 判断指定列是否为过道 |
| `isRowAisle(rowIndex)` | ~9160 | 判断指定行是否为过道 |
| `toggleColumnAisle(colIndex)` | ~9170 | 切换列过道状态 |
| `toggleRowAisle(rowIndex)` | ~9190 | 切换行过道状态 |
| `getAisleModeDescription()` | ~9210 | 获取当前过道模式的中文描述 |
| `addVerticalAisle()` | ~9230 | 添加一条纵向过道 |
| `removeVerticalAisle()` | ~9240 | 移除最后一条纵向过道 |
| `addHorizontalAisle()` | ~9250 | 添加一条横向过道 |
| `removeHorizontalAisle()` | ~9260 | 移除最后一条横向过道 |
| `getAislePreviewText()` | ~9270 | 获取过道预览描述文本 |
| `insertRowAisle(area, afterRowIndex)` | ~8700 | 在指定行后插入横向过道（区域级别操作） |
| `insertColumnAisle(area, afterColIndex)` | ~8750 | 在指定列后插入纵向过道 |
| `deleteAisle(area, aisleIndex, type)` | ~9010 | 删除指定过道 |
| `getColumnAisleStyle(area, colIndex)` | ~6780 | 获取列过道 CSS 样式 |

### 5.2 过道模式 (aisleMode)

| 模式 | 说明 |
|------|------|
| `none` | 无过道 |
| `left` | 左侧过道 |
| `right` | 右侧过道 |
| `center` | 中间过道（居中一条） |
| `double` | 左右两侧各一条过道 |
| `triple` | 三条过道（1/4, 1/2, 3/4 处） |
| `cross` | 十字过道（水平+垂直交叉） |
| `custom` | 自定义过道（手动选择列/行） |

---

## 六、布局管理

### 6.1 会场布局选择

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `selectLayout(type)` | ~7800 | 选择会场布局类型（标准/U型/圆桌） |
| `createInitialLayout(type)` | ~7850 | 创建初始布局（清空已有区域后生成默认区域） |

**布局类型** (`layoutTypes`):
| 类型 | 名称 | 默认配置 |
|------|------|---------|
| `normal` | 标准会场 | 8行×12列 |
| `ushape` | U型会场 | U型三段 |
| `round` | 圆桌会场 | 圆周排列 |

### 6.2 多块座位区布局（Grid 系统）

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `switchLayout(mode)` | 模板内 | 切换布局模式（auto/manual） |
| `autoLayoutAreas()` | 模板内 | 自动计算所有区域的 Grid 位置 |
| `calculateAreaGridSize(area)` | 模板内 | 计算单个区域的 Grid 跨度 |
| `getAreaGridStyle(area, index)` | 模板内 | 返回区域的 CSS Grid 样式对象 |
| `applyLayoutConfig()` | 模板内 | 应用预设布局配置 |
| `assignGridPosition(area, row, col, spanRow, spanCol)` | 模板内 | 手动分配 Grid 定位 |
| `getAutoGridSize()` | 模板内 | 获取自动 Grid 网格尺寸 |
| `resetAreaGridPosition(areaId)` | ~8600 | 重置指定区域的 Grid 定位为自动 |
| `applySimpleGridPosition(index, gridRow, gridCol)` | ~8620 | 简单设置区域 Grid 位置 |

---

## 七、搜索与编辑模式

### 7.1 搜索功能

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `handleSearchInput()` | 模板内 | 搜索输入处理：按姓名/部门/座位号匹配 |
| `locateFirstResult()` | 模板内 | 定位到第一个搜索结果 |
| `locateSeat(seat)` | 模板内 | 滚动并高亮定位到指定座位 |
| `highlightSeat(seatId)` | 模板内 | 临时高亮座位（闪烁动画） |

### 7.2 编辑模式

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `toggleEditMode()` | 模板内 | 切换编辑模式（启用/禁用拖拽等高级操作） |
| `toggleShortcutsPanel()` | 模板内 | 切换快捷键面板显示 |

### 7.3 参会人员筛选

| 变量/计算属性 | 类型 | 功能 |
|--------------|------|------|
| `attendeeFilterKeyword` | ref | 筛选关键词 |
| `attendeeFilterType` | ref | 筛选类型（all/unassigned/assigned/vip） |
| `filteredAttendeeList` | computed | 根据关键词+类型双重过滤的参会人员列表 |
| `isAttendeeSeated(attendee)` | 方法 | 判断参会者是否已有座位 |

### 7.4 键盘快捷键

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `handleKeyboardShortcuts(event)` | ~7340 | 全局键盘事件处理（Delete删除选中、Escape取消选择等） |

**快捷键列表**（由 `seating-optimization-patch.js` 的 `KeyboardShortcuts` 类注册）:
| 快捷键 | 功能 |
|--------|------|
| `Ctrl+Z` | 撤销 |
| `Ctrl+Y` | 重做 |
| `Ctrl+S` | 保存 |
| `Delete` | 删除选中座位内容 |
| `Escape` | 取消选择 |

---

## 八、辅助安排管理

### 8.1 乘车安排

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showTransportModal()` | ~10330 | 打开新增车辆弹窗 |
| `closeTransportModal()` | ~10350 | 关闭车辆弹窗 |
| `saveTransport()` | ~10370 | 保存车辆（支持新增/编辑） |
| `autoAssignTransport()` | ~10570 | 智能分配乘车：按部门分组→按剩余容量分配 |
| `showTransportListModal()` | ~10800 | 打开车辆列表管理弹窗 |
| `closeTransportListModal()` | ~10804 | 关闭列表弹窗 |
| `editTransport(vehicle)` | ~10808 | 编辑车辆信息 |
| `deleteTransport(id)` | ~10830 | 删除车辆（清除人员分配） |
| `removePassenger(vehicleId, passengerId)` | ~10850 | 从车辆中移除乘客 |
| `assignPassengers(vehicle)` | ~11070 | 打开人员分配弹窗 |

**车辆类型容量映射**: 小车=4, 中巴=15, 大巴=45

### 8.2 住宿安排

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showAccommodationModal()` | ~10430 | 打开新增房间弹窗 |
| `closeAccommodationModal()` | ~10450 | 关闭房间弹窗 |
| `saveAccommodation()` | ~10470 | 保存房间（支持新增/编辑） |
| `autoAssignAccommodation()` | ~10630 | 智能分配住宿：按性别分房（同性同室），单人间/标准间处理 |
| `showAccommodationListModal()` | ~10870 | 打开房间列表弹窗 |
| `closeAccommodationListModal()` | ~10874 | 关闭列表弹窗 |
| `editAccommodation(room)` | ~10878 | 编辑房间信息 |
| `deleteAccommodation(id)` | ~10900 | 删除房间 |
| `removeOccupant(roomId, occupantId)` | ~10920 | 从房间中移除住客 |
| `assignOccupants(room)` | ~11080 | 打开人员分配弹窗 |

**住宿自动分配逻辑**:
- 按性别分为 男/女/未知 三组
- 单人间：优先分配未知性别，再分男/女
- 标准间(2人)：同性别优先配对
- 多人间：使用未知性别人员填充

### 8.3 讨论室安排

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showDiscussionModal()` | ~10510 | 打开新增讨论室弹窗 |
| `closeDiscussionModal()` | ~10530 | 关闭弹窗 |
| `saveDiscussion()` | ~10540 | 保存讨论室 |
| `autoAssignDiscussions()` | ~10690 | 智能分配讨论室：按部门分组→整组分配到一个讨论室→散客单独分配 |
| `showDiscussionListModal()` | ~10940 | 打开列表弹窗 |
| `closeDiscussionListModal()` | ~10944 | 关闭列表弹窗 |
| `editDiscussion(discussion)` | ~10948 | 编辑讨论室 |
| `deleteDiscussion(id)` | ~10970 | 删除讨论室 |
| `assignDiscussionGroups(discussion)` | ~11090 | 讨论室分组分配（占位，显示alert提示） |

### 8.4 用餐安排

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showDiningModal()` | ~10560 | 打开新增用餐弹窗 |
| `closeDiningModal()` | ~10565 | 关闭弹窗 |
| `saveDining()` | ~10570 | 保存用餐安排 |
| `autoAssignDining()` | ~10580 | 自动平均分配参会者到各桌 |
| `showDiningListModal()` | 占位 | 管理功能开发中（alert提示） |

### 8.5 接送机安排

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `showAirportTransferModal()` | ~10600 | 打开接送机弹窗 |
| `closeAirportTransferModal()` | ~10610 | 关闭弹窗 |
| `saveAirportTransfer()` | ~10620 | 保存接送机安排 |
| `autoAssignAirportTransfer()` | ~10640 | 按航班号自动分配（匹配参会者的 flightNumber） |
| `showAirportTransferListModal()` | 占位 | 管理功能开发中（alert提示） |

### 8.6 统一操作

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `autoAssignAllArrangements()` | ~10660 | 一键智能分配所有辅助安排（乘车+住宿+讨论+用餐+接送机） |
| `viewArrangements()` | ~10750 | 打开分配结果查看弹窗 |
| `closeArrangementResultModal()` | ~10990 | 关闭结果弹窗 |
| `exportArrangementResults()` | ~11000 | 导出分配结果为 TXT 文件下载 |

### 8.7 通用人员分配弹窗

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `closePersonAssignModal()` | ~11100 | 关闭人员分配弹窗 |
| `addPersonToArrangement(attendee)` | ~11110 | 添加人员到当前安排项（乘车/住宿） |
| `removePersonFromArrangement(personId)` | ~11170 | 从当前安排项移除人员 |
| `unassignedAttendees` | computed ~11230 | 计算未分配到当前类型安排的人员列表 |

---

## 九、导入/导出

### 9.1 参会人员导入

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `openImportModal()` | 模板内 | 打开导入弹窗 |
| `closeImportModal()` | 模板内 | 关闭导入弹窗 |
| `executeImport()` | 模板内 | 执行导入操作 |
| `handleExcelImport(event)` | ~9440 | Excel导入入口：使用 `ExcelDataValidator` 验证后路由到对应处理函数 |
| `importAttendeeListFromExcel(data)` | ~9470 | 从Excel导入参会人员列表（姓名/部门/职位/电话映射） |
| `importSeatingFromExcel(data)` | ~9490 | 从Excel导入座位分配表（区域/排号/座位号/姓名映射） |
| `assignSeatByPosition(areaName, rowNum, seatNum, attendeeName)` | ~9510 | 按位置分配座位（配合Excel导入使用） |

### 9.2 导出功能

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `exportSeatingChart()` | ~10060 | 导出入口：prompt 选择格式（Excel/PDF） |
| `exportToExcel()` | ~10080 | 导出为 Excel（使用 XLSX.js）：含区域/排号/座位号/姓名/单位/职位/手机号/类型 + 统计 |
| `exportToPDF()` | ~10190 | 导出为 PDF（使用 jsPDF，按需加载CDN）：含标题/时间/区域/座位数据/统计 |
| `exportChangeHistory()` | ~7650 | 导出变更历史记录 |
| `exportArrangementResults()` | ~11000 | 导出辅助安排分配结果为 TXT |

### 9.3 QR码与LED导示图

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `generateQRCode()` | ~9910 | 生成查座链接（格式：`/app/learner/scan-seat.html?confId=...`），prompt显示+复制到剪贴板 |
| `generateLEDMap()` | ~9960 | 生成 LED 导示图（1920×1080 分辨率），使用 html2canvas 渲染为 PNG 下载 |

---

## 十、通知推送

### 10.1 方法清单

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `sendSeatNotifications()` | ~11480 | 发送座位安排通知（APP推送/短信/微信小程序） |
| `sendTransportNotifications()` | ~11530 | 发送乘车安排通知 |
| `sendAccommodationNotifications()` | ~11590 | 发送住宿安排通知 |
| `sendDiscussionNotifications()` | ~11640 | 发送讨论安排通知 |
| `sendAllArrangementNotifications()` | ~11700 | 一键发送所有类型通知 |
| `sendSeatChangeNotification(change)` | ~7580 | 发送单条座位变更通知（实时） |

### 10.2 通知数据结构

每条通知包含：
```json
{
  "userId": "参会者ID",
  "userName": "姓名",
  "userPhone": "手机号",
  "title": "通知标题",
  "content": "通知正文（含座位/车辆/房间详细信息）",
  "type": "seat|transport|accommodation|discussion",
  "data": { /* 结构化数据 */ }
}
```

> ⚠️ **注意**: 所有通知发送当前为 `console.log` 模拟，标注有 `TODO: 调用真实的APP推送接口`，尚未对接真实推送服务。

### 10.3 通知状态计算属性

| 属性名 | 行号(约) | 功能 |
|--------|----------|------|
| `hasSeatAssignments` | ~11450 | 是否存在座位分配 |
| `hasTransportAssignments` | ~11454 | 是否存在乘车分配 |
| `hasAccommodationAssignments` | ~11458 | 是否存在住宿分配 |
| `hasDiscussionAssignments` | ~11462 | 是否存在讨论室分配 |
| `hasAnyAssignments` | ~11466 | 是否存在任何类型的分配 |

---

## 十一、变更历史与撤销/重做

### 11.1 页面内方法

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `recordSeatChange(change)` | ~7530 | 记录座位变更到 `seatChangeHistory`，限制最多50条 |
| `sendSeatChangeNotification(change)` | ~7580 | 变更时发送实时通知 |
| `viewChangeHistory()` | ~7600 | 弹窗显示变更历史 |
| `undoLastChange()` | ~7620 | 撤销最后一次变更（从历史栈弹出并恢复） |
| `exportChangeHistory()` | ~7650 | 导出变更历史 |

### 11.2 seating-optimization-patch.js 中的 UndoRedoManager

| 方法 | 功能 |
|------|------|
| `constructor(options)` | 接收 `seatingAreas` 和 `attendees` 引用 |
| `saveState()` | 保存当前状态快照 |
| `undo()` | 恢复到上一个状态 |
| `redo()` | 前进到下一个状态 |
| `canUndo()` | 检查是否可以撤销 |
| `canRedo()` | 检查是否可以重做 |

---

## 十二、日程管理（多日程排座）

### 12.1 方法清单

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `loadSchedules()` | 模板/模块内 | 加载会议的所有日程列表 |
| `selectSchedule(scheduleId)` | 模板/模块内 | 选择日程（触发确认弹窗） |
| `confirmScheduleChange()` | 模板/模块内 | 确认切换日程（保存当前→加载新日程） |
| `openScheduleSelector()` | 模板/模块内 | 打开日程选择器弹窗 |
| `closeScheduleSelector()` | 模板/模块内 | 关闭日程选择器 |
| `saveScheduleSeatingResult()` | 模板/模块内 | 保存当前日程的排座结果 |
| `loadScheduleSeatingData()` | 模板/模块内 | 加载指定日程的排座数据 |

> 注：日程管理通过 `useScheduleManager` 组合函数提供，来自 `seating-schedule-manager.js` 模块。

### 12.2 多日程数据存储

每个日程的排座数据独立存储：
- localStorage key: `schedule_seating_{confId}_{schedId}`
- 后端 API: 通过 `scheduleId` 参数区分

---

## 十三、数据持久化与 API

### 13.1 保存方法

| 方法名 | 行号(约) | 功能 |
|--------|----------|------|
| `saveData()` | ~11250 | 核心保存函数：同时保存到 localStorage（会议级+日程级）和后端数据库 |
| `saveLayoutToBackend(confId, schedId)` | ~11290 | 异步将布局同步到后端（不阻塞UI） |

### 13.2 saveData 保存逻辑

```
saveData()
  ├── localStorage.setItem(`seating_{confId}`, data)      // 会议级缓存
  ├── localStorage.setItem(`schedule_seating_{confId}_{schedId}`, data)  // 日程级缓存（如有）
  └── saveLayoutToBackend()                                 // 异步同步到后端
```

### 13.3 完整 API 端点清单

| 方法 | 端点 | 用途 | 调用位置 |
|------|------|------|---------|
| GET | `/api/meeting/{conferenceId}` | 加载会议信息 | conference-context.js |
| GET | `/api/registration/list?conferenceId=...&page=1&pageSize=1000` | 加载已审批参会人员 | initializeDefaultAttendees(), reloadAttendeesFromRegistration() |
| GET | `/api/registration/stats?conferenceId=...` | 报名统计 | conference-context.js |
| GET | `/api/registration/qr/generate?conferenceId=...&registrationUrl=...` | 报名二维码 | conference-context.js |
| GET | `/api/seating/layout/load?conferenceId=...&scheduleId=...` | 从数据库加载完整座位布局 | loadSeatingData() |
| POST | `/api/seating/layout/save` | 保存座位布局到数据库 | saveLayoutToBackend() |

### 13.4 后端保存请求格式 (POST `/api/seating/layout/save`)

```json
{
  "conferenceId": "会议ID",
  "scheduleId": "日程ID (可选)",
  "areas": [{
    "areaId": "区域ID",
    "name": "区域名称",
    "type": "regular|vip",
    "venueType": "normal|ushape|round",
    "rows": 8,
    "cols": 12,
    "angle": 0,
    "numberingMode": "leftToRight",
    "startNumber": 1,
    "layoutJson": "完整区域JSON字符串",
    "seats": [{
      "seatId": "座位ID",
      "row": 1,
      "col": 1,
      "seatNumber": "1-1",
      "seatType": "normal|vip",
      "status": "available|occupied",
      "occupantId": "参会者ID",
      "occupantName": "姓名"
    }]
  }],
  "attendees": [{
    "attendeeId": "ID",
    "name": "姓名",
    "department": "部门",
    "position": "职位",
    "company": "单位",
    "isVip": false,
    "phone": "手机号"
  }]
}
```

### 13.5 认证头

所有 API 请求携带：
- `Authorization: Bearer {authToken}` （从 localStorage 读取）
- `X-Tenant-Id: {tenantId}` （多租户支持）

---

## 十四、生命周期与初始化

### 14.1 onMounted 流程 (~11340)

```
onMounted(async () => {
  1. await initializeConference()       // 加载会议信息
  2. await loadSchedules()              // 加载日程列表
  3. await loadSeatingData()            // 加载座位布局（DB → localStorage → 默认）
  4. nextTick → setTimeout(100ms)       // 强制刷新过道位置
  5. setTimeout(100ms) → initializeOptimizations()  // 初始化优化功能
  6. 加载"不再提示"配置
  7. 初始化 UndoRedoManager
  8. 初始化 KeyboardShortcuts
  9. 注册全局 keydown 监听
  10. 注册全局 click 监听（点击外部关闭搜索）
})
```

> ⚠️ **BUG/冗余**: `initializeOptimizations()` 在 `onMounted` 中被调用了**两次**：
> 1. 第一次通过 `setTimeout` 调用 `initializeOptimizations()` 函数（~11355行）
> 2. 第二次在 try-catch 中重复了完全相同的初始化逻辑（~11365-11435行）
> 
> 这导致 `UndoRedoManager` 和 `KeyboardShortcuts` 被实例化两次，可能造成快捷键事件重复触发。

### 14.2 initializeOptimizations() (~11290)

1. 恢复 `seating-confirmed-actions`（"不再提示"配置）
2. 实例化 `UndoRedoManager`（传入 seatingAreas/attendees 引用）
3. 实例化 `KeyboardShortcuts`（传入 undo/redo/save/selectedSeats/clearSelection）
4. console.log 快捷键提示

---

## 十五、seating-optimization-patch.js 功能清单（872行）

### 15.1 类与组件

| 类名 | 行号 | 功能 |
|------|------|------|
| `ToastNotification` | ~20 | 轻量级 Toast 通知组件（自动创建DOM容器，支持 info/success/warning/error 4种类型） |
| `ProgressIndicator` | ~100 | 进度条指示器（全屏遮罩+百分比+文本） |
| `ExcelDataValidator` | ~180 | Excel数据验证器（检查空行、必填字段、手机号格式、部门名验证） |
| `KeyboardShortcuts` | ~350 | 键盘快捷键管理器（Ctrl+Z/Y/S、Delete、Escape） |
| `BatchOperations` | ~450 | 批量操作管理器（批量分配/清除/移动，带进度条反馈） |
| `SaveManager` | ~550 | 保存管理器（自动重试机制，最多3次，指数退避） |
| `UndoRedoManager` | ~650 | 撤销/重做管理器（状态快照栈，最多50个历史状态） |

### 15.2 独立函数

| 函数名 | 功能 |
|--------|------|
| `showConfirm(options)` | 增强确认对话框（支持"不再提示"复选框，配置持久化到 localStorage） |

### 15.3 全局导出

```javascript
window.SeatingOptimization = {
    ToastNotification, ProgressIndicator, ExcelDataValidator,
    KeyboardShortcuts, BatchOperations, SaveManager, UndoRedoManager,
    showConfirm, config: SeatingOptimizationConfig
};
window.toast = new ToastNotification();
window.progress = new ProgressIndicator();
```

---

## 十六、conference-context.js 功能清单（643行）

### 16.1 ConferenceContextManager 类

| 方法 | 功能 |
|------|------|
| `constructor()` | 初始化状态、绑定事件 |
| `loadConferenceData(conferenceId)` | 加载会议数据（`GET /api/meeting/{id}`） |
| `loadRegistrationStats()` | 加载报名统计（`GET /api/registration/stats`） |
| `generateQRCode()` | 生成报名二维码（`GET /api/registration/qr/generate`） |
| `getConferenceId()` | 从 URL 参数获取会议ID |
| `updateUI()` | 更新页面上的会议信息显示 |
| `registerDataSource(name, loader)` | 注册数据源（支持 registration/tasks/groups/navigation/ai/seating/notifications/attendance/data） |
| `setupRealTimeConnection()` | 设置实时连接（占位） |

### 16.2 全局实例

```javascript
window.conferenceContext = new ConferenceContextManager();
```

---

## 十七、HTML 模板关键 UI 组件

### 17.1 导航区域 (~3431)
- 顶部导航栏（返回按钮、会议名称、日程选择器）

### 17.2 弹窗/模态框清单

| 弹窗 | 触发 | 功能 |
|------|------|------|
| 日程选择弹窗 | 点击日程按钮 | 选择/切换日程 |
| 日程切换确认弹窗 | 切换日程时 | 确认是否保存当前数据 |
| 排座模式选择弹窗 | 点击排座按钮 | 选择自动/引导/高级排座模式 |
| 区域配置弹窗 | 添加/编辑区域 | 完整区域参数配置（含过道、编号、预览） |
| 乘车安排弹窗 | 添加/编辑车辆 | 车辆信息表单 |
| 住宿安排弹窗 | 添加/编辑房间 | 房间信息表单 |
| 讨论室安排弹窗 | 添加/编辑讨论室 | 讨论室信息表单 |
| 用餐安排弹窗 | 添加用餐安排 | 用餐信息表单 |
| 接送机安排弹窗 | 添加接送机 | 接送机信息表单 |
| 车辆列表管理弹窗 | 查看所有车辆 | 列表+编辑/删除 |
| 房间列表管理弹窗 | 查看所有房间 | 列表+编辑/删除 |
| 讨论室列表管理弹窗 | 查看所有讨论室 | 列表+编辑/删除 |
| 分配结果查看弹窗 | 查看分配结果 | 乘车+住宿+讨论室汇总 |
| 人员分配弹窗 | 手动分配人员 | 未分配列表→添加到安排项 |
| 座位详情面板 | 点击座位 | 右侧滑入面板（座位/人员信息） |
| 导入弹窗 | 导入按钮 | Excel文件上传 |

### 17.3 主内容区域结构

```
#app
├── nav.top-nav                        // 导航栏
├── .schedule-selector                 // 日程选择器
├── 各种 Modal                          // 弹窗集合
├── .page-header                       // 页头（会议信息+操作按钮）
├── .layout-selection                  // 布局选择（标准/U型/圆桌）
├── .seat-map-container                // 座位图主区域
│   ├── .seat-areas                    // 座位区域容器（Grid布局）
│   │   └── .seat-area (v-for)         // 每个座位区
│   │       ├── .area-header           // 区域头（名称+操作按钮）
│   │       └── .seat-rows             // 座位行列
│   │           └── .seat-row (v-for)  // 每行
│   │               └── .seat (v-for)  // 每个座位
│   ├── .seat-search-toolbar           // 搜索工具栏
│   └── .batch-operations-toolbar      // 批量操作工具栏
├── .seat-detail-panel                 // 座位详情面板
├── .shortcuts-panel                   // 快捷键面板
├── .assign-mode-bar                   // 分配模式提示栏
├── .hint-bar                          // 提示信息栏
├── .edit-mode-indicator               // 编辑模式指示器
├── .bottom-panels                     // 底部面板集合
│   ├── 统计面板                        // 总座位/已分配/VIP/可用
│   ├── 参会人员面板                     // 人员列表+筛选
│   ├── 快速操作面板                     // 自动排座/清空/导出等
│   ├── 辅助安排面板                     // 乘车/住宿/讨论/用餐/接送机
│   ├── 通知面板                        // 各类通知发送按钮
│   └── 变更历史面板                     // 历史记录+撤销
└── .context-menu                      // 右键菜单
```

---

## 十八、已发现的 BUG 和问题

### 🔴 严重

| # | 问题 | 位置 | 说明 |
|---|------|------|------|
| 1 | `getPositionLevel` 对象重复 key | ~9690 | `'部长': 75` 和 `'部长': 60` 重复，`'副部长': 90` 和 `'副部长': 50` 重复，`'副主任': 90` 和 `'副主任': 30` 重复。后者覆盖前者，导致职级判定不准确 |
| 2 | `onMounted` 重复初始化 | ~11340-11435 | `initializeOptimizations()` 和内联代码重复执行相同逻辑，`KeyboardShortcuts` 和 `UndoRedoManager` 被实例化两次 |

### 🟡 中等

| # | 问题 | 位置 | 说明 |
|---|------|------|------|
| 3 | 通知功能未实现 | ~11480+ | 所有 `send*Notifications` 方法仅 `console.log`，标注 TODO |
| 4 | 用餐/接送机列表管理未完成 | ~10560 | `showDiningListModal()` 和 `showAirportTransferListModal()` 只有 `alert('开发中')` |
| 5 | 重复 `console.error` | 多处 | `generateLEDMap`、`exportToExcel`、`exportToPDF` 中 `console.error` 被调用了两次 |
| 6 | exportToExcel 数据格式混乱 | ~10080 | 表头使用了数组格式 `['区域', ...]`，但数据行使用了对象格式 `{区域: '...'}` — XLSX.js 的 `aoa_to_sheet` 需要纯数组 |
| 7 | QR码生成简陋 | ~9910 | 使用 `prompt()` 显示链接，未实际生成可视化二维码图片 |

### 🟢 轻微

| # | 问题 | 位置 | 说明 |
|---|------|------|------|
| 8 | `assignDiscussionGroups` 占位实现 | ~11090 | 仅 `alert` 提示，无实际逻辑 |
| 9 | `debugAreaConfig` 调试方法暴露到生产 | ~return | 调试函数被暴露在 return 中 |
| 10 | html2canvas/jsPDF CDN 加载无错误处理 | ~9960, ~10200 | `setTimeout(resolve, 3000)` 可能在脚本未加载完成时就 resolve |

---

## 十九、统计汇总

### 方法数量统计

| 类别 | 数量 |
|------|------|
| 会议初始化与数据加载 | 6 |
| 座位区域 CRUD | 8 |
| 座位操作（点击/拖拽/分配） | 18 |
| 自动排座算法 | 11 |
| 过道配置 | 16 |
| 布局管理 | 10 |
| 搜索与编辑模式 | 9 |
| 辅助安排管理 | 35+ |
| 导入/导出 | 9 |
| 通知推送 | 6 |
| 变更历史与撤销 | 5 |
| 日程管理 | 7 |
| 数据持久化 | 3 |
| 工具方法 | 4 |
| **总计** | **~147 个方法** |

### computed 计算属性

| 名称 | 用途 |
|------|------|
| `filteredAttendeeList` | 筛选后的参会人员列表 |
| `vipCount` | VIP人数 |
| `previewRows` | 区域配置预览行 |
| `previewStats` | 区域配置预览统计 |
| `hasSeatAssignments` | 是否有座位分配 |
| `hasTransportAssignments` | 是否有乘车分配 |
| `hasAccommodationAssignments` | 是否有住宿分配 |
| `hasDiscussionAssignments` | 是否有讨论分配 |
| `hasAnyAssignments` | 是否有任何分配 |
| `unassignedAttendees` | 未分配人员列表 |

### return 导出清单

`return {}` 语句导出了约 **180+ 个** 响应式变量和方法到模板，涵盖所有13个功能模块。

---

## 二十、seating-optimization-patch.js 配置参数

```javascript
SeatingOptimizationConfig = {
    toastDuration: 3000,       // Toast 显示时长 (ms)
    maxHistory: 50,            // 撤销/重做最大历史数
    autoSaveInterval: 30000,   // 自动保存间隔 (ms) — 注：代码中声明但未实际启用定时器
    confirmedActions: new Set() // "不再提示"的操作集合
}
```

---

> **审计结论**: `seating-mgr.html` 是一个功能非常密集的单文件 Vue 应用，包含 ~147 个方法、~10 个计算属性、6 个 API 端点、15+ 个弹窗，实现了座位排列、5种辅助安排、4种排座算法、多种导出格式、日程管理、通知推送等完整会务管理功能。核心架构合理但存在部分代码冗余和未完成功能。建议将其拆分为多个 Vue 组件以提升可维护性。
