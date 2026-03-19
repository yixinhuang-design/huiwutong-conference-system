<template>
  <view class="container">
    <view class="card">
      <view class="title">扫码报名</view>
      <view class="hint">会议ID：{{ conferenceId }}</view>
    </view>

    <view class="card">
      <input class="input" v-model="form.realName" placeholder="姓名*" />
      <input class="input" v-model="form.phone" placeholder="手机号*" type="number" maxlength="11" />
      <input class="input" v-model="form.department" placeholder="单位*" />
      <input class="input" v-model="form.position" placeholder="职位*" />
      <input class="input" v-model="form.remark" placeholder="备注" />

      <view v-for="field in dynamicFields" :key="field.key" class="field-row">
        <input
          v-if="field.type !== 'select'"
          class="input"
          v-model="dynamicValues[field.key]"
          :placeholder="`${field.label}${field.required ? '*' : ''}`"
        />
        <picker v-else :range="field.options || []" @change="(e) => onSelect(e, field)">
          <view class="picker">{{ dynamicValues[field.key] || `请选择${field.label}` }}</view>
        </picker>
      </view>

      <button @click="uploadIdCard">上传身份证</button>
      <view class="hint">{{ uploadStatus }}</view>

      <label class="agree"><checkbox :checked="agree" @click="agree = !agree" />我已阅读并同意报名须知</label>
      <button class="btn-primary" @click="submit" :loading="submitting">提交报名</button>
    </view>
  </view>
</template>

<script>
import { request } from '../../../common/request';
import { API_CONFIG } from '../../../common/api';

export default {
  data() {
    return {
      conferenceId: '',
      form: {
        realName: '',
        phone: '',
        department: '',
        position: '',
        remark: ''
      },
      dynamicFields: [],
      dynamicValues: {},
      upload: {
        fileUrl: '',
        ocr: null
      },
      uploadStatus: '未上传',
      agree: false,
      submitting: false
    };
  },
  async onLoad(options) {
    this.conferenceId = options.conferenceId || options.meetingId || '';
    if (!this.conferenceId) {
      uni.showToast({ title: '缺少会议ID', icon: 'none' });
      return;
    }
    await this.loadDynamicFields();
  },
  methods: {
    async loadDynamicFields() {
      try {
        const data = await request({
          service: 'registration',
          url: `${API_CONFIG.ENDPOINTS.FORM_FIELDS}?conferenceId=${this.conferenceId}`,
          method: 'GET'
        });
        const fixed = new Set(['name', 'realname', 'phone', 'unit', 'department', 'position', 'remark', 'email']);
        this.dynamicFields = (Array.isArray(data) ? data : [])
          .filter(f => f && f.enabled !== false && !fixed.has((f.key || '').toLowerCase()))
          .sort((a, b) => (a.sort || 0) - (b.sort || 0));
      } catch (e) {
        uni.showToast({ title: `动态字段加载失败: ${e.message}`, icon: 'none' });
      }
    },
    onSelect(e, field) {
      const options = field.options || [];
      this.dynamicValues[field.key] = options[e.detail.value] || '';
    },
    uploadIdCard() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          this.doUpload(filePath);
        }
      });
    },
    doUpload(filePath) {
      this.uploadStatus = '上传中...';
      const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
      const token = uni.getStorageSync('token') || uni.getStorageSync('auth_token') || '';
      
      uni.uploadFile({
        url: `${API_CONFIG.BASE_URL.registration}${API_CONFIG.ENDPOINTS.REGISTRATION_UPLOAD}`,
        filePath,
        name: 'file',
        formData: {
          conferenceId: String(this.conferenceId),
          fileType: 'idCard'
        },
        header: {
          'X-Tenant-Id': tenantId,
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: async (resp) => {
          try {
            const parsed = JSON.parse(resp.data || '{}');
            if (resp.statusCode !== 200 || parsed.code !== 200) {
              throw new Error(parsed.message || '上传失败');
            }
            this.upload.fileUrl = parsed.data.fileUrl;
            this.uploadStatus = '上传成功，OCR识别中...';

            const ocr = await request({
              service: 'registration',
              url: `${API_CONFIG.ENDPOINTS.REGISTRATION_OCR}?photoUrl=${encodeURIComponent(this.upload.fileUrl)}`,
              method: 'POST'
            });
            this.upload.ocr = ocr;
            this.uploadStatus = `OCR: ${ocr?.ocrStatus || 'REVIEW'}`;
          } catch (e) {
            this.uploadStatus = `上传失败: ${e.message}`;
          }
        },
        fail: () => {
          this.uploadStatus = '上传失败';
        }
      });
    },
    async submit() {
      if (!this.form.realName || !this.form.phone || !this.form.department || !this.form.position) {
        uni.showToast({ title: '请填写必填项', icon: 'none' });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        uni.showToast({ title: '手机号格式错误', icon: 'none' });
        return;
      }
      if (!this.agree) {
        uni.showToast({ title: '请先同意报名须知', icon: 'none' });
        return;
      }
      for (const field of this.dynamicFields) {
        if (field.required && !this.dynamicValues[field.key]) {
          uni.showToast({ title: `请填写${field.label}`, icon: 'none' });
          return;
        }
      }

      this.submitting = true;
      try {
        const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || API_CONFIG.DEFAULT_TENANT_ID;
        
        const registrationData = {
          realName: this.form.realName,
          name: this.form.realName,
          phone: this.form.phone,
          unit: this.form.department,
          department: this.form.department,
          position: this.form.position,
          remark: this.form.remark,
          ...this.dynamicValues,
          files: { idCardPhotoUrl: this.upload.fileUrl || '' },
          ocr: this.upload.ocr || null
        };

        console.log('=== 报名提交信息 ===');
        console.log('会议ID:', this.conferenceId);
        console.log('租户ID:', tenantId);
        console.log('姓名:', this.form.realName);
        console.log('手机号:', this.form.phone);
        
        const result = await request({
          service: 'registration',
          url: API_CONFIG.ENDPOINTS.REGISTRATION_SUBMIT,
          method: 'POST',
          header: {
            'X-Tenant-Id': tenantId
          },
          data: {
            conferenceId: Number(this.conferenceId),
            realName: this.form.realName,
            phone: this.form.phone,
            email: '',
            department: this.form.department,
            position: this.form.position,
            idCard: this.upload?.ocr?.idCard || '未识别',
            idCardPhotoUrl: this.upload.fileUrl || '',
            diplomaPhotoUrl: '',
            registrationData: JSON.stringify(registrationData)
          }
        });

        // 验证返回的数据
        console.log('=== 报名提交返回结果 ===');
        console.log('返回的报名ID:', result?.id);
        console.log('返回的会议ID:', result?.conferenceId);
        console.log('返回的租户ID:', result?.tenantId);
        console.log('返回的状态:', result?.status);
        
        if (!result || !result.id) {
          throw new Error('服务器返回数据异常，缺少报名ID');
        }
        
        if (result.conferenceId !== Number(this.conferenceId)) {
          console.warn('警告：返回的会议ID与提交时不一致！提交:', Number(this.conferenceId), '返回:', result.conferenceId);
        }

        uni.showToast({ title: '报名提交成功', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } catch (e) {
        console.error('提交失败:', e);
        uni.showToast({ title: `提交失败: ${e.message || '网络错误'}`, icon: 'none', duration: 3000 });
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.title { font-size: 34rpx; font-weight: 700; margin-bottom: 10rpx; }
.hint { color: #64748b; font-size: 24rpx; margin-bottom: 10rpx; }
.input { border: 2rpx solid #e2e8f0; border-radius: 14rpx; height: 84rpx; padding: 0 20rpx; margin-bottom: 14rpx; background: #fff; }
.picker { border: 2rpx solid #e2e8f0; border-radius: 14rpx; height: 84rpx; padding: 0 20rpx; display: flex; align-items: center; color: #64748b; margin-bottom: 14rpx; }
.agree { display: flex; align-items: center; color: #334155; margin: 12rpx 0 18rpx; font-size: 26rpx; }
.btn-primary { height: 88rpx; border-radius: 14rpx; }
</style>
