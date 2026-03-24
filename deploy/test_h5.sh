#!/bin/bash
echo "=== Test H5 page ==="
curl -s -o /dev/null -w '%{http_code}' "http://39.103.85.255/app/learner/scan-register.html?meetingId=2036011038369902593"
echo ""

echo "=== Test registration submit (no auth) ==="
curl -s -X POST 'http://127.0.0.1:8080/api/registration/submit' \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: 100' \
  -d '{"conferenceId":2036011038369902593,"realName":"测试用户","phone":"13800138001","department":"测试部门","position":"测试职位","email":"","idCard":"","idCardPhotoUrl":"","diplomaPhotoUrl":"","registrationData":"{}"}'
echo ""

echo "=== Test query status (no auth) ==="
curl -s "http://127.0.0.1:8080/api/registration/query?phone=13800138001&conferenceId=2036011038369902593" \
  -H 'X-Tenant-Id: 100'
echo ""

echo "=== Test QR code generate ==="
curl -s "http://127.0.0.1:8080/api/registration/qr/generate?conferenceId=2036011038369902593" \
  -H 'X-Tenant-Id: 100' \
  -H 'Host: 39.103.85.255' | head -c 200
echo "..."
echo ""

echo "=== All done ==="
