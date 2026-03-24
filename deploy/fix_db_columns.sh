#!/bin/bash
# Fix database column mismatches for conf_alert_event and task_info

MYSQL="mysql -u conference -p'Conference@2026'"

echo "=== Fixing conf_alert_event table ==="

# Add missing 'severity' column
$MYSQL conference_notification -e "
ALTER TABLE conf_alert_event 
  ADD COLUMN severity VARCHAR(20) DEFAULT NULL COMMENT 'high/medium/low' AFTER rule_id;
" 2>&1 && echo "Added 'severity' column" || echo "severity already exists or error"

# Add missing 'metric' column  
$MYSQL conference_notification -e "
ALTER TABLE conf_alert_event
  ADD COLUMN metric VARCHAR(100) DEFAULT NULL COMMENT '指标名称' AFTER rule_id;
" 2>&1 && echo "Added 'metric' column" || echo "metric already exists or error"

# Add missing 'threshold' column
$MYSQL conference_notification -e "
ALTER TABLE conf_alert_event
  ADD COLUMN threshold DECIMAL(10,2) DEFAULT NULL COMMENT '阈值' AFTER metric_value;
" 2>&1 && echo "Added 'threshold' column" || echo "threshold already exists or error"

# Fix metric_value type from double to decimal
$MYSQL conference_notification -e "
ALTER TABLE conf_alert_event
  MODIFY COLUMN metric_value DECIMAL(10,2) DEFAULT NULL COMMENT '指标值';
" 2>&1 && echo "Fixed metric_value type" || echo "metric_value fix error"

echo ""
echo "=== Fixing task_info table ==="

# Add missing 'category' column
$MYSQL conference_collaboration -e "
ALTER TABLE task_info
  ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT 'venue/student/custom' AFTER task_type;
" 2>&1 && echo "Added 'category' column" || echo "category already exists or error"

# Add 'config' column (entity has config but table has tags)
$MYSQL conference_collaboration -e "
ALTER TABLE task_info
  ADD COLUMN config TEXT DEFAULT NULL COMMENT 'JSON配置' AFTER target_groups;
" 2>&1 && echo "Added 'config' column" || echo "config already exists or error"

echo ""
echo "=== Verifying conf_alert_event ==="
$MYSQL conference_notification -e "DESCRIBE conf_alert_event;" 2>&1

echo ""
echo "=== Verifying task_info ==="
$MYSQL conference_collaboration -e "DESCRIBE task_info;" 2>&1

echo ""
echo "=== Restarting affected services ==="
systemctl restart conference-notification
systemctl restart conference-collaboration
sleep 5

echo "=== Service status ==="
systemctl is-active conference-notification
systemctl is-active conference-collaboration

echo ""
echo "=== Testing APIs ==="
TOKEN=$(curl -s -X POST 'http://127.0.0.1:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"admin123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["accessToken"])')

echo "Alert stats:"
curl -s "http://127.0.0.1:8080/api/alert/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" 2>&1

echo ""
echo "Task stats:"
curl -s "http://127.0.0.1:8080/api/task/stats?conferenceId=2036011038369902593" \
  -H "Authorization: Bearer $TOKEN" 2>&1

echo ""
echo "=== DONE ==="
