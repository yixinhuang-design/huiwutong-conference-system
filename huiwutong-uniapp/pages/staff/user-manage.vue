<template>
  <view class="user-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">用户管理</text>
        <text class="header-action" @click="addUser">+ 新建</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-section">
      <view class="filter-bar">
        <picker mode="selector" :range="tenantList" @change="onTenantChange">
          <view class="filter-item">
            <text class="filter-label">租户：</text>
            <text class="filter-value">{{ selectedTenant || '全部租户' }}</text>
            <text class="filter-arrow">›</text>
          </view>
        </picker>

        <picker mode="selector" :range="roleList" @change="onRoleChange">
          <view class="filter-item">
            <text class="filter-label">角色：</text>
            <text class="filter-value">{{ selectedRole || '全部角色' }}</text>
            <text class="filter-arrow">›</text>
          </view>
        </picker>

        <picker mode="selector" :range="statusList" @change="onStatusChange">
          <view class="filter-item">
            <text class="filter-label">状态：</text>
            <text class="filter-value">{{ selectedStatus || '全部状态' }}</text>
            <text class="filter-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon"><text class="fa fa-search"></text></text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索用户名、手机号、邮箱"
          @input="searchUsers"
        />
      </view>
    </view>

    <!-- 用户列表 -->
    <scroll-view class="user-list-scroll" scroll-y>
      <view class="user-list">
        <view
          v-for="user in filteredUsers"
          :key="user.id"
          class="user-item card"
          @click="viewUserDetail(user)"
        >
          <view class="user-header">
            <view class="user-avatar">
              {{ user.name.charAt(user.name.length - 1) }}
            </view>
            <view class="user-info">
              <text class="user-name">{{ user.name }}</text>
              <view class="user-tags">
                <text class="user-tag">{{ user.role }}</text>
                <text
                  class="user-status"
                  :class="user.status"
                >
                  {{ getStatusLabel(user.status) }}
                </text>
              </view>
            </view>
            <view class="user-actions">
              <text class="action-btn" @click.stop="editUser(user)">编辑</text>
              <text
                class="action-btn"
                :class="{ disabled: user.status === 'disabled' }"
                @click.stop="toggleUserStatus(user)"
              >
                {{ user.status === 'active' ? '禁用' : '启用' }}
              </text>
              <text class="action-btn delete" @click.stop="deleteUser(user)">删除</text>
            </view>
          </view>

          <view class="user-meta">
            <view class="meta-row">
              <text class="meta-label">租户：</text>
              <text class="meta-value">{{ user.tenant }}</text>
            </view>
            <view class="meta-row">
              <text class="meta-label">部门：</text>
              <text class="meta-value">{{ user.department || '-' }}</text>
            </view>
            <view class="meta-row">
              <text class="meta-label">手机号：</text>
              <text class="meta-value">{{ user.phone }}</text>
            </view>
            <view class="meta-row">
              <text class="meta-label">邮箱：</text>
              <text class="meta-value">{{ user.email }}</text>
            </view>
          </view>

          <view class="user-stats">
            <view class="stat-item">
              <text class="stat-value">{{ user.loginCount }}</text>
              <text class="stat-label">登录次数</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ user.lastLogin }}</text>
              <text class="stat-label">最后登录</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="load-more-btn" @click="loadMore">
        <text>加载更多</text>
      </view>
    </scroll-view>

    <!-- 新建/编辑用户弹窗 -->
    <view v-if="showUserModal" class="modal-overlay" @click="hideUserModal">
      <view class="user-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑用户' : '新建用户' }}</text>
          <text class="modal-close" @click="hideUserModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-section">
            <view class="form-item required">
              <text class="form-label">用户名</text>
              <input
                v-model="userForm.username"
                class="form-input"
                placeholder="请输入用户名"
                :disabled="isEdit"
              />
            </view>

            <view class="form-item required" v-if="!isEdit">
              <text class="form-label">密码</text>
              <input
                v-model="userForm.password"
                class="form-input"
                placeholder="请输入密码"
                type="password"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">姓名</text>
              <input
                v-model="userForm.name"
                class="form-input"
                placeholder="请输入姓名"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">租户</text>
              <picker mode="selector" :range="tenantList" @change="onFormTenantChange">
                <view class="picker-input">
                  {{ userForm.tenant || '请选择租户' }}
                </view>
              </picker>
            </view>

            <view class="form-item required">
              <text class="form-label">角色</text>
              <picker mode="selector" :range="roleList" @change="onFormRoleChange">
                <view class="picker-input">
                  {{ userForm.role || '请选择角色' }}
                </view>
              </picker>
            </view>

            <view class="form-item">
              <text class="form-label">部门</text>
              <input
                v-model="userForm.department"
                class="form-input"
                placeholder="请输入部门"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">手机号</text>
              <input
                v-model="userForm.phone"
                class="form-input"
                placeholder="请输入手机号"
                type="number"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">邮箱</text>
              <input
                v-model="userForm.email"
                class="form-input"
                placeholder="请输入邮箱"
                type="email"
              />
            </view>

            <view class="form-item">
              <text class="form-label">账号状态</text>
              <radio-group @change="onFormStatusChange">
                <label class="radio-item">
                  <radio value="active" :checked="userForm.status === 'active'" color="#667eea" />
                  <text class="radio-label">启用</text>
                </label>
                <label class="radio-item">
                  <radio value="disabled" :checked="userForm.status === 'disabled'" color="#667eea" />
                  <text class="radio-label">禁用</text>
                </label>
              </radio-group>
            </view>

            <view class="form-item">
              <text class="form-label">备注说明</text>
              <textarea
                v-model="userForm.remark"
                class="form-textarea"
                placeholder="请输入备注说明"
                :maxlength="500"
              ></textarea>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideUserModal">取消</button>
          <button class="btn btn-primary" @click="saveUser">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchKeyword: '',
      selectedTenant: '',
      selectedRole: '',
      selectedStatus: '',
      hasMore: true,
      currentPage: 1,
      showUserModal: false,
      isEdit: false,
      currentUser: null,
      tenantList: ['市委组织部', '市教育局', '市卫健委', '市财政局'],
      roleList: ['管理员', '工作人员', '学员'],
      statusList: ['启用', '禁用'],
      userForm: {
        username: '',
        password: '',
        name: '',
        tenant: '',
        role: '',
        department: '',
        phone: '',
        email: '',
        status: 'active',
        remark: ''
      },
      userList: [
        {
          id: 1,
          username: 'admin001',
          name: '张伟',
          tenant: '市委组织部',
          role: '管理员',
          department: '培训科',
          phone: '13800138001',
          email: 'zhangwei@org.gov.cn',
          status: 'active',
          loginCount: 156,
          lastLogin: '2025-01-16'
        },
        {
          id: 2,
          username: 'staff002',
          name: '李娜',
          tenant: '市教育局',
          role: '工作人员',
          department: '人事科',
          phone: '13800138002',
          email: 'lina@edu.gov.cn',
          status: 'active',
          loginCount: 89,
          lastLogin: '2025-01-15'
        },
        {
          id: 3,
          username: 'learner003',
          name: '王强',
          tenant: '市卫健委',
          role: '学员',
          department: '办公室',
          phone: '13800138003',
          email: 'wangqiang@hea.gov.cn',
          status: 'active',
          loginCount: 23,
          lastLogin: '2025-01-14'
        },
        {
          id: 4,
          username: 'admin004',
          name: '刘芳',
          tenant: '市财政局',
          role: '管理员',
          department: '培训中心',
          phone: '13800138004',
          email: 'liufang@fin.gov.cn',
          status: 'disabled',
          loginCount: 201,
          lastLogin: '2025-01-10'
        },
        {
          id: 5,
          username: 'learner005',
          name: '陈明',
          tenant: '市委组织部',
          role: '学员',
          department: '',
          phone: '13800138005',
          email: 'chenming@org.gov.cn',
          status: 'active',
          loginCount: 45,
          lastLogin: '2025-01-16'
        }
      ]
    }
  },

  computed: {
    filteredUsers() {
      let users = this.userList

      if (this.selectedTenant) {
        users = users.filter(user => user.tenant === this.selectedTenant)
      }

      if (this.selectedRole) {
        users = users.filter(user => user.role === this.selectedRole)
      }

      if (this.selectedStatus === '启用') {
        users = users.filter(user => user.status === 'active')
      } else if (this.selectedStatus === '禁用') {
        users = users.filter(user => user.status === 'disabled')
      }

      if (this.searchKeyword) {
        users = users.filter(user =>
          user.name.includes(this.searchKeyword) ||
          user.phone.includes(this.searchKeyword) ||
          user.email.includes(this.searchKeyword)
        )
      }

      return users
    }
  },

  methods: {
    getStatusLabel(status) {
      const labelMap = {
        active: '启用',
        disabled: '禁用'
      }
      return labelMap[status] || status
    },

    searchUsers() {
      // 搜索用户
    },

    onTenantChange(e) {
      this.selectedTenant = this.tenantList[e.detail.value] || ''
    },

    onRoleChange(e) {
      this.selectedRole = this.roleList[e.detail.value] || ''
    },

    onStatusChange(e) {
      this.selectedStatus = this.statusList[e.detail.value] || ''
    },

    onFormTenantChange(e) {
      this.userForm.tenant = this.tenantList[e.detail.value]
    },

    onFormRoleChange(e) {
      this.userForm.role = this.roleList[e.detail.value]
    },

    onFormStatusChange(e) {
      this.userForm.status = e.detail.value
    },

    addUser() {
      this.isEdit = false
      this.currentUser = null
      this.userForm = {
        username: '',
        password: '',
        name: '',
        tenant: '',
        role: '',
        department: '',
        phone: '',
        email: '',
        status: 'active',
        remark: ''
      }
      this.showUserModal = true
    },

    editUser(user) {
      this.isEdit = true
      this.currentUser = user
      this.userForm = { ...user }
      this.showUserModal = true
    },

    viewUserDetail(user) {
      uni.navigateTo({
        url: `/pages/staff/user-detail?id=${user.id}`
      })
    },

    toggleUserStatus(user) {
      const action = user.status === 'active' ? '禁用' : '启用'

      uni.showModal({
        title: `${action}用户`,
        content: `确定要${action}用户"${user.name}"吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })

            setTimeout(() => {
              uni.hideLoading()
              user.status = user.status === 'active' ? 'disabled' : 'active'

              uni.showToast({
                title: `${action}成功`,
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    deleteUser(user) {
      uni.showModal({
        title: '删除用户',
        content: `确定要删除用户"${user.name}"吗？`,
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

    saveUser() {
      if (!this.userForm.username) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return
      }

      if (!this.isEdit && !this.userForm.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }

      if (!this.userForm.name) {
        uni.showToast({
          title: '请输入姓名',
          icon: 'none'
        })
        return
      }

      if (!this.userForm.tenant) {
        uni.showToast({
          title: '请选择租户',
          icon: 'none'
        })
        return
      }

      if (!this.userForm.role) {
        uni.showToast({
          title: '请选择角色',
          icon: 'none'
        })
        return
      }

      if (!this.userForm.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }

      if (!this.userForm.email) {
        uni.showToast({
          title: '请输入邮箱',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hideUserModal()

        uni.showToast({
          title: this.isEdit ? '更新成功' : '创建成功',
          icon: 'success'
        })
      }, 1000)
    },

    hideUserModal() {
      this.showUserModal = false
    },

    loadMore() {
      if (!this.hasMore) return

      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hasMore = false
      }, 1000)
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

.user-manage-container {
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

.filter-section {
  padding: $spacing-md;
  padding-bottom: 0;
}

.filter-bar {
  display: flex;
  gap: $spacing-md;
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.filter-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.filter-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
}

.filter-arrow {
  font-size: 20rpx;
  color: $text-tertiary;
}

.search-section {
  padding: $spacing-md;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.search-icon {
  font-size: $font-size-lg;
}

.search-input {
  flex: 1;
  font-size: $font-size-md;
}

.user-list-scroll {
  height: calc(100vh - 340rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.user-item {
  margin-bottom: $spacing-md;
}

.user-header {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.user-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.user-tags {
  display: flex;
  gap: $spacing-sm;
}

.user-tag {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.user-status {
  padding: 4rpx 12rpx;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.user-status.active {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.user-status.disabled {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  align-items: flex-end;
}

.action-btn {
  font-size: $font-size-sm;
  color: $primary-color;
}

.action-btn.disabled {
  color: $text-tertiary;
}

.action-btn.delete {
  color: #ef4444;
}

.user-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.meta-row {
  display: flex;
  gap: $spacing-xs;
}

.meta-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.meta-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
}

.user-stats {
  display: flex;
  gap: $spacing-md;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.load-more-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
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

.user-modal {
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

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  margin-right: $spacing-xl;
}

.radio-label {
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
