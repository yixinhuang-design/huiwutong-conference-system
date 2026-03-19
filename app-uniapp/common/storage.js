import { API_CONFIG } from './api';

export function getToken() {
  return uni.getStorageSync('token') || '';
}

export function getTenantId() {
  return uni.getStorageSync('tenantId') || API_CONFIG.DEFAULT_TENANT_ID;
}

export function getUserInfo() {
  const raw = uni.getStorageSync('userInfo');
  if (!raw) return null;
  try {
    return typeof raw === 'string' ? JSON.parse(raw) : raw;
  } catch (e) {
    return null;
  }
}
