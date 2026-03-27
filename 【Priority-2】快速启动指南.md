# 🚀 Priority-2页面改造 - 快速启动指南

**状态**: 🟢 准备就绪  
**基础设施**: ✅ 已完成 (Pinia + API + 错误处理)  
**下一步**: 开始改造8个高优先级页面  

---

## 📋 高优先级页面改造清单 (Week 1)

### 优先级排序 (按依赖关系)

| 序号 | 页面 | 目录 | 复杂度 | 预计时间 | 依赖 | 状态 |
|------|------|------|--------|---------|------|------|
| 1 | **tenant-login** | common/tenant-login | 🟢 低 | 4-6h | 无 | ⏳ |
| 2 | **tenant-create** | staff/tenant-create | 🟡 中 | 8-12h | auth | ⏳ |
| 3 | **tenant-manage** | staff/tenant-manage | 🟡 中 | 10-14h | auth | ⏳ |
| 4 | **training** | staff/training | 🟡 中 | 9-13h | training-create | ⏳ |
| 5 | **checkin** | learner/checkin | 🟡 中 | 10-14h | seat-manage | ⏳ |
| 6 | **schedule** | staff/schedule | 🟡 中 | 12-16h | training | ⏳ |
| 7 | **grouping-manage** | staff/grouping-manage | 🟡 中 | 11-15h | registration | ⏳ |
| 8 | **notice-manage** | staff/notice-manage | 🟠 中高 | 14-18h | 无 | ⏳ |

---

## 🎯 第一个页面: tenant-login (4-6小时)

### 目标
快速胜利，为后续页面建立模式。登录前的租户选择页面。

### 实现步骤

#### Step 1: 分析原始HTML (20分钟)
```bash
# 位置: static/legacy/common/tenant-login.html
# 获取UI设计、交互逻辑、表单验证方式
```

#### Step 2: 创建Vue组件骨架 (30分钟)
```bash
# 创建文件: pages/common/tenant-login/tenant-login.vue

# 结构:
<template>
  <view class="tenant-login-container">
    <!-- 租户选择下拉框 -->
    <!-- 记住租户选项 -->
    <!-- 继续按钮 -->
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores'
import { request } from '@/common/request-enhanced'

const authStore = useAuthStore()
const tenantList = ref([])
const selectedTenant = ref(null)
const rememberTenant = ref(false)
</script>

<style scoped lang="scss">
// 样式复用: gradient背景、卡片布局、按钮样式
</style>
```

#### Step 3: 实现业务逻辑 (3小时)
```javascript
// 核心功能:
1. onMounted时获取租户列表
   - 调用GET /api/tenant/list
   - 使用useAuthStore.setTenantId()保存选择

2. 下拉框选择变化
   - 更新selectedTenant
   - 如果rememberTenant,保存到localStorage

3. 继续按钮点击
   - 验证租户已选择
   - 保存选择: authStore.setTenantId(selectedTenant.id)
   - 跳转: uni.navigateTo({ url: '/pages/common/login/login' })

4. 恢复之前的选择
   - 从localStorage读取'selected_tenant_id'
   - 默认选中该租户
```

#### Step 4: 样式设计 (1小时)
```scss
// 复用已有的设计系统:
// - 渐变背景 (来自login.vue)
// - 卡片布局 (来自registration-manage.vue)
// - Element UI Plus el-select组件
// - 按钮和输入框样式

.tenant-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tenant-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}
```

#### Step 5: 测试 (30分钟)
```bash
# 测试清单:
✅ 租户列表能否正确加载
✅ 选择租户后是否能正确保存
✅ 记住租户功能是否工作
✅ 导航到登录页面是否成功
✅ 移动设备响应式是否正常
```

### 关键代码片段

```javascript
// 获取租户列表
const fetchTenantList = async () => {
  try {
    const response = await request('/api/tenant/list', {
      method: 'GET',
      service: 'auth',
      showLoading: true,
    })
    tenantList.value = response || []
  } catch (error) {
    uni.showToast({
      title: '获取租户列表失败',
      icon: 'error',
    })
  }
}

// 处理继续按钮
const handleContinue = () => {
  if (!selectedTenant.value) {
    uni.showToast({
      title: '请选择租户',
      icon: 'error',
    })
    return
  }

  // 保存租户选择
  authStore.setTenantId(selectedTenant.value.id)

  if (rememberTenant.value) {
    uni.setStorageSync('selected_tenant_id', selectedTenant.value.id)
  }

  // 导航到登录
  uni.navigateTo({
    url: '/pages/common/login/login',
  })
}

// 恢复之前的选择
onMounted(() => {
  const savedTenantId = uni.getStorageSync('selected_tenant_id')
  if (savedTenantId) {
    selectedTenant.value = tenantList.value.find(
      (t) => t.id === savedTenantId
    )
  }
})
```

---

## 🎨 设计系统复用

### 已有的可复用资源

#### 1. 样式主题 (uni.scss)
```scss
// 颜色定义
$primary-color: #667eea;
$secondary-color: #764ba2;
$success-color: #42b983;
$error-color: #f56c6c;
$warning-color: #e6a23c;
$info-color: #909399;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 圆角
$border-radius-sm: 4px;
$border-radius-md: 8px;
$border-radius-lg: 12px;

// 阴影
$shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
$shadow-md: 0 4px 12px rgba(0, 0, 0, 0.15);
$shadow-lg: 0 10px 40px rgba(0, 0, 0, 0.1);
```

#### 2. 常用组件 (common/components/)
- FormField.vue - 表单字段包装器
- DataTable.vue - 数据表格
- SearchBar.vue - 搜索栏
- ActionButtons.vue - 操作按钮组
- ConfirmDialog.vue - 确认对话框

#### 3. Pinia Store
- useAuthStore() - 认证状态
- useTrainingStore() - 培训状态
- useMeetingStore() - 会议座位状态
- useTaskStore() - 任务状态
- useUIStore() - UI状态

#### 4. API服务
- authAPI.* - 认证相关API
- trainingAPI.* - 培训API (待创建)
- meetingAPI.* - 会议API (待创建)
- taskAPI.* - 任务API (待创建)

#### 5. 工具函数
- request/get/post/put/delete - HTTP请求
- handleApiError - 错误处理
- showErrorToast / showSuccessToast - 提示显示

---

## 📋 通用改造模板

### 基础页面模板

```vue
<template>
  <view class="page-container">
    <!-- 标题栏 -->
    <view class="header">
      <text class="title">{{ pageTitle }}</text>
    </view>

    <!-- 内容区域 -->
    <view class="content">
      <!-- 页面具体内容 -->
    </view>

    <!-- 加载和错误状态 -->
    <view v-if="isLoading" class="loading-overlay">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, useUIStore } from '@/stores'
import { request, post, put, deleteRequest } from '@/common/request-enhanced'
import { handleApiError } from '@/common/api'

// ==================== 基础状态 ====================
const pageTitle = ref('页面标题')
const isLoading = ref(false)
const authStore = useAuthStore()
const uiStore = useUIStore()

// ==================== 数据状态 ====================
const dataList = ref([])
const currentItem = ref(null)
const filters = ref({})
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
})

// ==================== 计算属性 ====================
const isLoggedIn = computed(() => authStore.isLoggedIn)
const userRole = computed(() => authStore.userRole)

// ==================== 生命周期 ====================
onMounted(() => {
  if (!isLoggedIn.value) {
    uni.redirectTo({ url: '/pages/common/login/login' })
    return
  }
  fetchData()
})

// ==================== 数据获取 ====================
const fetchData = async () => {
  isLoading.value = true
  try {
    // 调用API获取数据
    const response = await request('/api/endpoint', {
      method: 'GET',
      data: { ...filters.value, ...pagination.value },
      service: 'meeting', // 根据需要更改服务
      showLoading: false,
    })

    dataList.value = response.items || []
    pagination.value.total = response.total || 0
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    isLoading.value = false
  }
}

// ==================== 操作方法 ====================
const handleCreate = () => {
  uni.navigateTo({
    url: '/pages/module/page-create/page-create',
  })
}

const handleEdit = (item) => {
  uni.navigateTo({
    url: `/pages/module/page-edit/page-edit?id=${item.id}`,
  })
}

const handleDelete = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除 ${item.name} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteRequest(`/api/endpoint/${item.id}`, null, {
            service: 'meeting',
          })
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchData()
        } catch (error) {
          console.error('Delete failed:', error)
        }
      }
    },
  })
}
</script>

<style scoped lang="scss">
@import '@/uni.scss';

.page-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  padding: $spacing-lg;
  border-bottom: 1px solid #ebedf0;

  .title {
    font-size: 24px;
    font-weight: bold;
    color: #333;
  }
}

.content {
  padding: $spacing-lg;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
</style>
```

---

## ⚡ 快速开发技巧

### 1. 复用已有组件和样式
```javascript
// ✅ 好的做法
import { useAuthStore } from '@/stores' // 复用已有Store
import { request } from '@/common/request-enhanced' // 复用API方法

// ✅ 使用已有的样式变量
<style scoped lang="scss">
@import '@/uni.scss';
// 使用 $primary-color, $spacing-lg 等
</style>
```

### 2. 充分利用Pinia Store
```javascript
// ✅ 好的做法
const trainingStore = useTrainingStore()
const filteredList = computed(() => trainingStore.filteredTrainingList)

// ❌ 避免
const localFilteredList = computed(() => {
  // 重复实现过滤逻辑
})
```

### 3. 统一的错误处理
```javascript
// ✅ 好的做法
try {
  const result = await request('/api/...')
} catch (error) {
  // 错误已自动显示，只需要处理特殊情况
}

// ❌ 避免
.catch(error => {
  uni.showToast({ title: error.message })
})
```

### 4. 使用响应式设计模式
```scss
// ✅ 好的做法
// 使用flex布局和移动优先设计
.card {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;

  @media (min-width: 768px) {
    flex-direction: row;
  }
}
```

---

## 🔍 审查清单 (每个页面)

完成后，验证以下几点:

- ✅ 功能完整性
  - 所有原型功能都已实现
  - 表单验证正确
  - 错误处理妥当

- ✅ 代码质量
  - 使用Composition API (setup)
  - 充分利用Pinia Store
  - 调用统一的API服务
  - 代码注释完善

- ✅ 用户体验
  - 加载状态显示正确
  - 错误提示用户友好
  - 响应式设计正确
  - 触摸交互流畅

- ✅ 性能
  - 首屏加载 < 2秒
  - 列表滚动帧率 >= 50fps
  - API请求有超时和重试

- ✅ 安全
  - Token自动添加
  - 租户ID自动添加
  - 权限检查正确

---

## 📊 预期进度

```
Day 1-2:  tenant-login + tenant-create + tenant-manage (30h)
Day 3-4:  training + checkin (22h)
Day 5-6:  schedule + grouping-manage (26h)
Day 7:    notice-manage + 测试和优化 (14h)

总计: 92小时 = 约11-12个工作天 (按8h/天)

加速模式: 1周完成 (每天12-14小时工作)
```

---

## 🎯 立即行动

**现在开始:**
1. ✅ 分析 tenant-login.html
2. ✅ 创建 pages/common/tenant-login/tenant-login.vue
3. ✅ 实现业务逻辑（4-6小时内）
4. ✅ 测试和优化

**预计完成**: 6小时内发布第一个Priority-2页面 🚀

---

**状态**: 🟢 准备开发  
**预计完成**: 第一周完成8个高优先级页面  
**后续**: 第二周完成12个中优先级页面  
