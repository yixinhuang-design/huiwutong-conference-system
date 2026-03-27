/**
 * 数据服务API
 * 对接数据服务 (端口8088)
 * 系统监控、日志、业务统计、数据大屏
 */

import { get, post } from './request'

export default {
  // ============ 系统监控 ============
  /**
   * 系统总览
   * GET /api/data/system/overview
   */
  getSystemOverview() {
    return get('/data/system/overview')
  },

  /**
   * CPU使用率
   * GET /api/data/system/cpu
   */
  getCpuUsage() {
    return get('/data/system/cpu')
  },

  /**
   * 内存使用率
   * GET /api/data/system/memory
   */
  getMemoryUsage() {
    return get('/data/system/memory')
  },

  /**
   * 磁盘使用率
   * GET /api/data/system/disk
   */
  getDiskUsage() {
    return get('/data/system/disk')
  },

  /**
   * 网络状态
   * GET /api/data/system/network
   */
  getNetworkStatus() {
    return get('/data/system/network')
  },

  /**
   * 高频端点
   * GET /api/data/system/top-endpoints
   */
  getTopEndpoints() {
    return get('/data/system/top-endpoints')
  },

  /**
   * 响应时间
   * GET /api/data/system/response-times
   */
  getResponseTimes() {
    return get('/data/system/response-times')
  },

  /**
   * 错误率
   * GET /api/data/system/error-rates
   */
  getErrorRates() {
    return get('/data/system/error-rates')
  },

  /**
   * 系统事件
   * GET /api/data/system/events
   */
  getSystemEvents(params) {
    return get('/data/system/events', params)
  },

  /**
   * 用户活动
   * GET /api/data/system/user-activities
   */
  getUserActivities(params) {
    return get('/data/system/user-activities', params)
  },

  // ============ 系统日志 ============
  /**
   * 日志列表
   * GET /api/data/logs?page=1&size=50
   */
  listLogs(params) {
    return get('/data/logs', params)
  },

  /**
   * 写入日志
   * POST /api/data/logs
   */
  writeLog(data) {
    return post('/data/logs', data)
  },

  // ============ 业务数据统计 ============
  /**
   * 业务总览
   * GET /api/data/business/overview?meetingId={id}
   */
  getBusinessOverview(meetingId) {
    return get('/data/business/overview', { meetingId: meetingId })
  },

  /**
   * 报名统计
   * GET /api/data/business/registration?meetingId={id}
   */
  getRegistrationStats(meetingId) {
    return get('/data/business/registration', { meetingId: meetingId })
  },

  /**
   * 签到统计
   * GET /api/data/business/checkin?meetingId={id}
   */
  getCheckinStats(meetingId) {
    return get('/data/business/checkin', { meetingId: meetingId })
  },

  /**
   * 通知统计
   * GET /api/data/business/notification?meetingId={id}
   */
  getNotificationStats(meetingId) {
    return get('/data/business/notification', { meetingId: meetingId })
  },

  /**
   * 任务统计
   * GET /api/data/business/task?meetingId={id}
   */
  getTaskStats(meetingId) {
    return get('/data/business/task', { meetingId: meetingId })
  },

  /**
   * 趋势数据
   * GET /api/data/business/trend?meetingId={id}
   */
  getTrendData(meetingId, params) {
    return get('/data/business/trend', {
      meetingId: meetingId,
      ...params
    })
  },

  /**
   * 横向对比
   * GET /api/data/business/comparison
   */
  getComparison(params) {
    return get('/data/business/comparison', params)
  },

  /**
   * 数据导出
   * GET /api/data/business/export?meetingId={id}&format=excel
   */
  exportBusinessData(meetingId, format = 'excel') {
    return get('/data/business/export', {
      meetingId: meetingId,
      format
    })
  },

  // ============ 数据大屏 ============
  /**
   * 大屏数据
   * GET /api/data/dashboard?meetingId={id}
   */
  getDashboardData(meetingId) {
    return get('/data/dashboard', { meetingId: meetingId })
  },

  /**
   * 预警数据
   * GET /api/data/warning?meetingId={id}
   */
  getWarningData(meetingId) {
    return get('/data/warning', { meetingId: meetingId })
  },

  /**
   * 排名数据
   * GET /api/data/ranking?meetingId={id}
   */
  getRankingData(meetingId) {
    return get('/data/ranking', { meetingId: meetingId })
  },

  // ============ 健康检查与API统计 ============
  /**
   * 各服务健康检查
   * GET /api/data/health/check
   */
  getHealthCheck() {
    return get('/data/health/check')
  },

  /**
   * 服务状态列表
   * GET /api/data/health/services
   */
  getServiceHealth() {
    return get('/data/health/services')
  },

  /**
   * API调用统计
   * GET /api/data/api/stats
   */
  getApiStats(params) {
    return get('/data/api/stats', params)
  },

  /**
   * 慢接口排名
   * GET /api/data/api/slow-endpoints
   */
  getSlowEndpoints(params) {
    return get('/data/api/slow-endpoints', params)
  }
}
