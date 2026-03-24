#!/bin/bash
# Get token
TOKEN=$(curl -s -X POST 'http://127.0.0.1:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"admin123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["token"])')

echo "=== TOKEN obtained ==="

# Test alert stats
echo "=== ALERT STATS ==="
curl -s -w '\nHTTP_CODE:%{http_code}\n' \
  "http://127.0.0.1:8080/api/alert/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN"

echo "=== TASK STATS ==="
curl -s -w '\nHTTP_CODE:%{http_code}\n' \
  "http://127.0.0.1:8080/api/task/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN"

# Check gateway logs for errors
echo "=== GATEWAY LOGS (last errors) ==="
journalctl -u conference-gateway --no-pager -n 50 2>/dev/null | grep -i "error\|500\|alert\|task" | tail -20

# Check which service handles alert and task
echo "=== CHECKING ROUTES ==="
cat /www/wwwroot/conference-backend/conf/gateway-routes.yml 2>/dev/null || echo "No routes file found"
grep -r "alert\|task" /www/wwwroot/conference-backend/conf/ 2>/dev/null | head -20
