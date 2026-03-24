<template>
  <view class="contact-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">通讯录</text>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar card">
      <view class="search-input-wrapper">
        <text class="search-icon"><text class="fa fa-search"></text></text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索姓名或单位"
          @input="handleSearch"
        />
      </view>
    </view>

    <!-- 索引列表 -->
    <view class="contact-list">
      <view
        v-for="(group, key) in groupedContacts"
        :key="key"
        class="contact-group"
      >
        <view class="group-index">{{ key }}</view>
        <view
          v-for="(contact, index) in group"
          :key="index"
          class="contact-item list-item"
          @click="showContactDetail(contact)"
        >
          <view class="contact-avatar">{{ contact.avatar }}</view>
          <view class="contact-info">
            <view class="contact-name">{{ contact.name }}</view>
            <view class="contact-org">{{ contact.organization }}</view>
          </view>
          <view class="contact-actions">
            <text class="action-icon" @click.stop="callContact(contact)"><text class="fa fa-phone"></text></text>
            <text class="action-icon" @click.stop="chatContact(contact)"><text class="fa fa-comments"></text></text>
          </view>
        </view>
      </view>

      <view v-if="contactList.length === 0" class="empty-state">
        <text class="empty-icon"><text class="fa fa-address-book"></text></text>
        <text class="empty-text">暂无联系人</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      searchKeyword: '',
      contactList: [
        {
          id: 1,
          name: '丁丽华',
          pinyin: 'dinglihua',
          organization: '市委组织部',
          position: '副处长',
          phone: '138****1234'
        },
        {
          id: 2,
          name: '王小明',
          pinyin: 'wangxiaoming',
          organization: '市委宣传部',
          position: '科员',
          phone: '139****5678'
        },
        {
          id: 3,
          name: '李建国',
          pinyin: 'lijianguo',
          organization: '市委统战部',
          position: '主任',
          phone: '137****9012'
        },
        {
          id: 4,
          name: '张思思',
          pinyin: 'zhangsisi',
          organization: '市教育局',
          position: '科长',
          phone: '136****3456'
        },
        {
          id: 5,
          name: '陈伟',
          pinyin: 'chenwei',
          organization: '市财政局',
          position: '副处长',
          phone: '135****7890'
        },
        {
          id: 6,
          name: '刘洋',
          pinyin: 'liuyang',
          organization: '市公安局',
          position: '科员',
          phone: '134****2345'
        },
        {
          id: 7,
          name: '赵敏',
          pinyin: 'zhaomin',
          organization: '市卫健委',
          position: '主任',
          phone: '133****6789'
        },
        {
          id: 8,
          name: '孙强',
          pinyin: 'sunqiang',
          organization: '市发改委',
          position: '副科长',
          phone: '132****0123'
        }
      ]
    }
  },

  computed: {
    avatarText() {
      return (contact) => {
        return contact.name ? contact.name.charAt(contact.name.length - 1) : '学'
      }
    },

    groupedContacts() {
      const groups = {}
      const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

      this.contactList.forEach(contact => {
        const letter = contact.pinyin ? contact.pinyin.charAt(0).toUpperCase() : '#'
        if (!groups[letter]) {
          groups[letter] = []
        }
        groups[letter].push(contact)
      })

      return groups
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadContacts()
    }
  },

  methods: {
    /**
     * 加载通讯录
     */
    async loadContacts() {
      try {
        // TODO: 调用API获取通讯录
        // const res = await this.$api.meeting.getContacts(this.meetingId)
      } catch (error) {
        console.error('Load contacts error:', error)
      }
    },

    /**
     * 搜索联系人
     */
    handleSearch() {
      const keyword = this.searchKeyword.toLowerCase().trim()

      if (!keyword) {
        this.loadContacts()
        return
      }

      // 简单的前端搜索
      this.contactList = this.contactList.filter(contact => {
        return contact.name.toLowerCase().includes(keyword) ||
               contact.pinyin.toLowerCase().includes(keyword) ||
               contact.organization.toLowerCase().includes(keyword)
      })
    },

    /**
     * 显示联系人详情
     */
    showContactDetail(contact) {
      uni.showModal({
        title: contact.name,
        content: `单位：${contact.organization}\n职务：${contact.position}\n电话：${contact.phone}`,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '拨打',
        success: (res) => {
          if (res.confirm) {
            this.callContact(contact)
          }
        }
      })
    },

    /**
     * 拨打电话
     */
    callContact(contact) {
      uni.makePhoneCall({
        phoneNumber: contact.phone,
        fail: () => {
          uni.showToast({
            title: '拨号失败',
            icon: 'none'
          })
        }
      })
    },

    /**
     * 发起聊天
     */
    chatContact(contact) {
      uni.navigateTo({
        url: `/pages/learner/chat-private?id=${contact.id}&name=${contact.name}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.contact-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.search-bar {
  margin: $spacing-md;
  padding: 0;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.search-icon {
  font-size: $font-size-lg;
}

.search-input {
  flex: 1;
  font-size: $font-size-md;
}

.contact-list {
  padding-bottom: $spacing-md;
}

.contact-group {
  margin-bottom: $spacing-lg;
}

.group-index {
  padding: $spacing-md $spacing-md $spacing-sm;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;
  background: $bg-secondary;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  margin: 0 $spacing-md $spacing-sm;
  border-radius: $border-radius-md;
  transition: $transition-base;
}

.contact-item:active {
  transform: scale(0.98);
  background: $bg-tertiary;
}

.contact-avatar {
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
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
}

.contact-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.contact-org {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.contact-actions {
  display: flex;
  gap: $spacing-md;
  flex-shrink: 0;
}

.action-icon {
  font-size: 40rpx;
  padding: $spacing-sm;
}
</style>
