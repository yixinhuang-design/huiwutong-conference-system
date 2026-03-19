/**
 * 租户管理API
 * 对接认证服务 - 租户管理模块 (端口8081)
 */

import { get, post, put, del } from './request'

export default {
  /**
   * 获取租户列表
   * GET /tenant/list
   */
  list(params) {
    return get('/tenant/list', params)
  },

  /**
   * 获取租户详情
   * GET /tenant/{tenantId}
   */
  getDetail(tenantId) {
    return get(`/tenant/${tenantId}`)
  },

  /**
   * 创建租户
   * POST /tenant/create
   */
  create(data) {
    return post('/tenant/create', data)
  },

  /**
   * 更新租户
   * PUT /tenant/{tenantId}
   */
  update(tenantId, data) {
    return put(`/tenant/${tenantId}`, data)
  },

  /**
   * 删除租户
   * DELETE /tenant/{tenantId}
   */
  delete(tenantId) {
    return del(`/tenant/${tenantId}`)
  },

  /**
   * 切换租户状态
   * PUT /tenant/{tenantId}/status?status={status}
   */
  toggleStatus(tenantId, status) {
    return put(`/tenant/${tenantId}/status?status=${status}`)
  },

  /**
   * 获取租户统计
   * GET /tenant/{tenantId}/stats
   */
  getStats(tenantId) {
    return get(`/tenant/${tenantId}/stats`)
  }
}
