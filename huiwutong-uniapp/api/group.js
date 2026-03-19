/**
 * 群组管理 & 聊天API
 * 对接协同服务 (端口8089)
 */

import { get, post, put, del } from './request'

// 协同服务基础地址
const COLLAB_BASE = { baseUrl: 'http://localhost:8089' }

export default {
  // ============ 群组管理 ============
  /** 创建群组 */
  createGroup(data) {
    return post('/api/group/create', data, COLLAB_BASE)
  },
  /** 群组列表 */
  listGroups(params) {
    return get('/api/group/list', params, COLLAB_BASE)
  },
  /** 我的群组 */
  myGroups(params) {
    return get('/api/group/my-groups', params, COLLAB_BASE)
  },
  /** 群组详情 */
  groupDetail(groupId) {
    return get(`/api/group/${groupId}`, {}, COLLAB_BASE)
  },
  /** 群成员列表 */
  getMembers(groupId) {
    return get(`/api/group/${groupId}/members`, {}, COLLAB_BASE)
  },
  /** 添加群成员 */
  addMember(groupId, data) {
    return post(`/api/group/${groupId}/members`, data, COLLAB_BASE)
  },
  /** 批量添加群成员 */
  batchAddMembers(groupId, members) {
    return post(`/api/group/${groupId}/members/batch`, members, COLLAB_BASE)
  },
  /** 移除群成员 */
  removeMember(groupId, userId) {
    return del(`/api/group/${groupId}/members/${userId}`, {}, COLLAB_BASE)
  },
  /** 更新群设置 */
  updateSettings(groupId, data) {
    return put(`/api/group/${groupId}/settings`, data, COLLAB_BASE)
  },
  /** 删除群组 */
  deleteGroup(groupId) {
    return del(`/api/group/${groupId}`, {}, COLLAB_BASE)
  },
  /** 群组统计 */
  groupStats(params) {
    return get('/api/group/stats', params, COLLAB_BASE)
  },

  // ============ 聊天消息 ============
  /** 发送消息(HTTP) */
  sendMessage(data) {
    return post('/api/chat/send', data, COLLAB_BASE)
  },
  /** 群组聊天记录 */
  getGroupMessages(groupId, params) {
    return get(`/api/chat/group/${groupId}/messages`, params, COLLAB_BASE)
  },
  /** 私聊记录 */
  getPrivateMessages(params) {
    return get('/api/chat/private/messages', params, COLLAB_BASE)
  },
  /** 搜索消息 */
  searchMessages(groupId, keyword) {
    return get(`/api/chat/group/${groupId}/search`, { keyword }, COLLAB_BASE)
  },
  /** 消息统计 */
  messageStats(groupId) {
    return get(`/api/chat/group/${groupId}/stats`, {}, COLLAB_BASE)
  }
}
