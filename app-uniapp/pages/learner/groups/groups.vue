<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <view v-for="group in groups" :key="group.id" class="group-card">
        <view class="group-header">
          <text class="group-icon">{{ group.icon }}</text>
          <view class="group-info">
            <text class="group-title">{{ group.name }}</text>
            <text class="group-members">{{ group.memberCount }}名成员</text>
          </view>
        </view>
        <view class="members-section">
          <text class="members-label">群组成员:</text>
          <view class="member-grid">
            <view v-for="member in group.members" :key="member.id" class="member-item">
              <text class="member-avatar" :class="'avatar-' + member.color">{{ member.initials }}</text>
              <text class="member-name">{{ member.name }}</text>
            </view>
          </view>
        </view>
        <view class="group-actions">
          <button class="action-btn" @click="contactGroup(group)">💬 联系群组</button>
          <button class="action-btn-secondary" @click="viewGroupDetail(group)">→ 详情</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const meeting = ref({
  id: '1',
  title: '2025党务干部培训班'
});

const groups = ref([
  {
    id: 1,
    name: '第一学习小组',
    memberCount: 28,
    icon: '1️⃣',
    members: [
      { id: 1, name: '学员A', initials: 'A', color: 'blue' },
      { id: 2, name: '学员B', initials: 'B', color: 'green' },
      { id: 3, name: '学员C', initials: 'C', color: 'purple' },
      { id: 4, name: '学员D', initials: 'D', color: 'orange' }
    ]
  },
  {
    id: 2,
    name: '第二学习小组',
    memberCount: 26,
    icon: '2️⃣',
    members: [
      { id: 5, name: '学员E', initials: 'E', color: 'blue' },
      { id: 6, name: '学员F', initials: 'F', color: 'green' },
      { id: 7, name: '学员G', initials: 'G', color: 'purple' }
    ]
  },
  {
    id: 3,
    name: '第三学习小组',
    memberCount: 24,
    icon: '3️⃣',
    members: [
      { id: 8, name: '学员H', initials: 'H', color: 'blue' },
      { id: 9, name: '学员I', initials: 'I', color: 'green' }
    ]
  }
]);

const goBack = () => {
  uni.navigateBack();
};

const contactGroup = (group) => {
  uni.navigateTo({ url: `/pages/learner/chat-room/chat-room?groupId=${group.id}` });
};

const viewGroupDetail = (group) => {
  uni.showToast({
    title: '查看' + group.name + '详情',
    icon: 'none'
  });
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '学员群组' });
  loadGroupsData();
});

const loadGroupsData = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/groups', method: 'GET' });
    if (res[1]?.data) groups.value = res[1].data;
  } catch (e) {
    console.log('Using mock data');
  }
};
</script>

<style scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f7fb;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  flex: 1;
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.scroll-content {
  flex: 1;
  padding: 12px;
}

.group-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.group-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.group-icon {
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.group-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.group-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.group-members {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.members-section {
  margin: 16px 0;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.members-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  margin-bottom: 8px;
}

.member-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 0 0 calc(25% - 9px);
}

.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.avatar-blue {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.avatar-green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.avatar-purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.avatar-orange {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.member-name {
  font-size: 11px;
  color: #475569;
  text-align: center;
}

.group-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.action-btn {
  flex: 1;
  padding: 10px 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
}

.action-btn-secondary {
  flex: 1;
  padding: 10px 12px;
  background: #f8fafc;
  color: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 4px;
  font-size: 13px;
}
</style>
