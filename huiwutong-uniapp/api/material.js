/**
 * 资料管理API
 * 对接协同服务 (端口8089)
 */

import { get, post, put, del } from './request'

// 协同服务基础地址
const COLLAB_BASE = { baseUrl: 'http://localhost:8089' }

export default {
  /** 上传资料 */
  upload(data) {
    return post('/api/material/upload', data, COLLAB_BASE)
  },
  /** 按群组获取资料 */
  listByGroup(groupId, params) {
    return get(`/api/material/group/${groupId}`, params, COLLAB_BASE)
  },
  /** 按会议获取资料 */
  list(params) {
    return get('/api/material/list', params, COLLAB_BASE)
  },
  /** 资料详情 */
  detail(materialId) {
    return get(`/api/material/${materialId}`, {}, COLLAB_BASE)
  },
  /** 删除资料 */
  remove(materialId) {
    return del(`/api/material/${materialId}`, {}, COLLAB_BASE)
  },
  /** 下载资料 */
  download(materialId) {
    return post(`/api/material/${materialId}/download`, {}, COLLAB_BASE)
  },
  /** 搜索资料 */
  search(params) {
    return get('/api/material/search', params, COLLAB_BASE)
  },
  /** 资料统计 */
  stats(params) {
    return get('/api/material/stats', params, COLLAB_BASE)
  }
}
