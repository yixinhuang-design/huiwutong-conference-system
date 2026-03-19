<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <view class="card">
        <view class="section" v-for="(section, idx) in sections" :key="idx">
          <text class="section-title">{{ section.title }}</text>
          <view class="content">
            <text v-for="(item, i) in section.items" :key="i" class="content-item">{{ item }}</text>
            <text v-if="section.note" class="section-note">{{ section.note }}</text>
          </view>
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

const sections = ref([
  {
    title: '一、报到流程',
    items: [
      '请于指定时间到达报到点，出示报名二维码或身份证。',
      '领取资料包、房卡、胸牌等物品。',
      '完成签到后可前往住宿酒店办理入住。'
    ]
  },
  {
    title: '二、会议日程',
    items: [
      '请关注“日程”页面，按时参加各项培训和活动。',
      '如有变动，将通过通知及时告知。'
    ]
  },
  {
    title: '三、纪律要求',
    items: [
      '请遵守培训纪律，按时签到，不得无故缺年。',
      '手机请调至静音，保持课堂秩序。'
    ]
  },
  {
    title: '四、住宿与用餐',
    items: [
      '凭房卡入住，贵重物品请妥善保管。',
      '用餐时间及地点详见“日程”页面。'
    ]
  },
  {
    title: '五、资料与作業',
    items: [
      '所有学习资料可在“资料下载”页面获取。',
      '如有作業/心得体会，请按要求及时提交。'
    ]
  },
  {
    title: '六、联系方式',
    items: [],
    note: '如需帮助，可在“通讯录”中找到班主任/工作人员并点击发消息联系。'
  }
]);

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '参会须知' });
  loadGuideData();
});

const loadGuideData = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/guide', method: 'GET' });
    if (res[1]?.data) sections.value = res[1].data;
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
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #1e293b;
}

.scroll-content {
  flex: 1;
  padding: 12px;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section {
  margin-bottom: 24px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
  display: block;
  margin-bottom: 12px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-item {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  padding-left: 12px;
}

.content-item::before {
  content: '• ';
  margin-right: 8px;
  color: #2563eb;
}

.section-note {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  display: block;
}
</style>
