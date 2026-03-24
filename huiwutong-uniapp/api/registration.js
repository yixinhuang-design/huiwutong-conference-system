/**
 * 报名管理API
 * 对接报名服务 (端口8082)
 */

import { get, post, getBlob } from './request'

export default {
  /**
   * 提交报名
   * POST /registration/submit
   */
  submit(data) {
    return post('/registration/submit', data)
  },

  /**
   * 获取报名详情
   * GET /registration/{id}
   */
  getDetail(id) {
    return get(`/registration/${id}`)
  },

  /**
   * 分页查询报名列表
   * GET /registration/list
   */
  list(params) {
    return get('/registration/list', params)
  },

  /**
   * 根据手机号查询报名状态
   * GET /registration/query
   */
  queryByPhone(conferenceId, phone) {
    return get('/registration/query', {
      conferenceId,
      phone
    })
  },

  /**
   * 审核报名
   * POST /registration/audit
   */
  audit(data) {
    return post('/registration/audit', data)
  },

  /**
   * 批量审核报名
   * POST /registration/batchAudit
   */
  batchAudit(data) {
    return post('/registration/batchAudit', data)
  },

  /**
   * 导出报名名册（Excel）
   * GET /registration/export
   */
  exportList(conferenceId) {
    return getBlob(`/registration/export?conferenceId=${conferenceId}`)
  },

  /**
   * 获取报名统计
   * GET /registration/stats
   */
  getStats(conferenceId) {
    return get('/registration/stats', { conferenceId })
  },

  /**
   * OCR识别身份证
   * POST /registration/ocr/idCard
   */
  ocrIdCard(photoUrl) {
    return post('/registration/ocr/idCard', { photoUrl })
  },

  /**
   * 上传报名附件
   * POST /registration/upload
   */
  upload(conferenceId, fileType, filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/registration/upload',
        filePath,
        name: 'file',
        formData: {
          conferenceId,
          fileType
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } else {
            reject(res)
          }
        },
        fail: (error) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  },

  /**
   * 获取动态报名字段配置
   * GET /registration/form/fields
   */
  getFormFields(conferenceId) {
    return get('/registration/form/fields', { conferenceId })
  },

  /**
   * 保存动态报名字段配置
   * POST /registration/form/fields/save
   */
  saveFormFields(data) {
    return post('/registration/form/fields/save', data)
  },

  /**
   * 获取OCR规则校验结果
   * GET /registration/ocr/result
   */
  getOcrResult(registrationId) {
    return get('/registration/ocr/result', { registrationId })
  },

  /**
   * 生成PDF名册
   * POST /registration/pdf/roster
   */
  generatePdfRoster(conferenceId, coverImage, remarks) {
    return getBlob(`/registration/pdf/roster?conferenceId=${conferenceId}&coverImage=${coverImage || ''}&remarks=${remarks || ''}`)
  },

  /**
   * 生成会议注册二维码
   * GET /registration/qr/generate
   */
  generateQrCode(conferenceId, registrationUrl) {
    return get('/registration/qr/generate', {
      conferenceId,
      registrationUrl
    })
  },

  // ============ 手册管理 ============
  /**
   * 手册列表
   * GET /registration/handbook/list?meetingId={id}
   */
  listHandbooks(meetingId) {
    return get('/registration/handbook/list', { meetingId: meetingId })
  },

  /**
   * 手册详情
   * GET /registration/handbook/{id}
   */
  getHandbookDetail(id) {
    return get(`/registration/handbook/${id}`)
  },

  /**
   * 保存手册
   * POST /registration/handbook/save
   */
  saveHandbook(data) {
    return post('/registration/handbook/save', data)
  },

  /**
   * 删除手册
   * DELETE /registration/handbook/{id}
   */
  deleteHandbook(id) {
    return post(`/registration/handbook/${id}/delete`)
  },

  /**
   * 发布手册
   * POST /registration/handbook/{id}/publish
   */
  publishHandbook(id) {
    return post(`/registration/handbook/${id}/publish`)
  },

  /**
   * 手册讨论列表
   * GET /registration/handbook/{id}/discussions
   */
  listHandbookDiscussions(id) {
    return get(`/registration/handbook/${id}/discussions`)
  },

  /**
   * 添加手册讨论
   * POST /registration/handbook/discussion/add
   */
  addHandbookDiscussion(data) {
    return post('/registration/handbook/discussion/add', data)
  },

  // ============ 分组管理 ============
  /**
   * 分组列表
   * GET /api/grouping/list?meetingId={id}
   */
  listGroupings(meetingId) {
    return get('/grouping/list', { meetingId: meetingId })
  },

  /**
   * 保存分组
   * POST /api/grouping/save
   */
  saveGrouping(data) {
    return post('/grouping/save', data)
  },

  /**
   * 更新分组名称
   * PUT /api/grouping/updateName
   */
  updateGroupName(data) {
    return put('/grouping/updateName', data)
  },

  /**
   * 更新组长
   * PUT /api/grouping/updateLeader
   */
  updateGroupLeader(data) {
    return put('/grouping/updateLeader', data)
  },

  /**
   * 调整成员
   * POST /api/grouping/adjustMembers
   */
  adjustGroupMembers(data) {
    return post('/grouping/adjustMembers', data)
  },

  /**
   * 移除成员
   * DELETE /api/grouping/removeMember
   */
  removeGroupMember(data) {
    return post('/grouping/removeMember', data)
  },

  /**
   * 删除全部分组
   * DELETE /api/grouping/deleteAll?meetingId={id}
   */
  deleteAllGroupings(meetingId) {
    return post(`/grouping/deleteAll?meetingId=${meetingId}`)
  }
}
