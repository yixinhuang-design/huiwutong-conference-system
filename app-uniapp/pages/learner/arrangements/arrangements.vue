<template>
  <view class="page">
    <view class="header-card">
      <view class="title">辅助安排</view>
      <view class="tab-row">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.name }}
        </view>
      </view>
    </view>

    <view v-for="item in currentList" :key="item.id" class="arrange-card">
      <view class="arrange-name">{{ item.name }}</view>
      <view class="arrange-time">{{ item.time }}</view>
      <view v-for="(detail, idx) in item.details" :key="idx" class="arrange-detail">{{ detail }}</view>
      <button v-if="item.phone" class="call-btn" @click="call(item.phone)">一键拨号</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';

uni.setNavigationBarTitle({ title: '辅助安排' });

const tabs = ref([
  { key: 'vehicle', name: '车辆' },
  { key: 'hotel', name: '住宿' },
  { key: 'meal', name: '餐饮' },
  { key: 'discussion', name: '分组' }
]);

const activeTab = ref('vehicle');

const dataMap = ref({
  vehicle: [
    { id: 1, name: '1号大巴车', time: '1月10日 14:00', details: ['司机：李师傅'], phone: '13800000001' },
    { id: 2, name: '返程车辆', time: '1月13日 09:00', details: ['集合地点：酒店大堂'] }
  ],
  hotel: [
    { id: 3, name: '维也纳酒店', time: '入住期间', details: ['房间号：806', '同住：李四'], phone: '02088888888' }
  ],
  meal: [
    { id: 4, name: '1月10日 晚餐', time: '18:00-19:30', details: ['维也纳酒店自助餐厅'] },
    { id: 5, name: '1月11日 午餐', time: '12:00-13:30', details: ['越秀会议中心5F'] },
    { id: 6, name: '1月11日 晚宴', time: '18:00-20:00', details: ['宴会厅', '座位：1排2号桌'] }
  ],
  discussion: [
    { id: 7, name: '第一组讨论', time: '1月12日 14:00-16:00', details: ['地点：A栋302会议室', '主持人：张教授', '参与：第一组全体成员'] }
  ]
});

const currentList = computed(() => dataMap.value[activeTab.value] || []);

const call = (phone) => {
  uni.makePhoneCall({ phoneNumber: phone });
};
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx; background: #f5f7fb; }
.header-card, .arrange-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.title { font-size: 34rpx; font-weight: 700; color: #1e293b; margin-bottom: 14rpx; }
.tab-row { display: flex; gap: 10rpx; }
.tab-item { padding: 8rpx 16rpx; border-radius: 999rpx; font-size: 24rpx; background: #f1f5f9; color: #64748b; }
.tab-item.active { background: #2563eb; color: #fff; }
.arrange-name { font-size: 30rpx; font-weight: 700; color: #0f172a; }
.arrange-time { margin-top: 8rpx; color: #2563eb; font-size: 24rpx; }
.arrange-detail { margin-top: 8rpx; color: #475569; font-size: 25rpx; }
.call-btn { margin-top: 14rpx; background: #eff6ff; color: #2563eb; border: none; font-size: 24rpx; border-radius: 10rpx; }
</style>
