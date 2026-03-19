#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import pymysql
from datetime import datetime, timedelta

config = {
    'host': 'localhost',
    'port': 3308,
    'user': 'root',
    'password': 'Hnhx@123',
    'database': 'conference_registration'
}

MEETING_ID = 2030309010523144194
TENANT_ID = 2027317834622709762
CREATED_BY = 8
today = datetime.now().date()

schedules = [
    (1001, '开幕式', 9, 10, '主会场A厅', '李主任', '张总经理', 3),
    (1002, '主题报告：数字化转型', 10, 11.5, '主会场A厅', '王主持', '赵教授', 2),
    (1003, '分论坛：人工智能应用', 11.75, 13.25, '分会场B厅', '陈主持', '刘博士', 2),
    (1004, '午餐休息', 13.5, 14.5, '餐饮区', None, None, 1),
    (1005, '闭幕式', 14.5, 15.5, '主会场A厅', '李主任', '李主任', 3),
]

try:
    conn = pymysql.connect(**config)
    cursor = conn.cursor()
    print('[OK] Connected to database')
    
    for sid, title, start_h, end_h, loc, host, speaker, priority in schedules:
        start = datetime.combine(today, datetime.min.time()) + timedelta(hours=start_h)
        end = datetime.combine(today, datetime.min.time()) + timedelta(hours=end_h)
        sql = "INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, 1, %s, %s, NOW(), 0)"
        cursor.execute(sql, (sid, MEETING_ID, TENANT_ID, title, start, end, loc, host, speaker, priority, CREATED_BY))
    
    conn.commit()
    print('[OK] Inserted 5 schedules')
    
    cursor.execute("SELECT COUNT(*) FROM conf_schedule WHERE meeting_id = %s AND tenant_id = %s AND deleted = 0", (MEETING_ID, TENANT_ID))
    total = cursor.fetchone()[0]
    print('[OK] Total schedules: ' + str(total))
    
    print('')
    print('[RESULT] Data inserted successfully! Refresh browser to see the schedules.')
    
    cursor.close()
    conn.close()
except Exception as e:
    print('[ERROR] ' + str(e))
