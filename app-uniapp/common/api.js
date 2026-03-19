const ENV = {
  dev: {
    auth: 'http://localhost:8081',
    meeting: 'http://localhost:8084',
    registration: 'http://localhost:8082'
  }
};

export const API_CONFIG = {
  BASE_URL: ENV.dev,
  DEFAULT_TENANT_ID: '2027317834622709762',
  TIMEOUT: 60000,
  ENDPOINTS: {
    LOGIN: '/api/auth/login',
    MEETING_CREATE: '/api/meeting/create',
    MEETING_LIST: '/api/meeting/list?pageNum=1&pageSize=100',
    FORM_FIELDS: '/api/registration/form/fields',
    REGISTRATION_UPLOAD: '/api/registration/upload',
    REGISTRATION_OCR: '/api/registration/ocr/idCard',
    REGISTRATION_SUBMIT: '/api/registration/submit'
  }
};
