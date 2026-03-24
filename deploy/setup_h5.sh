#!/bin/bash
# 1. 更新网关白名单配置（在外部 prod yml 中添加白名单覆盖）
echo "=== 更新网关白名单 ==="

cat >> /opt/conference/config/gateway-prod.yml << 'WHITELIST_EOF'

# 报名H5页面免认证白名单
gateway:
  white-list:
    - /api/v1/auth/login
    - /api/v1/auth/register
    - /api/v1/auth/captcha
    - /api/v1/auth/sms-code
    - /api/auth/login
    - /api/auth/register
    - /api/auth/captcha
    - /api/auth/sms-code
    - /api/tenant/list
    - /api/registration/submit
    - /api/registration/query
    - /api/registration/upload
    - /api/registration/ocr/**
    - /api/registration/form/fields
    - /api/registration/qr/generate
    - /api/meeting/**
    - /actuator/**
    - /doc.html
    - /swagger-resources/**
    - /v3/api-docs/**
    - /webjars/**
WHITELIST_EOF

echo "白名单配置已更新"
cat /opt/conference/config/gateway-prod.yml

# 2. 创建 H5 页面目录
echo ""
echo "=== 创建 H5 页面目录 ==="
mkdir -p /www/wwwroot/conference-admin/conference-admin-pc/app/learner/
echo "目录已创建"

# 3. 重启网关
echo ""
echo "=== 重启网关服务 ==="
systemctl restart conference-gateway
sleep 10

# 4. 验证网关端口
ss -tlnp | grep 8080
echo ""
echo "网关服务状态: $(systemctl is-active conference-gateway)"

# 5. 测试报名API是否免认证
echo ""
echo "=== 测试免认证API ==="
echo "--- registration/form/fields ---"
curl -s -w '\nHTTP:%{http_code}' "http://127.0.0.1:8080/api/registration/form/fields?conferenceId=2036011038369902593" -H "X-Tenant-Id: 100" 2>&1 | tail -c 200

echo ""
echo "--- meeting/2036011038369902593 ---"
curl -s -w '\nHTTP:%{http_code}' "http://127.0.0.1:8080/api/meeting/2036011038369902593" -H "X-Tenant-Id: 100" 2>&1 | tail -c 200

echo ""
echo "=== 完成 ==="
