<template>
  <view class="page">
    <view class="search-card">
      <view class="title">帮助中心</view>
      <input v-model="keyword" class="search-input" placeholder="搜索问题..." placeholder-class="placeholder" />
    </view>

    <view class="section-card">
      <view class="section-title">热门问题</view>
      <view
        v-for="item in filteredFaqs"
        :key="item.id"
        class="faq-item"
        @click="toggleFaq(item.id)"
      >
        <view class="faq-q">{{ item.question }}</view>
        <view v-if="openIds.includes(item.id)" class="faq-a">
          <view v-for="(line, idx) in item.answer" :key="idx" class="answer-line">• {{ line }}</view>
          <button v-if="item.actionText" class="mini-btn" @click.stop="openPage(item.actionUrl)">{{ item.actionText }}</button>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-title">使用指南</view>
      <view v-for="guide in guides" :key="guide.id" class="guide-item" @click="openGuide(guide)">
        <view class="guide-name">{{ guide.title }}</view>
        <view class="guide-meta">{{ guide.desc }} · {{ guide.minutes }}分钟</view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-title">仍需帮助？</view>
      <view class="support-item" @click="callSupport">客服电话：400-800-2025</view>
      <view class="support-item" @click="openChat">在线客服（工作日 08:30-18:00）</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';

const keyword = ref('');
const openIds = ref([1]);

const faqs = ref([
  {
    id: 1,
    question: '如何查看我的座位安排？',
    answer: ['首页进入“座位图”查看全场布局', '可使用“扫码查座”快速定位', '在日程页面可查看个人座位信息'],
    actionText: '查看座位',
    actionUrl: '/pages/learner/seat/seat'
  },
  {
    id: 2,
    question: '如何完成会议签到？',
    answer: ['支持现场签到和扫码签到两种方式', '签到失败可联系工作人员补签', '签到结果会同步到个人任务清单'],
    actionText: '立即签到',
    actionUrl: '/pages/learner/checkin/checkin'
  },
  {
    id: 3,
    question: '如何查看我的任务？',
    answer: ['首页显示任务提醒卡片', '任务列表支持按状态筛选', '超时任务会收到消息提醒'],
    actionText: '查看任务',
    actionUrl: '/pages/learner/task-list/task-list'
  },
  {
    id: 4,
    question: '如何联系其他参会人员？',
    answer: ['在分组页面查看同组成员', '在通讯录中搜索联系人', '可发起群聊或私聊'],
    actionText: '查看分组',
    actionUrl: '/pages/learner/groups/groups'
  }
]);

const guides = ref([
  { id: 'quick', title: '快速入门', desc: '5分钟掌握核心功能', minutes: 5 },
  { id: 'schedule', title: '日程管理', desc: '查看与提醒日程', minutes: 3 },
  { id: 'task', title: '任务完成', desc: '任务提交与状态跟踪', minutes: 4 },
  { id: 'comm', title: '沟通协作', desc: '聊天、分组、通讯录', minutes: 4 }
]);

const filteredFaqs = computed(() => {
  if (!keyword.value.trim()) return faqs.value;
  return faqs.value.filter((item) => item.question.includes(keyword.value.trim()));
});

uni.setNavigationBarTitle({ title: '帮助中心' });

const toggleFaq = (id) => {
  if (openIds.value.includes(id)) {
    openIds.value = openIds.value.filter((v) => v !== id);
  } else {
    openIds.value = [...openIds.value, id];
  }
};

const openPage = (url) => {
  uni.navigateTo({ url });
};

const openGuide = (guide) => {
  uni.showToast({ title: `打开${guide.title}`, icon: 'none' });
};

const callSupport = () => {
  uni.makePhoneCall({ phoneNumber: '4008002025' });
};

const openChat = () => {
  uni.navigateTo({ url: '/pages/common/communication/communication' });
};
</script>

<style scoped lang="scss">
.page { padding: 24rpx; background: #f5f7fb; min-height: 100vh; }
.search-card, .section-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.title { font-size: 34rpx; font-weight: 700; margin-bottom: 16rpx; color: #1e293b; }
.search-input { background: #f1f5f9; border-radius: 10rpx; padding: 16rpx; font-size: 26rpx; }
.placeholder { color: #94a3b8; }
.section-title { font-size: 30rpx; font-weight: 700; margin-bottom: 14rpx; color: #1e293b; }
.faq-item { border-top: 1rpx solid #e2e8f0; padding: 16rpx 0; }
.faq-q { font-size: 27rpx; color: #0f172a; font-weight: 600; }
.faq-a { margin-top: 10rpx; color: #475569; font-size: 25rpx; }
.answer-line { margin-bottom: 6rpx; }
.mini-btn { margin-top: 8rpx; font-size: 22rpx; color: #2563eb; background: #eff6ff; border: none; border-radius: 8rpx; padding: 8rpx 12rpx; }
.guide-item { border-top: 1rpx solid #e2e8f0; padding: 14rpx 0; }
.guide-name { font-size: 27rpx; font-weight: 600; color: #1e293b; }
.guide-meta { font-size: 24rpx; color: #64748b; margin-top: 4rpx; }
.support-item { border-top: 1rpx solid #e2e8f0; padding: 14rpx 0; font-size: 26rpx; color: #2563eb; }
</style>
