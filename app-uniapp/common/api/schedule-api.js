/**
 * 日程管理API模块 - UniApp版本
 * 为learner和staff两端统一提供API调用接口
 */

// 配置基础URL（可根据环境调整）
const BASE_URL = 'http://localhost:8084/api/schedule'

/**
 * 获取认证Token
 */
const getAuthToken = () => {
  const token = uni.getStorageSync('token')
  return token ? `Bearer ${token}` : ''
}

/**
 * 统一请求方法
 */
const request = async (method, url, data = null) => {
  try {
    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: `${BASE_URL}${url}`,
        method,
        data,
        header: {
          'Content-Type': 'application/json',
          'Authorization': getAuthToken()
        },
        success: resolve,
        fail: reject
      })
    })

    // 处理响应
    if (response[1].statusCode === 200 || response[1].statusCode === 201) {
      const result = response[1].data
      if (result.code === 0 || result.success) {
        return result.data
      } else {
        throw new Error(result.message || '请求失败')
      }
    } else {
      throw new Error(`HTTP ${response[1].statusCode}: ${response[1].errMsg}`)
    }
  } catch (error) {
    console.error('API Error:', error)
    throw error
  }
}

// 导出API对象
const ScheduleAPI = {
  /**
   * 创建日程
   * @param {number} meetingId - 会议ID
   * @param {object} scheduleData - 日程数据
   */
  createSchedule: (meetingId, scheduleData) => {
    return request('POST', `/create?meetingId=${meetingId}`, scheduleData)
  },

  /**
   * 更新日程
   * @param {number} scheduleId - 日程ID
   * @param {object} scheduleData - 日程数据
   */
  updateSchedule: (scheduleId, scheduleData) => {
    return request('PUT', `/${scheduleId}`, scheduleData)
  },

  /**
   * 删除日程
   * @param {number} scheduleId - 日程ID
   */
  deleteSchedule: (scheduleId) => {
    return request('DELETE', `/${scheduleId}`)
  },

  /**
   * 获取日程详情
   * @param {number} scheduleId - 日程ID
   */
  getSchedule: (scheduleId) => {
    return request('GET', `/${scheduleId}`)
  },

  /**
   * 获取日程列表（分页）
   * @param {number} meetingId - 会议ID
   * @param {number} pageNo - 页码（默认1）
   * @param {number} pageSize - 每页数量（默认10）
   */
  listSchedules: (meetingId, pageNo = 1, pageSize = 10) => {
    return request('GET', `/list?meetingId=${meetingId}&pageNo=${pageNo}&pageSize=${pageSize}`)
  },

  /**
   * 获取全部日程（不分页）
   * @param {number} meetingId - 会议ID
   */
  allSchedules: (meetingId) => {
    return request('GET', `/all?meetingId=${meetingId}`)
  },

  /**
   * 获取需要签到的日程
   * @param {number} meetingId - 会议ID
   */
  getNeedCheckinSchedules: (meetingId) => {
    return request('GET', `/need-checkin?meetingId=${meetingId}`)
  },

  /**
   * 获取需要提醒的日程
   * @param {number} meetingId - 会议ID
   */
  getNeedReminderSchedules: (meetingId) => {
    return request('GET', `/need-reminder?meetingId=${meetingId}`)
  },

  /**
   * 获取进行中的日程
   * @param {number} meetingId - 会议ID
   */
  getOngoingSchedules: (meetingId) => {
    return request('GET', `/ongoing?meetingId=${meetingId}`)
  },

  /**
   * 获取即将开始的日程（30分钟内）
   * @param {number} meetingId - 会议ID
   */
  getUpcomingSchedules: (meetingId) => {
    return request('GET', `/upcoming?meetingId=${meetingId}`)
  },

  /**
   * 获取下一个日程
   * @param {number} meetingId - 会议ID
   */
  getNextSchedule: (meetingId) => {
    return request('GET', `/next?meetingId=${meetingId}`)
  },

  /**
   * 获取当前日程
   * @param {number} meetingId - 会议ID
   */
  getCurrentSchedule: (meetingId) => {
    return request('GET', `/current?meetingId=${meetingId}`)
  },

  /**
   * 发布日程
   * @param {number} scheduleId - 日程ID
   */
  publishSchedule: (scheduleId) => {
    return request('PUT', `/${scheduleId}/publish`)
  },

  /**
   * 取消日程
   * @param {number} scheduleId - 日程ID
   */
  cancelSchedule: (scheduleId) => {
    return request('PUT', `/${scheduleId}/cancel`)
  },

  /**
   * 复制日程
   * @param {number} scheduleId - 日程ID
   */
  duplicateSchedule: (scheduleId) => {
    return request('POST', `/${scheduleId}/duplicate`)
  },

  /**
   * 统计日程数量
   * @param {number} meetingId - 会议ID
   */
  countSchedules: (meetingId) => {
    return request('GET', `/count?meetingId=${meetingId}`)
  }
}

// 导出
export default ScheduleAPI
