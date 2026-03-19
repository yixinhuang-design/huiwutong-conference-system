#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import mysql.connector
from datetime import datetime, timedelta

# 数据库连接信息
config = {
    'host': 'localhost',
    'port': 3308,
    'user': 'root',
    'password': 'Hnhx@123',
    'database': 'conference_registration'
}

# 参数
MEETING_ID = 2030309010523144194
TENANT_ID = 2027317834622709762
CREATED_BY = 8

# 计算日期时间
today = datetime.now().date()

schedules_data = [
    {
        'id': 1001,
        'title': '开幕式',
        'start': datetime.combine(today, datetime.min.time()) + timedelta(hours=9),
        'end': datetime.combine(today, datetime.min.time()) + timedelta(hours=10),
        'location': '主会场A厅',
        'host': '李主任',
        'speaker': '张总经理',
        'priority': 3
    },
    {
        'id': 1002,
        'title': '主题报告：数字化转型',
        'start': datetime.combine(today, datetime.min.time()) + timedelta(hours=10),
        'end': datetime.combine(today, datetime.min.time()) + timedelta(hours=11, minutes=30),
        'location': '主会场A厅',
        'host': '王主持',
        'speaker': '赵教授',
        'priority': 2
    },
    {
        'id': 1003,
        'title': '分论坛：人工智能应用',
        'start': datetime.combine(today, datetime.min.time()) + timedelta(hours=11, minutes=45),
        'end': datetime.combine(today, datetime.min.time()) + timedelta(hours=13, minutes=15),
        'location': '分会场B厅',
        'host': '陈主持',
        'speaker': '刘博士',
        'priority': 2
    },
    {
        'id': 1004,
        'title': '午餐休息',
        'start': datetime.combine(today, datetime.min.time()) + timedelta(hours=13, minutes=30),
        'end': datetime.combine(today, datetime.min.time()) + timedelta(hours=14, minutes=30),
        'location': '餐饮区',
        'host': None,
        'speaker': None,
        'priority': 1
    },
    {
        'id': 1005,
        'title': '闭幕式',
        'start': datetime.combine(today, datetime.min.time()) + timedelta(hours=14, minutes=30),
        'end': datetime.combine(today, datetime.min.time()) + timedelta(hours=15, minutes=30),
        'location': '主会场A厅',
        'host': '李主任',
        'speaker': '李主任',
        'priority': 3
    }
]

try:
    # 连接数据库
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    
    print("已连接到数据库")
    print("   Host: " + config['host'] + ":" + str(config['port']))
    print("   Database: " + config['database'])
    print("")
    
    # 插入数据
    insert_sql = """
    INSERT INTO conf_schedule 
    (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, 1, %s, %s, NOW(), 0)
    """
    
    for schedule in schedules_data:
        cursor.execute(insert_sql, (
            schedule['id'],
            MEETING_ID,
            TENANT_ID,
            schedule['title'],
            schedule['start'],
            schedule['end'],
            schedule['location'],
            schedule['host'],
            schedule['speaker'],
            schedule['priority'],
            CREATED_BY
        ))
    
    conn.commit()
    print("成功插入 " + str(len(schedules_data)) + " 条日程数据")
    print("")
    
    # 验证
    cursor.execute("""
    SELECT COUNT(*) as total FROM conf_schedule 
    WHERE meeting_id = %s AND tenant_id = %s AND deleted = 0
    """, (MEETING_ID, TENANT_ID))
    
    total = cursor.fetchone()[0]
    print("验证结果: " + str(total) + " 条日程已保存")
    print("")
    
    # 显示插入的数据
    cursor.execute("""
    SELECT id, title, start_time, end_time FROM conf_schedule 
    WHERE meeting_id = %s AND tenant_id = %s AND deleted = 0
    ORDER BY start_time ASC
    """, (MEETING_ID, TENANT_ID))
    
    print("完整日程列表:")
    print("-" * 80)
    for row in cursor.fetchall():
        print("  ID: " + str(row[0]).ljust(6) + " | " + str(row[1]).ljust(25) + " | " + str(row[2]) + " -> " + str(row[3]))
    print("-" * 80)
    print("")
    print("数据插入成功！请刷新浏览器查看日程列表。")
    
    cursor.close()
    conn.close()
    
except mysql.connector.Error as err:
    if err.errno == 2003:
        print("连接失败: 无法连接到 MySQL 服务器")
        print("   请检查 MySQL 是否运行在 localhost:3308")
    else:
        print("数据库错误: " + str(err))
except Exception as e:
    print("错误: " + str(e))
