<template>
  <view class="tenant-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">租户管理</text>
        <text class="header-action" @click="addTenant">+ 新建</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon"><text class="fa fa-search"></text></text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索租户名称"
          @input="searchTenants"
        />
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value">{{ tenantStats.total }}</text>
        <text class="stat-label">总租户数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ tenantStats.active }}</text>
        <text class="stat-label">活跃租户</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ tenantStats.expiring }}</text>
        <text class="stat-label">即将到期</text>
      </view>
    </view>

    <!-- 租户列表 -->
    <scroll-view class="tenant-list-scroll" scroll-y>
      <view class="tenant-list">
        <view
          v-for="tenant in filteredTenants"
          :key="tenant.id"
          class="tenant-item card"
          @click="viewTenantDetail(tenant)"
        >
          <view class="tenant-header">
            <view class="tenant-info">
              <text class="tenant-name">{{ tenant.name }}</text>
              <view
                class="tenant-status"
                :class="tenant.status"
              >
                <text class="status-text">{{ getStatusLabel(tenant.status) }}</text>
              </view>
            </view>
            <view class="tenant-actions">
              <text class="action-btn" @click.stop="editTenant(tenant)">编辑</text>
              <text class="action-btn delete" @click.stop="deleteTenant(tenant)">删除</text>
            </view>
          </view>

          <view class="tenant-meta">
            <text class="meta-item">📧 {{ tenant.contact }}</text>
            <text class="meta-item">📱 {{ tenant.phone }}</text>
          </view>

          <view class="tenant-details">
            <view class="detail-item">
              <text class="detail-label">租户代码：</text>
              <text class="detail-value">{{ tenant.code }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">到期时间：</text>
              <text class="detail-value" :class="{ expiring: isExpiring(tenant.expiryDate) }">
                {{ tenant.expiryDate }}
              </text>
            </view>
            <view class="detail-item">
              <text class="detail-label">用户数量：</text>
              <text class="detail-value">{{ tenant.userCount }}/{{ tenant.maxUsers }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">存储空间：</text>
              <text class="detail-value">{{ formatStorage(tenant.usedStorage, tenant.totalStorage) }}</text>
            </view>
          </view>

          <view class="tenant-progress">
            <view class="progress-item">
              <text class="progress-label">用户配额</text>
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (tenant.userCount / tenant.maxUsers * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-value">{{ tenant.userCount }}/{{ tenant.maxUsers }}</text>
            </view>
            <view class="progress-item">
              <text class="progress-label">存储空间</text>
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (tenant.usedStorage / tenant.totalStorage * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-value">{{ formatStorage(tenant.usedStorage, tenant.totalStorage) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="load-more-btn" @click="loadMore">
        <text>加载更多</text>
      </view>
    </scroll-view>

    <!-- 新建/编辑租户弹窗 -->
    <view v-if="showTenantModal" class="modal-overlay" @click="hideTenantModal">
      <view class="tenant-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑租户' : '新建租户' }}</text>
          <text class="modal-close" @click="hideTenantModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-section">
            <view class="form-item required">
              <text class="form-label">租户名称</text>
              <input
                v-model="tenantForm.name"
                class="form-input"
                placeholder="请输入租户名称"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">租户代码</text>
              <input
                v-model="tenantForm.code"
                class="form-input"
                placeholder="请输入租户代码"
                :disabled="isEdit"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">联系人</text>
              <input
                v-model="tenantForm.contact"
                class="form-input"
                placeholder="请输入联系人姓名"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">联系电话</text>
              <input
                v-model="tenantForm.phone"
                class="form-input"
                placeholder="请输入联系电话"
                type="number"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">联系邮箱</text>
              <input
                v-model="tenantForm.email"
                class="form-input"
                placeholder="请输入联系邮箱"
                type="email"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">到期时间</text>
              <picker mode="date" :value="tenantForm.expiryDate" @change="onExpiryDateChange">
                <view class="picker-input">
                  {{ tenantForm.expiryDate || '请选择到期时间' }}
                </view>
              </picker>
            </view>

            <view class="form-item required">
              <text class="form-label">最大用户数</text>
              <input
                v-model="tenantForm.maxUsers"
                class="form-input"
                placeholder="请输入最大用户数"
                type="number"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">存储空间（GB）</text>
              <input
                v-model="tenantForm.totalStorage"
                class="form-input"
                placeholder="请输入存储空间"
                type="number"
              />
            </view>

            <view class="form-item">
              <text class="form-label">租户状态</text>
              <radio-group @change="onStatusChange">
                <label class="radio-item">
                  <radio value="active" :checked="tenantForm.status === 'active'" color="#667eea" />
                  <text class="radio-label">活跃</text>
                </label>
                <label class="radio-item">
                  <radio value="suspended" :checked="tenantForm.status === 'suspended'" color="#667eea" />
                  <text class="radio-label">暂停</text>
                </label>
              </radio-group>
            </view>

            <view class="form-item">
              <text class="form-label">备注说明</text>
              <textarea
                v-model="tenantForm.remark"
                class="form-textarea"
                placeholder="请输入备注说明"
                :maxlength="500"
              ></textarea>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideTenantModal">取消</button>
          <button class="btn btn-primary" @click="saveTenant">保存</button>
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
      hasMore: true,
      currentPage: 1,
      showTenantModal: false,
      isEdit: false,
      currentTenant: null,
      tenantStats: {
        total: 12,
        active: 10,
        expiring: 2
      },
      tenantForm: {
        name: '',
        code: '',
        contact: '',
        phone: '',
        email: '',
        expiryDate: '',
        maxUsers: 100,
        totalStorage: 100,
        status: 'active',
        remark: ''
      },
      tenantList: [
        {
          id: 1,
          name: '市委组织部',
          code: 'ORG001',
          contact: '张主任',
          phone: '13800138001',
          email: 'zhang@org.gov.cn',
          status: 'active',
          expiryDate: '2025-12-31',
          userCount: 85,
          maxUsers: 100,
          usedStorage: 68.5,
          totalStorage: 100
        },
        {
          id: 2,
          name: '市教育局',
          code: 'EDU001',
          contact: '李科长',
          phone: '13800138002',
          email: 'li@edu.gov.cn',
          status: 'active',
          expiryDate: '2025-06-30',
          userCount: 120,
          maxUsers: 200,
          usedStorage: 85.2,
          totalStorage: 200
        },
        {
          id: 3,
          name: '市卫健委',
          code: 'HEA001',
          contact: '王处长',
          phone: '13800138003',
          email: 'wang@hea.gov.cn',
          status: 'expiring',
          expiryDate: '2025-02-28',
          userCount: 65,
          maxUsers: 100,
          usedStorage: 45.8,
          totalStorage: 100
        },
        {
          id: 4,
          name: '市财政局',
          code: 'FIN001',
          contact: '赵科长',
          phone: '13800138004',
          email: 'zhao@fin.gov.cn',
          status: 'active',
          expiryDate: '2025-12-31',
          userCount: 92,
          maxUsers: 150,
          usedStorage: 78.3,
          totalStorage: 150
        }
      ]
    }
  },

  computed: {
    filteredTenants() {
      if (!this.searchKeyword) return this.tenantList

      return this.tenantList.filter(tenant =>
        tenant.name.includes(this.searchKeyword) ||
        tenant.code.includes(this.searchKeyword)
      )
    }
  },

  methods: {
    getStatusLabel(status) {
      const labelMap = {
        active: '活跃',
        suspended: '暂停',
        expiring: '即将到期'
      }
      return labelMap[status] || status
    },

    isExpiring(date) {
      const expiryDate = new Date(date)
      const today = new Date()
      const diffDays = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24))
      return diffDays <= 30
    },

    formatStorage(used, total) {
      return `${used.toFixed(1)}GB/${total}GB`
    },

    searchTenants() {
      // 搜索租户
    },

    addTenant() {
      this.isEdit = false
      this.currentTenant = null
      this.tenantForm = {
        name: '',
        code: '',
        contact: '',
        phone: '',
        email: '',
        expiryDate: '',
        maxUsers: 100,
        totalStorage: 100,
        status: 'active',
        remark: ''
      }
      this.showTenantModal = true
    },

    editTenant(tenant) {
      this.isEdit = true
      this.currentTenant = tenant
      this.tenantForm = { ...tenant }
      this.showTenantModal = true
    },

    viewTenantDetail(tenant) {
      uni.navigateTo({
        url: `/pages/staff/tenant-detail?id=${tenant.id}`
      })
    },

    deleteTenant(tenant) {
      uni.showModal({
        title: '删除租户',
        content: `确定要删除租户"${tenant.name}"吗？`,
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

    onExpiryDateChange(e) {
      this.tenantForm.expiryDate = e.detail.value
    },

    onStatusChange(e) {
      this.tenantForm.status = e.detail.value
    },

    saveTenant() {
      if (!this.tenantForm.name) {
        uni.showToast({
          title: '请输入租户名称',
          icon: 'none'
        })
        return
      }

      if (!this.tenantForm.code) {
        uni.showToast({
          title: '请输入租户代码',
          icon: 'none'
        })
        return
      }

      if (!this.tenantForm.contact) {
        uni.showToast({
          title: '请输入联系人',
          icon: 'none'
        })
        return
      }

      if (!this.tenantForm.phone) {
        uni.showToast({
          title: '请输入联系电话',
          icon: 'none'
        })
        return
      }

      if (!this.tenantForm.expiryDate) {
        uni.showToast({
          title: '请选择到期时间',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hideTenantModal()

        uni.showToast({
          title: this.isEdit ? '更新成功' : '创建成功',
          icon: 'success'
        })
      }, 1000)
    },

    hideTenantModal() {
      this.showTenantModal = false
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

.tenant-manage-container {
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

.stats-section {
  display: flex;
  gap: $spacing-md;
  padding: 0 $spacing-md $spacing-md;
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-lg $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.tenant-list-scroll {
  height: calc(100vh - 320rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.tenant-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.tenant-item {
  margin-bottom: $spacing-md;
}

.tenant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.tenant-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.tenant-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.tenant-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
}

.tenant-status.active {
  background: rgba(16, 185, 129, 0.1);
}

.tenant-status.active .status-text {
  color: #10b981;
}

.tenant-status.expiring {
  background: rgba(245, 158, 11, 0.1);
}

.tenant-status.expiring .status-text {
  color: #f59e0b;
}

.tenant-status.suspended {
  background: rgba(239, 68, 68, 0.1);
}

.tenant-status.suspended .status-text {
  color: #ef4444;
}

.status-text {
  font-size: $font-size-xs;
  font-weight: 500;
}

.tenant-actions {
  display: flex;
  gap: $spacing-md;
}

.action-btn {
  font-size: $font-size-sm;
  color: $primary-color;
}

.action-btn.delete {
  color: #ef4444;
}

.tenant-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.tenant-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-sm;
}

.detail-label {
  color: $text-secondary;
}

.detail-value {
  color: $text-primary;
  font-weight: 500;
}

.detail-value.expiring {
  color: #f59e0b;
}

.tenant-progress {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.progress-label {
  width: 120rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.progress-value {
  width: 120rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: right;
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

.tenant-modal {
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
