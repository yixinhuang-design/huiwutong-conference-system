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
        url: 'http://localhost:8080/api/v1/registration/upload',
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
  }
}
