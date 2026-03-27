<!--
  虚拟列表组件
  用于优化长列表渲染性能
-->
<template>
  <scroll-view
    :class="['virtual-list', customClass]"
    :style="scrollViewStyle"
    scroll-y
    :scroll-top="scrollTop"
    @scroll="handleScroll"
    @scrolltoupper="handleToUpper"
    @scrolltolower="handleToLower"
  >

    <!-- 占位容器 -->
    <view class="virtual-list-phantom" :style="{ height: totalHeight + 'px' }"></view>

    <!-- 可见内容 -->
    <view class="virtual-list-content" :style="{ transform: `translateY(${offset}px)` }">
      <view
        v-for="(item, index) in visibleData"
        :key="itemKey ? item[itemKey] : index"
        :class="['virtual-list-item', itemClass]"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item" :index="startIndex + index"></slot>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="virtual-list-loading">
      <text>加载中...</text>
    </view>

    <!-- 加载完成 -->
    <view v-if="finished && visibleData.length > 0" class="virtual-list-finished">
      <text>已加载全部</text>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && visibleData.length === 0" class="virtual-list-empty">
      <slot name="empty">
        <text>暂无数据</text>
      </slot>
    </view>
  </scroll-view>
</template>

<script>
import { getVisibleData, throttle } from '@/utils/performance'

export default {
  name: 'VirtualList',
  props: {
    // 所有数据
    data: {
      type: Array,
      default: () => []
    },
    // 列表项高度
    itemHeight: {
      type: Number,
      default: 50
    },
    // 容器高度
    containerHeight: {
      type: Number,
      default: 600
    },
    // 缓冲区数量
    bufferCount: {
      type: Number,
      default: 5
    },
    // 数据的唯一标识字段
    itemKey: {
      type: String,
      default: ''
    },
    // 自定义类名
    customClass: {
      type: String,
      default: ''
    },
    // 列表项类名
    itemClass: {
      type: String,
      default: ''
    },
    // 是否正在加载
    loading: {
      type: Boolean,
      default: false
    },
    // 是否加载完成
    finished: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      scrollTop: 0,
      visibleData: [],
      startIndex: 0,
      endIndex: 0,
      offset: 0,
      totalHeight: 0
    }
  },
  computed: {
    scrollViewStyle() {
      return {
        height: this.containerHeight + 'px'
      }
    }
  },
  watch: {
    data: {
      handler() {
        this.updateVisibleData()
      },
      immediate: true
    }
  },
  created() {
    // 节流处理滚动事件
    this.handleScrollThrottled = throttle(this.handleScrollInternal, 16)
  },
  methods: {
    /**
     * 处理滚动
     */
    handleScroll(e) {
      this.handleScrollThrottled(e)
    },

    /**
     * 内部滚动处理
     */
    handleScrollInternal(e) {
      this.scrollTop = e.detail.scrollTop
      this.updateVisibleData()
    },

    /**
     * 更新可见数据
     */
    updateVisibleData() {
      const result = getVisibleData({
        allData: this.data,
        itemHeight: this.itemHeight,
        containerHeight: this.containerHeight,
        scrollTop: this.scrollTop,
        bufferCount: this.bufferCount
      })

      this.visibleData = result.visibleData
      this.startIndex = result.startIndex
      this.endIndex = result.endIndex
      this.offset = result.offset
      this.totalHeight = result.totalHeight
    },

    /**
     * 滚动到顶部
     */
    handleToUpper() {
      this.$emit('scrolltoupper')
    },

    /**
     * 滚动到底部
     */
    handleToLower() {
      if (!this.loading && !this.finished) {
        this.$emit('scrolltolower')
      }
    },

    /**
     * 滚动到指定位置
     */
    scrollTo(index) {
      this.scrollTop = index * this.itemHeight
    }
  }
}
</script>

<style lang="scss" scoped>
.virtual-list {
  position: relative;
  width: 100%;
  overflow: hidden;

  .virtual-list-phantom {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: -1;
  }

  .virtual-list-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    will-change: transform;

    .virtual-list-item {
      width: 100%;
      box-sizing: border-box;
      overflow: hidden;
    }
  }

  .virtual-list-loading,
  .virtual-list-finished,
  .virtual-list-empty {
    padding: 20px;
    text-align: center;
    color: #999;
    font-size: 14px;
  }
}
</style>
