<template>
  <view class="role-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">角色权限管理</text>
        <text class="header-action" @click="addRole">+ 新建</text>
      </view>
    </view>

    <!-- 角色列表 -->
    <scroll-view class="role-list-scroll" scroll-y>
      <view class="role-list">
        <view
          v-for="role in roleList"
          :key="role.id"
          class="role-item card"
          @click="viewRoleDetail(role)"
        >
          <view class="role-header">
            <view class="role-info">
              <text class="role-name">{{ role.name }}</text>
              <view
                class="role-badge"
                :class="role.type"
              >
                {{ getTypeLabel(role.type) }}
              </view>
            </view>
            <view class="role-actions">
              <text class="action-btn" @click.stop="editRole(role)">编辑</text>
              <text class="action-btn" @click.stop="configPermission(role)">权限配置</text>
              <text
                class="action-btn delete"
                v-if="role.type !== 'system'"
                @click.stop="deleteRole(role)"
              >
                删除
              </text>
            </view>
          </view>

          <view class="role-meta">
            <text class="meta-item"><text class="fa fa-users"></text> 用户数：{{ role.userCount }}</text>
            <text class="meta-item"><text class="fa fa-edit"></text> 描述：{{ role.description }}</text>
          </view>

          <view class="role-permissions">
            <text class="permissions-label">主要权限：</text>
            <view class="permission-tags">
              <text
                v-for="(perm, index) in role.mainPermissions.slice(0, 4)"
                :key="index"
                class="permission-tag"
              >
                {{ perm }}
              </text>
              <text v-if="role.mainPermissions.length > 4" class="permission-more">
                +{{ role.mainPermissions.length - 4 }}
              </text>
            </view>
          </view>

          <view class="role-stats">
            <view class="stat-item">
              <text class="stat-value">{{ role.permissionCount }}</text>
              <text class="stat-label">权限总数</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ role.moduleCount }}</text>
              <text class="stat-label">可访问模块</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 新建/编辑角色弹窗 -->
    <view v-if="showRoleModal" class="modal-overlay" @click="hideRoleModal">
      <view class="role-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑角色' : '新建角色' }}</text>
          <text class="modal-close" @click="hideRoleModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-section">
            <view class="form-item required">
              <text class="form-label">角色名称</text>
              <input
                v-model="roleForm.name"
                class="input-field"
                placeholder="请输入角色名称"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">角色编码</text>
              <input
                v-model="roleForm.code"
                class="input-field"
                placeholder="请输入角色编码"
                :disabled="isEdit"
              />
            </view>

            <view class="form-item required">
              <text class="form-label">角色类型</text>
              <radio-group @change="onTypeChange">
                <label class="radio-item">
                  <radio value="custom" :checked="roleForm.type === 'custom'" color="#667eea" />
                  <text class="radio-label">自定义</text>
                </label>
                <label class="radio-item">
                  <radio value="system" :checked="roleForm.type === 'system'" color="#667eea" />
                  <text class="radio-label">系统角色</text>
                </label>
              </radio-group>
            </view>

            <view class="form-item required">
              <text class="form-label">角色描述</text>
              <textarea
                v-model="roleForm.description"
                class="form-textarea"
                placeholder="请输入角色描述"
                :maxlength="200"
              ></textarea>
            </view>

            <view class="form-item">
              <text class="form-label">排序号</text>
              <input
                v-model="roleForm.sortOrder"
                class="input-field"
                placeholder="请输入排序号"
                type="number"
              />
            </view>

            <view class="form-item">
              <text class="form-label">备注说明</text>
              <textarea
                v-model="roleForm.remark"
                class="form-textarea"
                placeholder="请输入备注说明"
                :maxlength="500"
              ></textarea>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideRoleModal">取消</button>
          <button class="btn btn-primary" @click="saveRole">保存</button>
        </view>
      </view>
    </view>

    <!-- 权限配置弹窗 -->
    <view v-if="showPermissionModal" class="modal-overlay" @click="hidePermissionModal">
      <view class="permission-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">权限配置 - {{ currentRole?.name }}</text>
          <text class="modal-close" @click="hidePermissionModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="permission-section">
            <view class="section-header">
              <text class="section-title">模块权限</text>
              <view class="select-all-btn" @click="toggleAllModules">
                <checkbox :checked="allModulesSelected" color="#667eea" />
                <text class="select-all-text">全选</text>
              </view>
            </view>

            <view class="module-list">
              <view
                v-for="module in permissionModules"
                :key="module.id"
                class="module-item"
              >
                <view class="module-header" @click="toggleModule(module)">
                  <checkbox-group>
                    <label class="module-checkbox">
                      <checkbox
                        :checked="isModuleSelected(module.id)"
                        color="#667eea"
                        @click.stop="toggleModule(module)"
                      />
                    </label>
                  </checkbox-group>
                  <text class="module-name">{{ module.name }}</text>
                  <text class="expand-icon">{{ module.expanded ? '▼' : '▶' }}</text>
                </view>

                <view v-if="module.expanded" class="permission-details">
                  <view class="permission-group">
                    <text class="group-title">基础权限</text>
                    <view class="permission-options">
                      <label
                        v-for="perm in module.basicPermissions"
                        :key="perm.key"
                        class="permission-option"
                      >
                        <checkbox
                          :checked="isPermissionSelected(module.id, perm.key)"
                          color="#667eea"
                          @click="togglePermission(module.id, perm.key)"
                        />
                        <text class="permission-label">{{ perm.label }}</text>
                      </label>
                    </view>
                  </view>

                  <view class="permission-group" v-if="module.advancedPermissions">
                    <text class="group-title">高级权限</text>
                    <view class="permission-options">
                      <label
                        v-for="perm in module.advancedPermissions"
                        :key="perm.key"
                        class="permission-option"
                      >
                        <checkbox
                          :checked="isPermissionSelected(module.id, perm.key)"
                          color="#667eea"
                          @click="togglePermission(module.id, perm.key)"
                        />
                        <text class="permission-label">{{ perm.label }}</text>
                      </label>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hidePermissionModal">取消</button>
          <button class="btn btn-primary" @click="savePermissions">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      showRoleModal: false,
      showPermissionModal: false,
      isEdit: false,
      currentRole: null,
      roleForm: {
        name: '',
        code: '',
        type: 'custom',
        description: '',
        sortOrder: 0,
        remark: ''
      },
      selectedPermissions: [],
      roleList: [
        {
          id: 1,
          name: '超级管理员',
          code: 'SUPER_ADMIN',
          type: 'system',
          description: '拥有系统所有权限',
          userCount: 2,
          permissionCount: 156,
          moduleCount: 12,
          mainPermissions: ['全部权限', '用户管理', '角色管理', '系统设置']
        },
        {
          id: 2,
          name: '培训管理员',
          code: 'TRAINING_ADMIN',
          type: 'custom',
          description: '负责培训相关业务管理',
          userCount: 8,
          permissionCount: 45,
          moduleCount: 6,
          mainPermissions: ['培训管理', '学员管理', '课程管理', '签到管理']
        },
        {
          id: 3,
          name: '工作人员',
          code: 'STAFF',
          type: 'custom',
          description: '工作人员角色，负责日常业务',
          userCount: 25,
          permissionCount: 28,
          moduleCount: 5,
          mainPermissions: ['学员信息', '签到管理', '消息通知', '数据统计']
        },
        {
          id: 4,
          name: '学员',
          code: 'LEARNER',
          type: 'system',
          description: '学员角色，参与培训学习',
          userCount: 156,
          permissionCount: 15,
          moduleCount: 4,
          mainPermissions: ['个人信息', '培训课程', '签到打卡', '评价反馈']
        }
      ],
      permissionModules: [
        {
          id: 'training',
          name: '培训管理',
          expanded: true,
          basicPermissions: [
            { key: 'view', label: '查看培训' },
            { key: 'create', label: '创建培训' },
            { key: 'edit', label: '编辑培训' },
            { key: 'delete', label: '删除培训' }
          ],
          advancedPermissions: [
            { key: 'publish', label: '发布培训' },
            { key: 'export', label: '导出数据' }
          ]
        },
        {
          id: 'learner',
          name: '学员管理',
          expanded: false,
          basicPermissions: [
            { key: 'view', label: '查看学员' },
            { key: 'create', label: '添加学员' },
            { key: 'edit', label: '编辑学员' },
            { key: 'delete', label: '删除学员' }
          ],
          advancedPermissions: [
            { key: 'import', label: '导入学员' },
            { key: 'export', label: '导出学员' }
          ]
        },
        {
          id: 'course',
          name: '课程管理',
          expanded: false,
          basicPermissions: [
            { key: 'view', label: '查看课程' },
            { key: 'create', label: '创建课程' },
            { key: 'edit', label: '编辑课程' },
            { key: 'delete', label: '删除课程' }
          ]
        },
        {
          id: 'checkin',
          name: '签到管理',
          expanded: false,
          basicPermissions: [
            { key: 'view', label: '查看签到' },
            { key: 'manage', label: '管理签到' },
            { key: 'export', label: '导出数据' }
          ]
        },
        {
          id: 'notice',
          name: '消息通知',
          expanded: false,
          basicPermissions: [
            { key: 'view', label: '查看消息' },
            { key: 'send', label: '发送消息' },
            { key: 'manage', label: '管理消息' }
          ]
        },
        {
          id: 'report',
          name: '数据统计',
          expanded: false,
          basicPermissions: [
            { key: 'view', label: '查看报表' },
            { key: 'export', label: '导出报表' }
          ]
        }
      ]
    }
  },

  computed: {
    allModulesSelected() {
      return this.permissionModules.every(module =>
        this.selectedPermissions.includes(module.id)
      )
    }
  },

  methods: {
    getTypeLabel(type) {
      const labelMap = {
        system: '系统',
        custom: '自定义'
      }
      return labelMap[type] || type
    },

    addRole() {
      this.isEdit = false
      this.currentRole = null
      this.roleForm = {
        name: '',
        code: '',
        type: 'custom',
        description: '',
        sortOrder: 0,
        remark: ''
      }
      this.showRoleModal = true
    },

    editRole(role) {
      this.isEdit = true
      this.currentRole = role
      this.roleForm = { ...role }
      this.showRoleModal = true
    },

    viewRoleDetail(role) {
      // 查看角色详情
    },

    deleteRole(role) {
      uni.showModal({
        title: '删除角色',
        content: `确定要删除角色"${role.name}"吗？`,
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

    onTypeChange(e) {
      this.roleForm.type = e.detail.value
    },

    saveRole() {
      if (!this.roleForm.name) {
        uni.showToast({
          title: '请输入角色名称',
          icon: 'none'
        })
        return
      }

      if (!this.roleForm.code) {
        uni.showToast({
          title: '请输入角色编码',
          icon: 'none'
        })
        return
      }

      if (!this.roleForm.description) {
        uni.showToast({
          title: '请输入角色描述',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hideRoleModal()

        uni.showToast({
          title: this.isEdit ? '更新成功' : '创建成功',
          icon: 'success'
        })
      }, 1000)
    },

    hideRoleModal() {
      this.showRoleModal = false
    },

    configPermission(role) {
      this.currentRole = role
      // 模拟加载角色权限
      this.selectedPermissions = ['training', 'learner', 'course', 'checkin']
      this.showPermissionModal = true
    },

    toggleModule(module) {
      module.expanded = !module.expanded
    },

    toggleAllModules() {
      if (this.allModulesSelected) {
        this.selectedPermissions = []
      } else {
        this.selectedPermissions = this.permissionModules.map(m => m.id)
      }
    },

    isModuleSelected(moduleId) {
      return this.selectedPermissions.includes(moduleId)
    },

    isPermissionSelected(moduleId, permKey) {
      return this.selectedPermissions.includes(`${moduleId}:${permKey}`)
    },

    togglePermission(moduleId, permKey) {
      const key = `${moduleId}:${permKey}`
      const index = this.selectedPermissions.indexOf(key)

      if (index > -1) {
        this.selectedPermissions.splice(index, 1)
      } else {
        this.selectedPermissions.push(key)
      }
    },

    savePermissions() {
      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hidePermissionModal()

        uni.showToast({
          title: '保存成功',
          icon: 'success'
        })
      }, 1000)
    },

    hidePermissionModal() {
      this.showPermissionModal = false
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

.role-manage-container {
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

.role-list-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.role-item {
  margin-bottom: $spacing-md;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.role-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.role-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.role-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
}

.role-badge.system {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.role-badge.custom {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.role-actions {
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

.role-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.role-permissions {
  margin-bottom: $spacing-md;
}

.permissions-label {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.permission-tag {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.permission-more {
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.05);
  color: $text-tertiary;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.role-stats {
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

.role-modal,
.permission-modal {
  width: 100%;
  max-width: 650rpx;
  max-height: 85vh;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
}

.permission-modal {
  max-width: 700rpx;
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

.permission-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: $spacing-sm;
  border-bottom: 1rpx solid $border-color;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.select-all-btn {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.select-all-text {
  font-size: $font-size-sm;
  color: $text-primary;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.module-item {
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.module-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.module-checkbox {
  flex-shrink: 0;
}

.module-name {
  flex: 1;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.expand-icon {
  font-size: 20rpx;
  color: $text-tertiary;
}

.permission-details {
  padding: $spacing-md;
}

.permission-group {
  margin-bottom: $spacing-lg;
}

.permission-group:last-child {
  margin-bottom: 0;
}

.group-title {
  display: block;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.permission-options {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.permission-option {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.permission-label {
  font-size: $font-size-sm;
  color: $text-primary;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
  padding-top: $spacing-lg;
  border-top: 1rpx solid $border-color;
}
</style>
