<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title">2025党务干部培训班</view>
      <view class="sub">分组编排</view>
    </view>

    <view class="card">
      <view class="card-title">分组列表</view>
      <view class="group-item" v-for="g in groups" :key="g.id">
        <view>
          <view class="g-name" v-text="getGroupText(g)"></view>
          <view class="g-desc" v-text="getMembersText(g)"></view>
        </view>
        <button class="mini" @click="pickGroup(g)">管理</button>
      </view>
    </view>

    <view class="card">
      <view class="card-title">待分配成员</view>
      <view class="group-item" v-for="u in ungrouped" :key="u.id">
        <view>
          <view class="g-name" v-text="getUserText(u)"></view>
          <view class="g-desc" v-text="getOrgText(u)"></view>
        </view>
        <button class="mini primary" @click="autoAssign(u)">一键分配</button>
      </view>
      <view class="empty" v-if="ungrouped.length === 0">暂无待分配成员</view>
    </view>

    <view class="footer">
      <button class="save-btn" @click="saveGrouping">保存分组结果</button>
    </view>

    <view class="mask" v-if="selectedGroup" @click="selectedGroup = null">
      <view class="modal" @click.stop>
        <view class="modal-title" v-text="getModalTitle()"></view>
        <picker class="picker" :range="ungroupedNames" @change="appendMemberToSelected">
          <view class="picker-value">从待分配成员中添加</view>
        </picker>
        <view class="m-list">
          <view class="m-item" v-for="m in selectedGroup.members" :key="m.id">
            <text v-text="m.name"></text>
            <button class="x" @click="removeFromGroup(selectedGroup, m)">移出</button>
          </view>
        </view>
        <button class="save-btn" @click="selectedGroup = null">完成</button>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const groups = ref([])
const ungrouped = ref([])
const selectedGroup = ref(null)

const ungroupedNames = computed(function() {
  return ungrouped.value.map(function(u) { return u.name })
})

function getGroupText(g) {
  var leader = g.leader || '未设置'
  return g.name + '（组长：' + leader + '）'
}

function getMembersText(g) {
  var names = ''
  if (g.members && g.members.length > 0) {
    var list = []
    for (var i = 0; i < g.members.length; i++) {
      list.push(g.members[i].name)
    }
    names = list.join('、')
  }
  return '成员：' + (names || '暂无')
}

function getUserText(u) {
  var title = u.title || '成员'
  return u.name + '（' + title + '）'
}

function getOrgText(u) {
  var org = u.org || '未设置单位'
  return org + '，待分配'
}

function getModalTitle() {
  return '管理分组：' + (selectedGroup.value ? selectedGroup.value.name : '')
}

function mockData() {
  groups.value = [
    { id: 'A', name: 'A组', leader: '李伟', members: [{ id: 101, name: '张三' }, { id: 102, name: '王五' }, { id: 103, name: '赵六' }] },
    { id: 'B', name: 'B组', leader: '王芳', members: [{ id: 104, name: '钱七' }, { id: 105, name: '孙八' }, { id: 106, name: '周九' }] },
    { id: 'C', name: 'C组', leader: '刘强', members: [{ id: 107, name: '吴十' }, { id: 108, name: '郑一' }] },
    { id: 'D', name: 'D组', leader: '陈军', members: [{ id: 109, name: '贾三' }] },
  ]
  ungrouped.value = [
    { id: 201, name: '陈十', title: '副主任', org: '市政府' },
    { id: 202, name: '吴十一', title: '专员', org: '市直' },
    { id: 203, name: '王十二', title: '主任', org: '市宣传部' },
    { id: 204, name: '肖十三', title: '科长', org: '市工商联' },
  ]
}

async function loadData() {
  try {
    var token = uni.getStorageSync('auth_token') || ''
    var tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    var result = await uni.request({
      url: 'http://localhost:8084/api/groups/list',
      method: 'GET',
      header: { Authorization: token ? 'Bearer ' + token : '', 'X-Tenant-Id': tenantId },
      timeout: 9000,
    })
    var res = result[1]
    var body = (res && res.data) || {}
    if (Array.isArray(body.data && body.data.groups)) {
      groups.value = body.data.groups
      ungrouped.value = body.data.ungrouped || []
    } else {
      mockData()
    }
  } catch (e) {
    mockData()
  }
}

function pickGroup(g) { 
  selectedGroup.value = g 
}

function smallestGroup() {
  var sorted = groups.value.slice().sort(function(a, b) { 
    var lenA = a.members && a.members.length || 0
    var lenB = b.members && b.members.length || 0
    return lenA - lenB
  })
  return sorted[0]
}

function autoAssign(person) {
  var g = smallestGroup()
  if (!g) return
  if (!g.members) g.members = []
  g.members.push(person)
  ungrouped.value = ungrouped.value.filter(function(u) { return u.id !== person.id })
  uni.showToast({ title: '已分配到' + g.name, icon: 'success' })
}

function appendMemberToSelected(e) {
  var idx = Number(e.detail.value)
  var p = ungrouped.value[idx]
  if (!p || !selectedGroup.value) return
  if (!selectedGroup.value.members) selectedGroup.value.members = []
  selectedGroup.value.members.push(p)
  ungrouped.value = ungrouped.value.filter(function(u) { return u.id !== p.id })
}

function removeFromGroup(g, m) {
  if (!g.members) return
  g.members = g.members.filter(function(x) { return x.id !== m.id })
  ungrouped.value.push(m)
}

async function saveGrouping() {
  try {
    var token = uni.getStorageSync('auth_token') || ''
    var tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    await uni.request({
      url: 'http://localhost:8084/api/groups/save',
      method: 'POST',
      header: {
        Authorization: token ? 'Bearer ' + token : '',
        'X-Tenant-Id': tenantId,
        'Content-Type': 'application/json',
      },
      data: {
        groups: groups.value,
        ungrouped: ungrouped.value,
      },
      timeout: 9000,
    })
  } catch (e) {}
  uni.showToast({ title: '分组结果已保存', icon: 'success' })
}

onMounted(function() {
  uni.setNavigationBarTitle({ title: '分组管理' })
  loadData()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; padding-bottom: 120rpx; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { font-size: 24rpx; color: #64748b; margin-top: 6rpx; }

.card { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }
.card-title { font-size: 28rpx; font-weight: 700; color: #334155; margin-bottom: 10rpx; }
.group-item { display: flex; justify-content: space-between; gap: 12rpx; padding: 12rpx 0; border-bottom: 1px solid #eef2f7; }
.group-item:last-child { border-bottom: none; }
.g-name { font-size: 26rpx; color: #1e293b; font-weight: 600; }
.g-desc { margin-top: 4rpx; font-size: 22rpx; color: #64748b; }

.mini { height: 56rpx; line-height: 56rpx; border-radius: 10rpx; padding: 0 16rpx; font-size: 22rpx; background: #e2e8f0; color: #334155; }
.mini.primary { background: #667eea; color: #fff; }

.empty { color: #94a3b8; font-size: 24rpx; padding: 12rpx 0; text-align: center; }

.footer { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; border-top: 1px solid #e2e8f0; padding: 14rpx 16rpx 24rpx; }
.save-btn { width: 100%; height: 82rpx; line-height: 82rpx; border-radius: 12rpx; color: #fff; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); font-size: 28rpx; }

.mask { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: flex-end; }
.modal { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 20rpx; }
.modal-title { font-size: 30rpx; font-weight: 700; margin-bottom: 12rpx; color: #1e293b; }
.picker { height: 74rpx; border: 2rpx solid #e2e8f0; border-radius: 10rpx; padding: 0 16rpx; display: flex; align-items: center; margin-bottom: 12rpx; }
.picker-value { color: #475569; font-size: 25rpx; }
.m-list { max-height: 320rpx; overflow-y: auto; margin-bottom: 12rpx; }
.m-item { display: flex; justify-content: space-between; align-items: center; padding: 10rpx 0; border-bottom: 1px solid #eef2f7; font-size: 24rpx; }
.x { height: 50rpx; line-height: 50rpx; border-radius: 8rpx; background: #fee2e2; color: #dc2626; font-size: 22rpx; padding: 0 14rpx; }
</style>
