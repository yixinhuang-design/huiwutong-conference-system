<template>
  <view class="department-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">部门管理</text>
        <text class="header-action" @click="addDepartment">+ 新建</text>
      </view>
    </view>

    <!-- 租户选择 -->
    <view class="tenant-section">
      <picker mode="selector" :range="tenantList" @change="onTenantChange">
        <view class="tenant-selector">
          <text class="selector-label">当前租户：</text>
          <text class="selector-value">{{ selectedTenant || '请选择租户' }}</text>
          <text class="selector-arrow">›</text>
        </view>
      </picker>
    </view>

    <!-- 部门树形列表 -->
    <scroll-view class="department-tree-scroll" scroll-y>
      <view class="department-tree">
        <view
          v-for="dept in departmentTree"
          :key="dept.id"
          class="department-item"
          :class="{ expanded: dept.expanded }"
        >
          <view class="department-header" @click="toggleExpand(dept)">
            <text class="expand-icon">{{ dept.expanded ? '▼' : '▶' }}</text>
            <text class="department-name">{{ dept.name }}</text>
            <view class="department-info">
              <text class="info-count">{{ dept.userCount }}人</text>
            </view>
            <view class="department-actions">
              <text class="action-btn" @click.stop="addSubDepartment(dept)">+ 子部门</text>
              <text class="action-btn" @click.stop="editDepartment(dept)">编辑</text>
              <text class="action-btn delete" @click.stop="deleteDepartment(dept)">删除</text>
            </view>
          </view>

          <!-- 子部门 -->
          <view v-if="dept.expanded && dept.children" class="sub-departments">
            <view
              v-for="child in dept.children"
              :key="child.id"
              class="sub-department-item"
            >
              <view class="sub-department-header" @click="toggleExpand(child)">
                <text class="expand-icon">{{ child.expanded ? '▼' : '▶' }}</text>
                <text class="sub-department-name">{{ child.name }}</text>
                <view class="department-info">
                  <text class="info-count">{{ child.userCount }}人</text>
                </view>
                <view class="department-actions">
                  <text class="action-btn" @click.stop="addSubDepartment(child)">+ 子部门</text>
                  <text class="action-btn" @click.stop="editDepartment(child)">编辑</text>
                  <text class="action-btn delete" @click.stop="deleteDepartment(child)">删除</text>
                </view>
              </view>

              <!-- 三级部门 -->
              <view v-if="child.expanded && child.children" class="sub-departments">
                <view
                  v-for="subChild in child.children"
                  :key="subChild.id"
                  class="sub-department-item"
                >
                  <view class="sub-department-header">
                    <text class="expand-icon" style="opacity: 0;">▶</text>
                    <text class="sub-department-name">{{ subChild.name }}</text>
                    <view class="department-info">
                      <text class="info-count">{{ subChild.userCount }}人</text>
                    </view>
                    <view class="department-actions">
                      <text class="action-btn" @click.stop="editDepartment(subChild)">编辑</text>
                      <text class="action-btn delete" @click.stop="deleteDepartment(subChild)">删除</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 新建/编辑部门弹窗 -->
    <view v-if="showDeptModal" class="modal-overlay" @click="hideDeptModal">
      <view class="dept-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑部门' : '新建部门' }}</text>
          <text class="modal-close" @click="hideDeptModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-section">
            <view class="form-item required">
              <text class="form-label">部门名称</text>
              <input
                v-model="deptForm.name"
                class="input-field"
                placeholder="请输入部门名称"
              />
            </view>

            <view class="form-item">
              <text class="form-label">上级部门</text>
              <picker mode="selector" :range="parentDeptOptions" @change="onParentChange">
                <view class="picker-input">
                  {{ deptForm.parentName || '无（顶级部门）' }}
                </view>
              </picker>
            </view>

            <view class="form-item">
              <text class="form-label">部门负责人</text>
              <input
                v-model="deptForm.leader"
                class="input-field"
                placeholder="请输入部门负责人"
              />
            </view>

            <view class="form-item">
              <text class="form-label">联系电话</text>
              <input
                v-model="deptForm.phone"
                class="input-field"
                placeholder="请输入联系电话"
                type="number"
              />
            </view>

            <view class="form-item">
              <text class="form-label">排序号</text>
              <input
                v-model="deptForm.sortOrder"
                class="input-field"
                placeholder="请输入排序号"
                type="number"
              />
            </view>

            <view class="form-item">
              <text class="form-label">备注说明</text>
              <textarea
                v-model="deptForm.remark"
                class="form-textarea"
                placeholder="请输入备注说明"
                :maxlength="500"
              ></textarea>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideDeptModal">取消</button>
          <button class="btn btn-primary" @click="saveDepartment">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedTenant: '',
      tenantList: ['市委组织部', '市教育局', '市卫健委', '市财政局'],
      showDeptModal: false,
      isEdit: false,
      currentDept: null,
      parentDeptOptions: ['无（顶级部门）'],
      deptForm: {
        name: '',
        parentId: '',
        parentName: '',
        leader: '',
        phone: '',
        sortOrder: 0,
        remark: ''
      },
      departmentTree: [
        {
          id: 1,
          name: '市委组织部',
          expanded: true,
          userCount: 45,
          children: [
            {
              id: 11,
              name: '干部教育科',
              expanded: false,
              userCount: 12,
              parentId: 1,
              children: []
            },
            {
              id: 12,
              name: '干部监督科',
              expanded: false,
              userCount: 8,
              parentId: 1,
              children: []
            },
            {
              id: 13,
              name: '干部调配科',
              expanded: false,
              userCount: 10,
              parentId: 1,
              children: []
            }
          ]
        },
        {
          id: 2,
          name: '市教育局',
          expanded: false,
          userCount: 68,
          children: [
            {
              id: 21,
              name: '人事科',
              expanded: false,
              userCount: 15,
              parentId: 2,
              children: [
                { id: 211, name: '师资管理组', userCount: 5, parentId: 21 },
                { id: 212, name: '编制管理组', userCount: 4, parentId: 21 }
              ]
            },
            {
              id: 22,
              name: '培训科',
              expanded: false,
              userCount: 18,
              parentId: 2,
              children: []
            }
          ]
        },
        {
          id: 3,
          name: '市卫健委',
          expanded: false,
          userCount: 52,
          children: []
        }
      ]
    }
  },

  methods: {
    onTenantChange(e) {
      this.selectedTenant = this.tenantList[e.detail.value]
      this.loadDepartments()
    },

    toggleExpand(dept) {
      dept.expanded = !dept.expanded
    },

    addDepartment() {
      if (!this.selectedTenant) {
        uni.showToast({
          title: '请先选择租户',
          icon: 'none'
        })
        return
      }

      this.isEdit = false
      this.currentDept = null
      this.deptForm = {
        name: '',
        parentId: '',
        parentName: '',
        leader: '',
        phone: '',
        sortOrder: 0,
        remark: ''
      }
      this.showDeptModal = true
    },

    addSubDepartment(parentDept) {
      this.isEdit = false
      this.currentDept = null
      this.deptForm = {
        name: '',
        parentId: parentDept.id,
        parentName: parentDept.name,
        leader: '',
        phone: '',
        sortOrder: 0,
        remark: ''
      }
      this.showDeptModal = true
    },

    editDepartment(dept) {
      this.isEdit = true
      this.currentDept = dept
      this.deptForm = {
        name: dept.name,
        parentId: dept.parentId || '',
        parentName: dept.parentName || '',
        leader: dept.leader || '',
        phone: dept.phone || '',
        sortOrder: dept.sortOrder || 0,
        remark: dept.remark || ''
      }
      this.showDeptModal = true
    },

    deleteDepartment(dept) {
      const hasChildren = dept.children && dept.children.length > 0

      uni.showModal({
        title: '删除部门',
        content: hasChildren
          ? `部门"${dept.name}"包含${dept.children.length}个子部门，确定要删除吗？`
          : `确定要删除部门"${dept.name}"吗？`,
        confirmColor: '#ef4444',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    onParentChange(e) {
      const index = e.detail.value
      if (index === 0) {
        this.deptForm.parentId = ''
        this.deptForm.parentName = ''
      } else {
        // 处理上级部门选择
      }
    },

    saveDepartment() {
      if (!this.deptForm.name) {
        uni.showToast({
          title: '请输入部门名称',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hideDeptModal()

        uni.showToast({
          title: this.isEdit ? '更新成功' : '创建成功',
          icon: 'success'
        })
      }, 1000)
    },

    hideDeptModal() {
      this.showDeptModal = false
    },

    loadDepartments() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.department-manage-container {
  min-height: 100vh;
  background: $bg-secondary;
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  border-bottom: 1rpx solid $border-color;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.tenant-section {
  padding: $spacing-md;
}

.tenant-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.selector-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.selector-value {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: 600;
}

.selector-arrow {
  font-size: 24rpx;
  color: $text-tertiary;
}

.department-tree-scroll {
  height: calc(100vh - 200rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.department-tree {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.department-item {
  background: $bg-primary;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.department-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.expand-icon {
  font-size: 20rpx;
  color: $text-tertiary;
  width: 30rpx;
}

.department-name {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.department-info {
  display: flex;
  gap: $spacing-md;
}

.info-count {
  font-size: $font-size-sm;
  color: $text-secondary;
  padding: 4rpx 12rpx;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
}

.department-actions {
  display: flex;
  gap: $spacing-sm;
}

.action-btn {
  font-size: $font-size-sm;
  color: $primary-color;
  padding: 4rpx 8rpx;
}

.action-btn.delete {
  color: #ef4444;
}

.sub-departments {
  padding-left: $spacing-xl;
  background: rgba(0, 0, 0, 0.02);
}

.sub-department-item {
  border-bottom: 1rpx solid $border-color;
}

.sub-department-item:last-child {
  border-bottom: none;
}

.sub-department-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
}

.sub-department-name {
  flex: 1;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.dept-modal {
  width: 100%;
  max-width: 650rpx;
  max-height: 85vh;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.modal-close {
  font-size: $font-size-xl;
  color: $text-secondary;
  padding: $spacing-sm;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.form-item.required .form-label::after {
  content: '*';
  color: #ef4444;
  margin-left: 4rpx;
}

.form-label {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.form-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.form-textarea {
  min-height: 120rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.picker-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
  padding-top: $spacing-lg;
  border-top: 1rpx solid $border-color;
}
</style>
