import { API_CONFIG } from './api';
import { getToken, getTenantId } from './storage';

export function request({ service, url, method = 'GET', data, header = {}, showLoading = false }) {
  const baseUrl = API_CONFIG.BASE_URL[service];
  const fullUrl = `${baseUrl}${url}`;

  if (showLoading) {
    uni.showLoading({ title: '加载中...', mask: true });
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      timeout: API_CONFIG.TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        'Authorization': getToken() ? `Bearer ${getToken()}` : '',
        'X-Tenant-Id': getTenantId(),
        ...header
      },
      success: (res) => {
        if (showLoading) uni.hideLoading();
        const body = res.data || {};
        if (res.statusCode !== 200) {
          reject(new Error(`HTTP ${res.statusCode}`));
          return;
        }
        if (body.code !== 200 && body.code !== 0) {
          reject(new Error(body.message || '请求失败'));
          return;
        }
        resolve(body.data);
      },
      fail: (err) => {
        if (showLoading) uni.hideLoading();
        reject(err);
      }
    });
  });
}
