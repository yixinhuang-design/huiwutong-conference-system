#!/bin/bash
# Get login response
echo "=== LOGIN RESPONSE ==="
LOGIN_RESP=$(curl -s -X POST 'http://127.0.0.1:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"admin123"}')
echo "$LOGIN_RESP" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESP"

# Extract token - try different paths
TOKEN=$(echo "$LOGIN_RESP" | python3 -c 'import sys,json; d=json.load(sys.stdin); print(d.get("data",{}).get("token","") or d.get("data","") or d.get("token",""))' 2>/dev/null)
if [ -z "$TOKEN" ]; then
  TOKEN=$(echo "$LOGIN_RESP" | python3 -c 'import sys,json; d=json.load(sys.stdin)["data"]; print(d if isinstance(d,str) else d.get("accessToken","") or d.get("token",""))' 2>/dev/null)
fi
echo "TOKEN=$TOKEN"

if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not extract token"
  exit 1
fi

# Test alert stats
echo ""
echo "=== ALERT STATS ==="
curl -s "http://127.0.0.1:8080/api/alert/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

echo ""
echo "=== TASK STATS ==="
curl -s "http://127.0.0.1:8080/api/task/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

# Check all services status
echo ""
echo "=== SERVICE STATUS ==="
for svc in gateway auth registration notification meeting seating data collaboration; do
  STATUS=$(systemctl is-active conference-$svc 2>/dev/null)
  echo "conference-$svc: $STATUS"
done

# Check which services handle alert and task routes
echo ""
echo "=== GATEWAY APPLICATION.YML ROUTES ==="
find /www/wwwroot/conference-backend/ -name "application*.yml" -o -name "application*.properties" 2>/dev/null | head -5
cat /www/wwwroot/conference-backend/gateway-app/application.yml 2>/dev/null | grep -A5 "alert\|task\|route" | head -30

echo ""
echo "=== MEETING SERVICE LOGS (last 30 lines) ==="
journalctl -u conference-meeting --no-pager -n 30 2>/dev/null | tail -30

echo ""
echo "=== DATA SERVICE LOGS (last 30 lines) ==="
journalctl -u conference-data --no-pager -n 30 2>/dev/null | tail -30
