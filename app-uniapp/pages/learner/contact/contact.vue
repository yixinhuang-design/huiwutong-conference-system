<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
    </view>
    <view class="search-box">
      <text class="search-icon">🔍</text>
      <input v-model="searchText" type="text" placeholder="求人 / 单位 / 职务" class="search-input" @input="filterContacts" />
    </view>
    <scroll-view scroll-y class="scroll-content">
      <!-- Teachers -->
      <view class="card">
        <view class="section-title">班主任 / 工作人员</view>
        <view class="contact-list">
          <view v-for="contact in filteredTeachers" :key="contact.id" class="contact-item">
            <text class="contact-avatar" :class="'avatar-' + contact.color">{{ contact.initials }}</text>
            <view class="contact-info">
              <text class="contact-name">{{ contact.name }} · {{ contact.role }}</text>
              <text class="contact-duty">{{ contact.duty }}</text>
              <text class="contact-phone">{{ contact.phone }}</text>
            </view>
            <button class="msg-btn" @click="messageContact(contact)">💬</button>
          </view>
        </view>
      </view>

      <!-- Classmates -->
      <view class="card">
        <view class="section-title">同学通讯录</view>
        <view class="contact-list">
          <view v-for="contact in filteredClassmates" :key="contact.id" class="contact-item">
            <text class="contact-avatar" :class="'avatar-' + contact.color">{{ contact.initials }}</text>
            <view class="contact-info">
              <text class="contact-name">{{ contact.name }} · {{ contact.role }}</text>
              <text class="contact-phone">{{ contact.phone || '暂无手机号' }}</text>
            </view>
            <button class="msg-btn" @click="messageContact(contact)">💬</button>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const meeting = ref({
  id: '1',
  title: '2025党务干部培训班'
});

const searchText = ref('');

const teachers = ref([
  { id: 1, name: '李敏', role: '班主任', duty: '学员管理与答疑', phone: '138****6250', color: 'blue', initials: 'L' },
  { id: 2, name: '王磊', role: '会务协调', duty: '签到/座位', phone: '139****0825', color: 'green', initials: 'W' }
]);

const classmates = ref([
  { id: 3, name: '张伟', role: '市委组织部科员', phone: '137****9080', color: 'blue', initials: 'Z' },
  { id: 4, name: '刘婷', role: '市直机关党务干事', phone: '暂无手机号', color: 'purple', initials: 'L' },
  { id: 5, name: '果探', role: '区选组公务员', phone: '138****1234', color: 'orange', initials: 'G' }
]);

const filteredTeachers = computed(() => {
  if (!searchText.value) return teachers.value;
  const q = searchText.value.toLowerCase();
  return teachers.value.filter(t => 
    t.name.toLowerCase().includes(q) || 
    t.role.toLowerCase().includes(q) ||
    t.duty.toLowerCase().includes(q)
  );
});

const filteredClassmates = computed(() => {
  if (!searchText.value) return classmates.value;
  const q = searchText.value.toLowerCase();
  return classmates.value.filter(c => 
    c.name.toLowerCase().includes(q) || 
    c.role.toLowerCase().includes(q)
  );
});

const filterContacts = () => {
  // Computed property handles filtering
};

const messageContact = (contact) => {
  uni.navigateTo({ url: `/pages/learner/chat-private/chat-private?contactId=${contact.id}` });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '通讯录' });
  loadContacts();
});

const loadContacts = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/contacts', method: 'GET' });
    if (res[1]?.data) {
      teachers.value = res[1].data.teachers || teachers.value;
      classmates.value = res[1].data.classmates || classmates.value;
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

.search-box {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin: 8px 12px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.search-icon {
  margin-right: 8px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #475569;
}

.scroll-content {
  flex: 1;
  padding: 12px;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #64748b;
  margin-bottom: 12px;
  display: block;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;
}

.contact-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
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

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.contact-duty {
  font-size: 12px;
  color: #64748b;
}

.contact-phone {
  font-size: 12px;
  color: #94a3b8;
}

.msg-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  flex-shrink: 0;
}
</style>
