/**
 * 附件服务API
 * 对接会议服务 (端口8084)
 * 支持普通上传和分块断点续传
 */

import { get, post, put, del } from './request'

export default {
  // ============ 普通上传 ============
  /**
   * 上传附件
   * POST /api/schedule-attachment/upload
   * Content-Type: multipart/form-data
   */
  upload(filePath, scheduleId, meetingId) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/schedule-attachment/upload',
        filePath,
        name: 'file',
        formData: {
          scheduleId,
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
   * 批量上传
   * POST /api/schedule-attachment/upload-batch
   * Content-Type: multipart/form-data
   */
  uploadBatch(filePaths, scheduleId, meetingId) {
    return new Promise((resolve, reject) => {
      const uploads = filePaths.map(filePath => {
        return new Promise((res, rej) => {
          uni.uploadFile({
            url: 'http://localhost:8080/api/schedule-attachment/upload-batch',
            filePath,
            name: 'files',
            formData: {
              scheduleId,
              meetingId
            },
            success: (response) => {
              if (response.statusCode === 200) {
                res(JSON.parse(response.data))
              } else {
                rej(response)
              }
            },
            fail: rej
          })
        })
      })

      Promise.all(uploads)
        .then(results => resolve(results))
        .catch(error => {
          uni.showToast({
            title: '批量上传失败',
            icon: 'none'
          })
          reject(error)
        })
    })
  },

  // ============ 附件管理 ============
  /**
   * 附件列表
   * GET /api/schedule-attachment/list?scheduleId={scheduleId}
   */
  list(scheduleId) {
    return get('/schedule-attachment/list', { scheduleId })
  },

  /**
   * 附件详情
   * GET /api/schedule-attachment/{id}
   */
  getDetail(id) {
    return get(`/schedule-attachment/${id}`)
  },

  /**
   * 删除附件
   * DELETE /api/schedule-attachment/{id}
   */
  delete(id) {
    return del(`/schedule-attachment/${id}`)
  },

  /**
   * 更新附件
   * PUT /api/schedule-attachment/{id}
   */
  update(id, data) {
    return put(`/schedule-attachment/${id}`, data)
  },

  /**
   * 获取下载URL
   * GET /api/schedule-attachment/{id}/download-url
   */
  getDownloadUrl(id) {
    return get(`/schedule-attachment/${id}/download-url`)
  },

  /**
   * 下载附件（直接下载）
   * GET /api/schedule-attachment/{id}/download
   */
  download(id) {
    return get(`/schedule-attachment/${id}/download`)
  },

  /**
   * 校验附件
   * POST /api/schedule-attachment/validate
   */
  validate(data) {
    return post('/schedule-attachment/validate', data)
  },

  // ============ 分块上传（断点续传） ============
  /**
   * 初始化分块上传
   * POST /api/schedule-attachment/chunk/init
   */
  initChunkUpload(data) {
    return post('/schedule-attachment/chunk/init', data)
  },

  /**
   * 上传单个分块
   * POST /api/schedule-attachment/chunk/upload
   * Content-Type: multipart/form-data
   */
  uploadChunk(filePath, uploadId, chunkIndex) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/schedule-attachment/chunk/upload',
        filePath,
        name: 'file',
        formData: {
          uploadId,
          chunkIndex
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(JSON.parse(res.data))
          } else {
            reject(res)
          }
        },
        fail: (error) => {
          uni.showToast({
            title: '分块上传失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  },

  /**
   * 合并分块
   * POST /api/schedule-attachment/chunk/merge
   */
  mergeChunks(data) {
    return post('/schedule-attachment/chunk/merge', data)
  },

  /**
   * 查询上传进度
   * GET /api/schedule-attachment/chunk/progress?uploadId={uploadId}
   */
  getChunkProgress(uploadId) {
    return get('/schedule-attachment/chunk/progress', { uploadId })
  },

  /**
   * 取消分块上传
   * DELETE /api/schedule-attachment/chunk/cancel?uploadId={uploadId}
   */
  cancelChunkUpload(uploadId) {
    return del('/schedule-attachment/chunk/cancel', { uploadId })
  },

  /**
   * 检查文件是否存在（秒传）
   * GET /api/schedule-attachment/chunk/check?md5={md5}
   */
  checkFileExists(md5) {
    return get('/schedule-attachment/chunk/check', { md5 })
  },

  /**
   * 获取已上传分块列表
   * GET /api/schedule-attachment/chunk/uploaded?uploadId={uploadId}
   */
  getUploadedChunks(uploadId) {
    return get('/schedule-attachment/chunk/uploaded', { uploadId })
  }
}
