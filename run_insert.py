#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, '/root/.local/lib/python3.14/site-packages')

try:
    import mysql.connector
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
        (1001, 'kaimbushi', 9, 10, 'zhuhuichangAting', 'lizhurenm', 'zhanggongjingli', 3),
        (1002, 'zhutibogao: shuzihuazhuanxing', 10, 11.5, 'zhuhuichangAting', 'wangzhuichi', 'zhaojiaooshou', 2),
        (1003, 'fenluntanbuwenhuaxueyingyong', 11.75, 13.25, 'fenhuichangBting', 'chenzhuichi', 'liuboshir', 2),
        (1004, 'wucanxiuxi', 13.5, 14.5, 'caninsququ', None, None, 1),
        (1005, 'bimu', 14.5, 15.5, 'zhuhuichangAting', 'lizhurenm', 'lizhurenm', 3),
    ]
    
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    print('[OK] Connected')
    
    for sid, title, start_h, end_h, loc, host, speaker, priority in schedules:
        start = datetime.combine(today, datetime.min.time()) + timedelta(hours=start_h)
        end = datetime.combine(today, datetime.min.time()) + timedelta(hours=end_h)
        sql = "INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, 1, %s, %s, NOW(), 0)"
        cursor.execute(sql, (sid, MEETING_ID, TENANT_ID, title, start, end, loc, host, speaker, priority, CREATED_BY))
    
    conn.commit()
    print('[OK] Insert 5 schedules')
    
    cursor.execute("SELECT COUNT(*) FROM conf_schedule WHERE meeting_id = %s AND tenant_id = %s AND deleted = 0", (MEETING_ID, TENANT_ID))
    total = cursor.fetchone()[0]
    print('[OK] Total: ' + str(total))
    print('')
    print('[SUCCESS] Data inserted! Refresh browser.')
    
    cursor.close()
    conn.close()
except Exception as e:
    print('[ERROR] ' + str(e))
    import traceback
    traceback.print_exc()
