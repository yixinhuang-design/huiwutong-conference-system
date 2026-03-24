#!/bin/bash
# Get login response and extract accessToken
LOGIN_RESP=$(curl -s -X POST 'http://127.0.0.1:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"admin123"}')
TOKEN=$(echo "$LOGIN_RESP" | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["accessToken"])')
echo "TOKEN_LEN=${#TOKEN}"

# Test alert stats
echo "=== ALERT STATS ==="
curl -s "http://127.0.0.1:8080/api/alert/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" 2>&1

echo ""
echo "=== TASK STATS ==="
curl -s "http://127.0.0.1:8080/api/task/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" 2>&1

# Check gateway config for routing
echo ""
echo "=== GATEWAY CONFIG ==="
find /www/wwwroot/conference-backend/ -name "*.yml" -o -name "*.yaml" -o -name "*.properties" 2>/dev/null | while read f; do
  if grep -l "alert\|task\|routes" "$f" 2>/dev/null; then
    echo "--- $f ---"
    cat "$f" 2>/dev/null
    echo ""
  fi
done

# Check gateway jar config
echo "=== GATEWAY JAR CONFIG ==="
ls -la /www/wwwroot/conference-backend/jars/ 2>/dev/null
echo ""
# Check application.yml in jars directory
find /www/wwwroot/conference-backend/ -name "application.yml" 2>/dev/null
echo ""

# Check gateway logs for routing info
echo "=== GATEWAY RECENT LOGS ==="
journalctl -u conference-gateway --no-pager -n 100 2>/dev/null | grep -i "alert\|task\|route\|error\|500" | tail -30

# Check data service - is it actually running?
echo ""
echo "=== PORT CHECK ==="
ss -tlnp | grep -E "808[0-9]"

# Check data service actual logs
echo ""
echo "=== DATA SERVICE STARTUP LOG ==="
journalctl -u conference-data --no-pager -n 100 2>/dev/null | grep -v "^--" | tail -30
