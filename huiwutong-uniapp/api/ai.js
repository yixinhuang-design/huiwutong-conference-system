/**
 * AI服务API
 * 对接AI服务 (端口8085)
 * 智能对话、知识库、FAQ、OCR、语音识别
 */

import { get, post, put, del } from './request'

export default {
  // ============ 智能聊天 ============
  /**
   * AI对话
   * POST /api/ai/chat
   */
  chat(data) {
    return post('/ai/chat', data)
  },

  /**
   * 对话列表
   * GET /api/ai/conversations?meetingId={id}
   */
  listConversations(meetingId) {
    return get('/ai/conversations', { meetingId: meetingId })
  },

  // ============ 对话管理 ============
  /**
   * 对话详情
   * GET /api/ai/conversation/{id}
   */
  getConversation(id) {
    return get(`/ai/conversation/${id}`)
  },

  /**
   * 更新对话标题
   * PUT /api/ai/conversation/{id}/title
   */
  updateConversationTitle(id, title) {
    return put(`/ai/conversation/${id}/title`, { title })
  },

  /**
   * 删除对话
   * DELETE /api/ai/conversation/{id}
   */
  deleteConversation(id) {
    return del(`/ai/conversation/${id}`)
  },

  /**
   * 对话消息
   * GET /api/ai/conversation/{id}/messages
   */
  getConversationMessages(id, params) {
    return get(`/ai/conversation/${id}/messages`, params)
  },

  // ============ 知识库管理 ============
  /**
   * 知识库列表
   * GET /api/ai/knowledge?meetingId={id}
   */
  listKnowledge(meetingId) {
    return get('/ai/knowledge', { meetingId: meetingId })
  },

  /**
   * 添加知识
   * POST /api/ai/knowledge
   */
  addKnowledge(data) {
    return post('/ai/knowledge', data)
  },

  /**
   * 上传文档到知识库
   * POST /api/ai/knowledge/upload
   * Content-Type: multipart/form-data
   */
  uploadKnowledge(filePath, meetingId) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/ai/knowledge/upload',
        filePath,
        name: 'file',
        formData: {
          meetingId
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
   * 删除知识
   * DELETE /api/ai/knowledge/{id}
   */
  deleteKnowledge(id) {
    return del(`/ai/knowledge/${id}`)
  },

  // ============ FAQ管理 ============
  /**
   * FAQ列表
   * GET /api/ai/faq?meetingId={id}
   */
  listFaq(meetingId) {
    return get('/ai/faq', { meetingId: meetingId })
  },

  /**
   * 添加FAQ
   * POST /api/ai/faq
   */
  addFaq(data) {
    return post('/ai/faq', data)
  },

  /**
   * 删除FAQ
   * DELETE /api/ai/faq/{id}
   */
  deleteFaq(id) {
    return del(`/ai/faq/${id}`)
  },

  // ============ 反馈与统计 ============
  /**
   * 提交反馈
   * POST /api/ai/feedback
   */
  submitFeedback(data) {
    return post('/ai/feedback', data)
  },

  /**
   * 反馈列表
   * GET /api/ai/feedback/list?meetingId={id}
   */
  listFeedback(meetingId) {
    return get('/ai/feedback/list', { meetingId: meetingId })
  },

  /**
   * AI统计
   * GET /api/ai/stats?meetingId={id}
   */
  getStats(meetingId) {
    return get('/ai/stats', { meetingId: meetingId })
  },

  /**
   * AI能力列表
   * GET /api/ai/capabilities
   */
  getCapabilities() {
    return get('/ai/capabilities')
  },

  /**
   * 上下文类型列表
   * GET /api/ai/contexts?meetingId={id}
   */
  listContexts(meetingId) {
    return get('/ai/contexts', { meetingId: meetingId })
  },

  // ============ OCR与语音识别 ============
  /**
   * OCR识别
   * POST /api/ai/ocr/recognize
   * Content-Type: multipart/form-data
   */
  recognizeOcr(filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/ai/ocr/recognize',
        filePath,
        name: 'file',
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              uni.showToast({
                title: 'OCR识别失败',
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
            title: 'OCR识别失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  },

  /**
   * 语音识别
   * POST /api/ai/speech/recognize
   * Content-Type: multipart/form-data
   */
  recognizeSpeech(filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/ai/speech/recognize',
        filePath,
        name: 'file',
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              uni.showToast({
                title: '语音识别失败',
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
            title: '语音识别失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  }
}
