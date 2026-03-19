<template>
  <view class="container">
    <!-- Tabs -->
    <view class="tabs">
      <view v-for="tab in tabs" :key="tab" class="tab-card" :class="{ active: activeTab === tab }" @click="activeTab = tab">
        <text class="tab-icon">{{ tabIcons[tab] }}</text>
        <text class="tab-label">{{ tabLabels[tab] }}</text>
      </view>
    </view>

    <!-- Tab Content -->
    <scroll-view scroll-y class="scroll-content">
      <!-- Group Chat Tab -->
      <view v-show="activeTab === 'group'" class="tab-content">
        <view class="card">
          <view class="message-list">
            <view v-for="group in groupChats" :key="group.id" class="message-item" @click="openChat(group, 'group')">
              <text class="item-icon">👥</text>
              <view class="item-content">
                <text class="item-title">{{ group.title }}</text>
                <text class="item-desc">{{ group.memberCount }} 名成员</text>
              </view>
              <view v-if="group.unread > 0" class="badge">{{ group.unread }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- Private Chat Tab -->
      <view v-show="activeTab === 'private'" class="tab-content">
        <view class="card">
          <view class="message-list">
            <view v-for="contact in privateChats" :key="contact.id" class="message-item" @click="openChat(contact, 'private')">
              <text class="item-icon">👤</text>
              <view class="item-content">
                <text class="item-title">{{ contact.name }}</text>
                <text class="item-desc">{{ contact.role }}</text>
              </view>
              <view v-if="contact.unread > 0" class="badge">{{ contact.unread }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- System Tab -->
      <view v-show="activeTab === 'system'" class="tab-content">
        <view class="card">
          <view class="message-list">
            <view v-for="sys in systemMessages" :key="sys.id" class="message-item" @click="openSystemMsg(sys)">
              <text class="item-icon">🔔</text>
              <view class="item-content">
                <text class="item-title">{{ sys.title }}</text>
                <text class="item-desc">{{ sys.time }}</text>
              </view>
              <view v-if="!sys.read" class="badge-dot"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const activeTab = ref('group');

const tabs = ['group', 'private', 'system'];

const tabLabels = {
  group: '群聊',
  private: '私聊',
  system: '系统'
};

const tabIcons = {
  group: '👥',
  private: '👤',
  system: '🔔'
};

const groupChats = ref([
  { id: 1, title: '2025党务干部培训班 · 工作人员群', memberCount: 12, unread: 3 },
  { id: 2, title: '2025党务干部培训班 · 全体学员群', memberCount: 142, unread: 4 },
  { id: 3, title: '2025党务干部培训班 · 第一学习小组', memberCount: 28, unread: 2 }
]);

const privateChats = ref([
  { id: 1, name: '张主任', role: '管理员', unread: 1 },
  { id: 2, name: '李敏', role: '第一组组长', unread: 0 }
]);

const systemMessages = ref([
  { id: 1, title: '签到提醒', time: '今天 09:00', read: true },
  { id: 2, title: '课程调整通知', time: '今天 08:30', read: true },
  { id: 3, title: '天气预警', time: '昨天 18:00', read: false }
]);

const openChat = (item, type) => {
  if (type === 'group') {
    uni.navigateTo({ url: '/pages/learner/chat-room/chat-room?groupId=' + item.id });
  } else {
    uni.navigateTo({ url: '/pages/learner/chat-private/chat-private?contactId=' + item.id });
  }
};

const openSystemMsg = (msg) => {
  uni.showModal({
    title: msg.title,
    content: '消息时间：' + msg.time,
    showCancel: false
  });
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '沟通' });
  loadChats();
});

const loadChats = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/communication/list', method: 'GET' });
    if (res[1]?.data) {
      groupChats.value = res[1].data.groups || groupChats.value;
      privateChats.value = res[1].data.private || privateChats.value;
    }
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

.tabs {
  display: flex;
  gap: 8px;
  padding: 16px;
  background: white;
  overflow-x: auto;
  border-bottom: 1px solid #e2e8f0;
}

.tab-card {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-card.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-icon {
  font-size: 16px;
}

.tab-label {
  font-size: 14px;
  font-weight: 500;
}

.scroll-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: background 0.2s ease;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:active {
  background: #f8fafc;
}

.item-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.item-desc {
  font-size: 12px;
  color: #94a3b8;
}

.badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #ef4444;
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.badge-dot {
  width: 12px;
  height: 12px;
  background: #ef4444;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
